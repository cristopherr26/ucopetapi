# Colección de Postman — Person + Login

**Importable directo.** Cubre el CRUD de `Person`, el login, el token, la autorización y los bugs
ya corregidos: **94 peticiones, 513 comprobaciones.**

## Levantarlo

```bash
docker compose up -d                                        # la base
UCOPET_ADMIN_PASSWORD='UcopetAdmin2026*' ./gradlew bootRun   # la app
```

Arrancá así la primera vez y **la colección corre sin tocar nada**: esa es la clave que trae por
defecto la variable `adminPassword`.

Si arrancás sin esa variable, la app crea el administrador con una clave **al azar y la imprime una
sola vez** en la consola:

```
=====================================================================
No habia ningun administrador activo. Se creo el de arranque:
   correo:     admin@ucopet.com
   contrasena: xxxxxxxxxxxxxxxx
=====================================================================
```

Copiala en la variable `adminPassword` de la colección.

## Importar y correr

Postman → **Import** → `ucopet-login.postman_collection.json`
Después: click derecho en la colección → **Run collection**.

## Qué hay adentro

| Carpeta | Para qué |
|---|---|
| `0 · Preparar datos` | entra como admin y crea lo que las demás necesitan |
| `1 · Acceso` | login: bien, mal, sin rol, bloqueado |
| `2 · CRUD de Person` | alta, consulta, edición, baja |
| `3 · El token en accion` | qué abre el token, y **cerrar sesión** |
| `4 · Regresiones` | bugs ya arreglados, para que no vuelvan |
| `5 · Autorizacion` | quién puede hacer qué |
| `9 · Limpiar` | deja la base como estaba |

**Corré la colección entera, en orden.** Las peticiones se pasan datos entre sí: sueltas fallan.

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
