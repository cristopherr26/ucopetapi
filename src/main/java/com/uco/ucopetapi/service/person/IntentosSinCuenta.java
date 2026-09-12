package com.uco.ucopetapi.service.person;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class IntentosSinCuenta {

    private static final int MAXIMO_CORREOS = 10_000;

    private final Map<String, Registro> porCorreo = new ConcurrentHashMap<>();

    public Instant bloqueadoHasta(String correo) {
        Registro registro = porCorreo.get(normalizar(correo));
        if (registro == null || registro.hasta == null || !registro.hasta.isAfter(Instant.now())) {
            return null;
        }
        return registro.hasta;
    }

    public void registrarFallo(String correo, int intentosParaBloquear,
                               int minutosPrimerBloqueo, int minutosSegundoBloqueo) {
        String clave = normalizar(correo);
        if (porCorreo.size() >= MAXIMO_CORREOS && !porCorreo.containsKey(clave)) {
            purgarVencidos();
            if (porCorreo.size() >= MAXIMO_CORREOS) {
                return;
            }
        }
        porCorreo.compute(clave, (_, previo) -> {
            int fallos = (previo == null ? 0 : previo.fallos) + 1;
            Instant hasta = null;
            if (fallos >= intentosParaBloquear * 2) {
                hasta = Instant.now().plus(minutosSegundoBloqueo, ChronoUnit.MINUTES);
            } else if (fallos >= intentosParaBloquear) {
                hasta = Instant.now().plus(minutosPrimerBloqueo, ChronoUnit.MINUTES);
            }
            return new Registro(fallos, hasta);
        });
    }

    private void purgarVencidos() {
        Instant ahora = Instant.now();
        porCorreo.values().removeIf(r -> r.hasta != null && !r.hasta.isAfter(ahora));
    }

    private static String normalizar(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase();
    }

    private record Registro(int fallos, Instant hasta) {
    }
}
