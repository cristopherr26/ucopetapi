# Evaluación del módulo `purchases` — UcoPet API

**Repositorio:** ucopetapi · **Alcance revisado:** `controllers/purchases`, `domain/purchases`, `dto/purchases`, `repository/purchases`, `service/purchases` (y sus dependencias directas: `person`, `headquarter`, `provider`, `product`)
**Fecha de revisión:** 25 de septiembre de 2026

## Resumen ejecutivo

El módulo de compras está bien estructurado en capas (controller → service → repository) y sigue las convenciones del resto del proyecto (DTOs con `record`, manejo de excepciones por `@RestControllerAdvice` acotado al paquete, seguridad con `@PreAuthorize`). Sin embargo, la revisión encontró un problema central que domina el resto de la evaluación: el commit más reciente sobre este módulo (`ef00723`, "refactor: desacopla módulo Purchase; corrige S8688") eliminó, de un solo golpe, toda la validación de integridad de negocio que existía una versión atrás. Hoy es posible crear una orden de compra con un proveedor inexistente, una sede inexistente o inactiva, y productos o servicios inexistentes o inactivos, y el cálculo de impuestos pasó de basarse en el catálogo real a confiar ciegamente en un campo que envía el cliente en el request. A esto se suma que el módulo no tiene ni una sola prueba automatizada. Ninguno de los hallazgos es exótico; son justamente el tipo de regresión que un desacople "de bajo nivel" (quitar dependencias del constructor) introduce cuando no se reemplaza la validación por otro mecanismo equivalente.

## 1. Regresión funcional: se perdió la validación contra el catálogo real

Comparando la versión actual de `PurchaseServiceImpl` con la anterior al commit `ef00723`, el `createPurchase` pasó de esto:

- Verificar que el proveedor (`supplierId`) existe, vía `ProviderService`, lanzando `SupplierNotFoundException` si no.
- Verificar que la sede (`headquarterId`) existe y está activa, vía `HeadquarterService`, lanzando `HeadquarterNotFoundException` / `HeadquarterInactiveException` si no.
- Para cada ítem, según su `itemType`, verificar que el producto o el servicio existe y está activo (y para servicios, que además sea "purchasable"), vía `ProductService`/`ServiceService`.
- Calcular la categoría fiscal de cada ítem leyendo `product.getTaxCategory()` o `service.getTaxCategory()` del catálogo — es decir, el impuesto lo determinaba el sistema, no el cliente.

A esto, en la versión actual:

- `createPurchase` no llama a ningún servicio externo. Guarda directamente lo que llega en el `PurchaseRequestDTO`.
- `supplierId`, `headquarterId` y `productId` de cada ítem son simplemente `UUID` sin ninguna verificación de existencia. Se puede crear una compra con cualquier UUID aleatorio en esos campos y la API responde `201 Created`.
- La categoría fiscal (`taxCategory`) ahora viene en el propio `PurchaseItemRequestDTO`, elegida por quien llama a la API, y el servicio la usa tal cual para calcular `totalTaxes`. Esto no es solo una pérdida de validación: es un cambio de quién es la fuente de verdad del impuesto, de "el catálogo" a "el cliente HTTP".
- Las excepciones `SupplierNotFoundException`, `HeadquarterNotFoundException`, `HeadquarterInactiveException`, `ProductNotFoundException`, `ProductInactiveException`, `ServiceItemNotFoundException` y `ServiceItemNotPurchasableException` fueron borradas junto con el código que las lanzaba.
- La respuesta (`PurchaseResponseDTO`) dejó de enriquecerse con nombres reales: `supplier`, `headquarter` y cada `item.catalogItem` ahora siempre devuelven `RelatedEntityDTO(id, null)` — el `name` quedó hardcodeado en `null` porque ya no hay ninguna llamada a `ProviderService`/`HeadquarterService`/`ProductService` para resolverlo.

El commit sí corrigió algo real y lo dejó explícito en el mensaje: el uso de `LocalDateTime.now()` sin zona horaria (issue de Sonar S8688) se reemplazó por `LocalDateTime.now(ZoneId.of("America/Bogota"))` en las cuatro operaciones que tocan fechas. Ese cambio puntual es correcto y vale la pena conservarlo. El problema es que se mezcló en el mismo commit con la eliminación de las cuatro validaciones de negocio de arriba, bajo el argumento de "desacoplar" el módulo (el diff quita `ProviderService`, `HeadquarterService`, `ProductService` y `ServiceService` del constructor de `PurchaseServiceImpl`, -141 líneas). Reducir el acoplamiento de un servicio es una meta válida, pero normalmente se resuelve con un patrón (eventos de dominio, un puerto/adaptador más liviano, validación asíncrona, o al menos una llamada de solo lectura) — no eliminando la validación sin más. Tal como quedó, cualquier persona con rol `ADMIN` puede registrar compras "fantasma": proveedores que no existen, sedes que no existen o están dadas de baja, e ítems que no están en el catálogo de productos/servicios ni activos. Dado que `Purchase` es la entrada del flujo de compras/gastos (`linkExpense` conecta la compra con un egreso), esto compromete la confiabilidad de los reportes financieros que dependan de estos datos.

Esto es lo primero que recomendaría resolver, y probablemente lo más rápido de justificar ante el equipo: no hace falta rediseñar nada, existe el código exacto que hacía esta validación una versión atrás en el historial de git (commit `823a172`), solo hay que decidir si se reintroduce tal cual o se reemplaza por un mecanismo desacoplado equivalente.

## 2. Máquina de estados de `PurchaseStatus` no se aplica

`PurchaseStatus` define `PENDING`, `LINKED`, `RECEIVED` y `CANCELLED`, pero ninguna de las transiciones (`receivePurchase`, `cancelPurchase`, `linkExpense`) valida el estado actual antes de cambiarlo. Con el código actual:

- Se puede "recibir" (`/receive`) una compra que ya está `CANCELLED`.
- Se puede "cancelar" (`/cancel`) una compra que ya está `RECEIVED` o ya `CANCELLED`.
- Se puede vincular un egreso (`/link-expense`) a una compra `CANCELLED`, o volver a vincular uno distinto a una que ya estaba `LINKED`, sobrescribiendo el `expenseId` anterior sin dejar rastro del cambio.

Ninguna de estas rutas tiene guardas de tipo "solo se puede cancelar si está en `PENDING` o `LINKED`", así que el enum comunica una intención (un ciclo de vida) que el servicio no hace cumplir. Esto suele terminar en datos inconsistentes que se descubren tarde, normalmente en conciliación contable.

## 3. Condición de carrera en la verificación de `purchaseNumber` duplicado

`createPurchase` primero llama a `purchaseRepository.existsByPurchaseNumber(...)` y, si no existe, hace `save(...)`. Entre esas dos operaciones no hay ninguna transacción explícita ni bloqueo, así que dos solicitudes concurrentes con el mismo `purchaseNumber` pueden pasar ambas la verificación y llegar juntas al `save`. La columna sí tiene `unique = true` en la entidad, así que la segunda inserción fallaría a nivel de base de datos con una `DataIntegrityViolationException` — pero `PurchaseExceptionHandler` no captura ese tipo de excepción, solo `DuplicatePurchaseNumberException` (que es la que lanza la verificación previa). El resultado es que, bajo concurrencia, el cliente que pierde la carrera recibe un `500 Internal Server Error` genérico en vez del `409 Conflict` que el propio commit `3a28667` ("fix: valida purchaseNumber duplicado con 409 en vez de 500") se propuso garantizar. La forma más simple de cerrar esto es capturar `DataIntegrityViolationException` alrededor del `save` en `createPurchase` y traducirla también a `DuplicatePurchaseNumberException`, en vez de depender solo de la verificación previa.

## 4. Resolución de personas: un `findById` por cada compra devuelta, sin capacidad de esto quede acotado

`toResponseDTO` llama a `resolvePerson` dos veces (`createdBy` y `updatedBy`) por cada `Purchase`. En el listado paginado (`GET /api/v1/purchases`), eso significa hasta `2 × size` llamadas a `PersonService.findById` (cada una con su propia consulta a la base de datos) solo para armar una página de resultados — con el `size` por defecto en 10, son hasta 20 consultas adicionales por página, y el controller no impone un tope máximo a `size` (`@RequestParam(defaultValue = "10") int size`), así que un cliente puede pedir `size=100000` y multiplicar el problema. Antes del desacople esto ya pasaba también con proveedor/sede/catálogo, así que al menos ahora hay menos llamadas que antes, pero el patrón de N+1 sobre `PersonService` se mantiene intacto y conviene resolverlo (por ejemplo, con un `findByIds` en lote) antes de que el volumen de compras crezca. Además, `resolvePerson` atrapa `ResponseStatusException` y en ese caso devuelve el nombre en `null` en silencio — es una decisión razonable para no romper el listado por una persona borrada, pero no queda registrada en ningún log, así que un dato "roto" (un `createdByPersonId` que ya no existe) pasa desapercibido.

## 5. `hasDiscount` es un campo sin efecto

`Purchase` y `PurchaseRequestDTO` tienen un booleano `hasDiscount`, se persiste y se devuelve en la respuesta, pero no participa en ningún cálculo: `subtotal`, `totalTaxes` y `total` se calculan exactamente igual sin importar su valor. O el campo quedó de una funcionalidad que nunca se terminó de implementar, o es puramente informativo y en ese caso valdría la pena decirlo en el nombre o en un comentario, porque tal como está invita a pensar que existe un descuento que en realidad no se aplica en ningún lado.

## 6. Duplicación de `ItemType` y `TaxCategory` entre módulos

Existen dos enums `ItemType` (`domain.purchases.ItemType` y `dto.sale.enums.ItemType`) y dos `TaxCategory` (`domain.purchases.TaxCategory`, con valores planos, y `domain.product.enums.TaxCategory`, que además trae el porcentaje asociado a cada categoría vía `getPercentage()`). El servicio de compras reimplementa las mismas tasas a mano en `taxRateFor` (`0.19`, `0.05`, `0`) en lugar de reusar `TaxCategory.getPercentage()` del módulo de producto. Esto ya causó un problema de sincronización real: en la versión anterior al desacople, el impuesto se calculaba con la categoría fiscal *del producto/servicio en el catálogo* (que usa el enum de `product.enums` con porcentaje); ahora se calcula con la categoría que declara el cliente en el request (que usa el enum de `purchases`, sin porcentaje, con la tasa hardcodeada en el servicio). Si en algún momento cambian las tasas de IVA en el módulo de producto, el módulo de compras no se entera y sigue facturando con las tasas viejas hasta que alguien lo note y actualice el `switch` a mano en dos lugares distintos.

## 7. Cobertura de pruebas: no hay ninguna

No existe ni un solo archivo de test bajo `src/test` para `controllers/purchases`, `service/purchases`, `domain/purchases` ni `repository/purchases`. El historial de git menciona "Pruebas de integración" en el commit `771930b`, pero no hay rastro de ellas en el estado actual del repositorio (o se borraron, o nunca llegaron a este módulo específico). Esto es agravante en combinación con el punto 1: un test de integración que ejercitara `createPurchase` con un `supplierId` inexistente habría detectado la regresión de validación en el mismo commit que la introdujo, antes de que llegara a `main`.

## 8. Observaciones menores

`PurchaseController.findByFilter` exige `headquarterId` pero no valida que exista; simplemente devuelve una página vacía si no hay compras para ese id, lo cual es un comportamiento aceptable pero vale la pena confirmarlo como intencional. El método `getPurchaseItems` sigue declarado en `PurchaseService`/`PurchaseServiceImpl` pero no está expuesto por ningún endpoint del controller — es código muerto o le falta su ruta. El uso de `America/Bogota` hardcodeado en cinco lugares distintos del servicio (correcto respecto al issue de Sonar, como se dijo en el punto 1) sería más mantenible como una constante o un bean inyectado, para no tener que tocar cinco líneas si la zona horaria de referencia cambiara. Y `currentPersonId()` asume que `SecurityContextHolder.getContext().getAuthentication().getName()` siempre es un UUID parseable y que `getAuthentication()` nunca es `null`; en un endpoint protegido por `@PreAuthorize("hasRole('ADMIN')")` esto normalmente se cumple, pero si alguna vez cambia el mecanismo de autenticación (por ejemplo, un token de servicio-a-servicio con otro formato de subject) esto lanza una excepción no controlada que cae en el `handleUnexpected` genérico (500) en vez de un error más claro.

## Priorización sugerida

1. Reintroducir la validación de existencia de proveedor, sede activa, y producto/servicio activo en `createPurchase`, y volver a calcular la categoría fiscal desde el catálogo en vez de confiar en el request del cliente. Es el hallazgo con mayor impacto de negocio y el más fácil de justificar, porque existe el código de referencia en el historial de git.
2. Agregar guardas de transición de estado en `receivePurchase`, `cancelPurchase` y `linkExpense`.
3. Capturar `DataIntegrityViolationException` en `createPurchase` para cerrar la condición de carrera del `purchaseNumber` duplicado.
4. Escribir pruebas de integración para el módulo completo — idealmente antes de tocar los puntos 1 y 2, para que sirvan de red de seguridad durante el cambio.
5. Resolver la duplicación de `ItemType`/`TaxCategory` y el N+1 de `PersonService`, que son mejoras de mantenibilidad y rendimiento pero no urgentes.
