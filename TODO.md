# TODO — Módulo de Compra (Purchase)

## Pendiente

- Integración con Product: no implementable todavía porque el módulo Product no existe en el proyecto (solo hay un `ProductController` vacío y un `ProductDTO` vacío, sin Service, Domain ni Repository). Los items de compra siguen guardando `productId` como UUID suelto, sin resolver su `name`.
- Autorización real por sede (restringir qué usuarios pueden operar sobre qué `headquarterId`): bloqueado porque `PersonDomain` y los claims del JWT no exponen `headquarterId`. No se puede resolver sin tocar el módulo de Person/Security, que está fuera de nuestro alcance.

## Decisiones de diseño cerradas

- No se implementará `PUT /purchases/{id}`. Para modificar una orden, el flujo es cancelar la existente (`POST /purchases/{id}/cancel`) y crear una nueva.
- Se descarta la convención de "muchos filtros" con paréntesis (`/purchases/(status,supplierId,...)`). El listado solo admite los filtros ya implementados como query params: `headquarterId` (obligatorio), `status` y `supplierId` (opcionales), además de `page`/`size` para paginación.

## Resuelto

- ~~Manejo global de excepciones~~ — `PurchaseExceptionHandler` (`controllers/purchases/exception/`) ya centraliza `PurchaseNotFoundException`, `SupplierNotFoundException`, `HeadquarterNotFoundException`, `HeadquarterInactiveException`, `MethodArgumentNotValidException` y `HttpMessageNotReadableException`, con catch-all genérico.
- ~~Inconsistencia en `getPurchaseItems`~~ — ahora valida que la Purchase exista (`PurchaseNotFoundException`) antes de devolver sus items, en vez de devolver silenciosamente una lista vacía.
- ~~Integración con Provider, Headquarter y Person~~ — `PurchaseServiceImpl` valida existencia real de proveedor y sede (y que la sede esté activa) antes de crear una compra, y resuelve los nombres de proveedor, sede y personas (`createdBy`/`updatedBy`) en cada respuesta consultando sus Services reales.
