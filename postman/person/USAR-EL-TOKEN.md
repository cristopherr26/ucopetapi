---
tags: [equipo, guia, seguridad, token]
---
# Cómo usar el token en tu módulo

**Desde el 2026-09-07 la API está cerrada: ninguna petición funciona sin token.** Esta guía es lo
que necesitás para que lo tuyo siga andando. Son cinco minutos.

## 1 · Pedir el token

```
POST /api/v1/persons/login
{ "email": "admin@ucopet.com", "password": "<la del arranque>" }
```

```json
{ "token": "eyJhbGciOiJIUzI1NiJ9…", "personId": "ef307a43-…",
  "fullName": "Carlos Pena Duque", "roles": ["ADMIN"] }
```

## 2 · Mandarlo en cada petición

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9…
```

**En Postman se pone una sola vez:** click derecho en tu colección → *Authorization* →
*Bearer Token* → `{{token}}`. Todas las peticiones lo heredan.

Eso es todo. Cuando vence (**12 h**), volvés a hacer login.

## 3 · Saber quién está llamando

Dentro de tu método, sin tocar el token:

```java
public ResponseEntity<X> crear(Authentication authentication) {
    UUID quienLlama = UUID.fromString(authentication.getName());   // el personId
}
```

> **Nunca decodifiquen el token a mano.** Cuando su método corre, el filtro ya verificó la firma,
> que no esté vencido, que la persona siga activa y que la sesión no se haya cerrado. Leer el JWT
> por su cuenta es repetir esa validación **y hacerla peor**.

---

## 🔧 ¿Necesitás otros permisos? Escribime

**A lo tuyo ya se le pusieron reglas**, agrupadas por los cinco procesadores del tablero —Usuarios,
Salud, Comercial, Inventario, Pagos—. Pero **las escribí yo sin consultarles**, porque cerrar la API
no podía esperar nueve respuestas. **Si la de tu módulo quedó mal, es esperable: decime y la
corrijo.**

Con una frase alcanza:

> *"En `/api/v1/loMio`: crear y borrar solo ADMIN, listar también DOCTOR."*

### Por qué no lo editan ustedes

`SecurityConfig` es **un solo archivo con las reglas de los 25 controllers**, y ahí **gana la primera
regla que coincide**. Una línea en el sitio equivocado no da error, no rompe el build, y **abre en
silencio** algo que debía estar cerrado. Nadie se entera hasta que alguien lo mira.

Hay además dos líneas cuyo efecto es global: si se tocan, **todos los errores de todos** vuelven
como `401` vacíos, o cambia el default de la API entera.

**No es celo, es que el archivo es uno solo.** Ustedes deciden la regla de negocio de su módulo; yo
la escribo donde va.

---

## Tres cosas que conviene saber

**El token no es secreto, es infalsificable.** Pegalo en [jwt.io](https://jwt.io) y lo leés entero:
es base64, no cifrado. Lo que nadie puede es **modificarlo** sin romper la firma. Así que **no metan
datos sensibles ahí** creyendo que van protegidos.

**Sin rol no hay token.** Si la persona no tiene ninguno, el login responde `401` aunque la
contraseña sea correcta. Hoy eso significa que **solo entra un administrador**: `DOCTOR` sale de la
tabla `doctors` y todavía no existe. En cuanto la creen, sus médicos entran solos.

**Cada reinicio de la app pide login de nuevo, y no es un error.** Si no se define
`UCOPET_JWT_SECRET`, la clave de firma se genera en cada arranque. La colección de Postman se
reloguea sola, así que no molesta.

---

**Para levantar el proyecto y probar:** [README.md](README.md)
