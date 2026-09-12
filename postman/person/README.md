# Colección de Postman — Person + Login

**Importable directo.** Cubre el CRUD de `Person`, el login, el token, la autorización y los bugs
ya corregidos: **94 peticiones, 609 comprobaciones.**

## Levantarlo

```bash
docker compose up -d     # la base
./gradlew bootRun        # la app
```

**Eso es todo: la colección corre sin configurar nada.** Si no hay ningún administrador, la app crea
el de arranque con una clave **fija y conocida**, que es la que ya trae la variable `adminPassword`:

```
correo:     admin@ucopet.com
contrasena: UcopetAdmin2026*
```

> ⚠️ **Esa clave está en el repositorio: cualquiera que lo lea la sabe.** Es una cuenta de arranque
> para desarrollo local, no una credencial. **Entrá con ella, creá tu propio usuario y cambiale la
> contraseña a esta.** Y si algún día esto se despliega, tiene que arrancar con
> `UCOPET_ADMIN_PASSWORD=<algo que nadie haya visto>`.

## Importar y correr

Postman → **Import** → `ucopet-login.postman_collection.json`
Después: click derecho en la colección → **Run collection**.

## Qué hay adentro

```
0 · Preparar datos          crea las cuentas que las demas necesitan

1 · ADMIN                   ← lo que SI funciona
    1.1 · Entrar
    1.2 · Ver y buscar personas
    1.3 · Crear, editar y dar de baja
    1.4 · Tipos de documento
    1.5 · Ponerle la clave a otro
    1.6 · Cambiar su propia clave
    1.7 · Que abre su token
    1.8 · Cerrar sesion

2 · PRUEBAS                 ← lo que NO debe funcionar
    2.1 · Credenciales malas
    2.2 · Cuenta sin contrasena
    2.3 · Cuenta sin rol
    2.4 · Cuenta bloqueada
    2.5 · Sin token
    2.6 · El token revocado
    2.7 · Validaciones de entrada
    2.8 · Reglas de negocio
    2.9 · CORS
    2.10 · Regresiones

9 · Limpiar                 deja la base como estaba
```

**Son dos historias.** `1 · ADMIN` es el camino feliz de punta a punta: entrar, trabajar, salir —
si eso pasa, el modulo sirve. `2 · PRUEBAS` es todo lo que **tiene que fallar**, agrupado por el
motivo: sin clave, sin rol, bloqueado, sin token, dato invalido.

**Corré la colección entera, en orden.** Las peticiones se pasan datos entre sí: una suelta puede
dar verde sin haber probado nada.

> Si ves una ruta con **dos barras** —`/api/v1/persons//password`— es eso: un `{{id}}` llegó vacío
> porque no corrió la petición que lo llena. La colección lo detecta sola y te lo dice.

## Lo único que hay que saber para consumir el login

```
POST /api/v1/persons/login   →   { "token": "...", "personId": "...", "roles": ["ADMIN"] }
```

Ese `token` va en **todas** las demás peticiones:

```
Authorization: Bearer <token>
```

La colección lo guarda sola al hacer login. → [USAR-EL-TOKEN.md](USAR-EL-TOKEN.md)

## Si algo falla

| Síntoma | Qué pasó |
|---|---|
| La primera petición da **401** | falta tu contraseña en la variable `adminPassword` |
| **Todo** da 401 de golpe tras reiniciar | normal: la app firma con una clave nueva. Volvé a hacer login |
| **423** | bloqueo por 5 intentos fallidos. Esperá 15 min o usá otro correo |
| La app no arranca | ¿está la base? `docker compose up -d` |
| Perdiste la contraseña del admin | `update persons set is_admin = false;` y reiniciá: se vuelve a crear |

## ¿Necesitás otros permisos en tu módulo?

**Escribime, no edites `SecurityConfig`.** Con una frase alcanza:

> *"En `/api/v1/loMio`: crear y borrar solo ADMIN, listar también DOCTOR."*

El porqué está en [USAR-EL-TOKEN.md](USAR-EL-TOKEN.md).
