# Prueba de rendimiento con JMeter — Módulo Compras

Prueba de carga sobre `GET /api/v1/purchases` (listado de compras) de `ucopetapi`,
corriendo en local. No requiere JMeter instalado como programa: solo se descomprime.

## 1. Instalar JMeter (si no lo tienes)

1. Descarga el binario desde https://jmeter.apache.org/download_jmeter.cgi
   (archivo `.zip`, por ejemplo `apache-jmeter-5.6.3.zip`).
2. Descomprímelo donde quieras, por ejemplo en `C:\apache-jmeter-5.6.3`.
3. Confirma que tienes Java: en PowerShell, `java -version` (JMeter necesita Java 8+).
4. Verifica JMeter: `C:\apache-jmeter-5.6.3\bin\jmeter.bat --version`.

## 2. Antes de correr la prueba

Con `ucopetapi`, Postgres y RabbitMQ corriendo localmente:

1. Abre `compras-load-test.jmx` con un editor de texto (o con la GUI de JMeter:
   `jmeter.bat` sin argumentos) y busca el elemento **"Variables de la prueba"**.
2. Cambia estos tres valores:
   - `ADMIN_EMAIL` / `ADMIN_PASSWORD`: un usuario con rol ADMIN de tu base de datos local.
   - `HEADQUARTER_ID`: el UUID de una sede existente (la ves en la tabla `headquarter`
     o en el selector "Trabajar en" del frontend, inspeccionando la llamada a
     `/api/v1/headquarter` desde las herramientas de desarrollador del navegador).
3. Si tu API corre en otro puerto, ajusta también `HOST`/`PORT` (por defecto
   `localhost:8080`).
4. Opcional: ajusta `THREADS` (usuarios concurrentes), `RAMPUP` (segundos para
   arrancarlos todos) y `LOOPS` (peticiones por usuario). Por defecto: 20 hilos,
   rampa de 5s, 20 iteraciones cada uno → 400 peticiones en total.

## 3. Correr la prueba (modo no-GUI, recomendado para resultados reales)

Desde la carpeta `jmeter/` de este repo, en PowerShell o CMD:

```
C:\apache-jmeter-5.6.3\bin\jmeter.bat -n -t compras-load-test.jmx -l resultados.jtl -e -o reporte
```

- `-n`: corre sin interfaz gráfica (así deben hacerse las pruebas de carga reales).
- `-l resultados.jtl`: guarda cada petición (crudo).
- `-e -o reporte`: genera un dashboard HTML en la carpeta `reporte/` con gráficas,
  percentiles, throughput, etc. — esto es lo que presentas como evidencia.

Al terminar, abre `reporte/index.html` en el navegador. Ahí están los indicadores
clave para la sustentación: tiempo de respuesta promedio/percentiles (p90/p95),
throughput (peticiones/seg) y porcentaje de errores.

## 4. Si algo falla

- Si todas las peticiones de "Login" fallan (401/400): revisa `ADMIN_EMAIL`/`ADMIN_PASSWORD`.
- Si "Listar compras" falla con 400: revisa que `HEADQUARTER_ID` sea un UUID real.
- Si todo falla con "Connection refused": `ucopetapi` no está corriendo o no está en
  el puerto configurado en `HOST`/`PORT`.
