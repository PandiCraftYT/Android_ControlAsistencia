package com.example.controlasistencias.Utils;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AppUtils {

    public static String normalizarTexto(String texto) {
        if (texto == null) return "";
        String string = Normalizer.normalize(texto, Normalizer.Form.NFD);
        string = string.replaceAll("[^\\p{ASCII}]", "");
        return string.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    public static String normalizarHora(String hora) {
        if (hora == null || hora.isEmpty()) return "";
        if (hora.contains(":")) {
            String[] p = hora.split(":");
            if (p.length >= 2) return p[0] + ":" + p[1];
        }
        return hora;
    }

    public static String normalizarFormatoFecha(String fecha) {
        if (fecha == null) return null;
        fecha = fecha.split(" ")[0].split("T")[0];
        if (fecha.contains("/")) {
            String[] p = fecha.split("/");
            if (p.length == 3) {
                if (p[0].length() == 4) return p[0] + "-" + p[1] + "-" + p[2];
                return p[2] + "-" + p[1] + "-" + p[0];
            }
        }
        return fecha;
    }

    public static String obtenerDiaActual() {
        String[] dias = {"domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado"};
        return dias[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1];
    }

    public static String getFechaHoyMazatlan() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("America/Mazatlan"));
        return sdf.format(new Date());
    }
    
    public static String getHoraActualMazatlan() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("America/Mazatlan"));
        return sdf.format(new Date());
    }
}
