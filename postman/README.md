# Postman

**Una carpeta por módulo.** Cada quien deja acá la colección de lo suyo, con su propio README.

```
postman/
└── person/     ← Person + Login (Pablo)
```

| Módulo | Carpeta | Estado |
|---|---|---|
| Person + Login | [`person/`](person/) | ✅ 77 peticiones · 424 aserciones |

## Para agregar la tuya

1. Creá `postman/<tu-modulo>/`.
2. Exportá la colección desde Postman (**Collection v2.1**) ahí adentro.
3. Un `README.md` al lado con **cómo arrancar y qué hace falta** — un compañero tiene que poder
   importarla y correrla sin preguntarte nada.

**Nada de credenciales reales en el JSON ni en el README** — ni siquiera de ejemplo, porque una
contraseña de aspecto real pegada "para ilustrar" se lee igual que una de verdad.

> La única excepción es lo que ya es público por diseño: `person/` trae `adminPassword` con la
> clave del **administrador de arranque local**, que existe para que la colección corra al
> importarla y solo sirve contra una base de desarrollo desechable. Un token, una clave de alguien
> real o la de un servidor **no** entran acá: van en variables de entorno.

> **Si tu colección necesita un token**, no lo copies a mano: `person/` ya lo resuelve y explica
> cómo consumirlo desde la tuya → [`person/USAR-EL-TOKEN.md`](person/USAR-EL-TOKEN.md)
