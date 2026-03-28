package com.example.controlasistencias.Modelos;
import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Asistencia {

    @SerializedName("horario_id")
    private int horario_id;

    @SerializedName("estatus")
    private String estatus;

    @SerializedName("firma_maestro")
    private String firma_maestro;

    @SerializedName("firma_jefe_grupo")
    private String firma_jefe_grupo;

    @SerializedName("observacion")
    private String observacion;

    @SerializedName("profesor_id")
    private int profesor_id;

    @SerializedName("cuenta_empleado")
    private String cuenta_empleado;

    @SerializedName("hora_registro")
    private String hora_registro;

    @SerializedName("fecha")
    private String fecha;

    @SerializedName("nombre_profesor")
    private String nombreProfesor;

    @SerializedName("profesor") // Probable nombre en el JSON
    private String nombreProfesorAlt;

    @SerializedName("nombre")
    private String nombreAlternativo;

    @SerializedName("horario") // Campo que se ve en la web "22:20:00 - 23:20:00"
    private String horarioTexto;

    @SerializedName("hora_inicio")
    private String horaInicio;

    public int getProfesorId() { return profesor_id; }
    public int getHorarioId() { return horario_id; }
    public String getFecha() { return fecha; }
    
    public String getNombreIdentificador() {
        if (nombreProfesor != null) return nombreProfesor;
        if (nombreProfesorAlt != null) return nombreProfesorAlt;
        return nombreAlternativo;
    }
    
    public String getHoraInicioIdentificador() {
        if (horaInicio != null) return horaInicio;
        if (horarioTexto != null && horarioTexto.contains("-")) {
            return horarioTexto.split("-")[0].trim();
        }
        return null;
    }

    public Asistencia(int horario_id, String estatus, String firma_maestro,
                      String firma_jefe_grupo, String observacion,
                      int profesor_id, String cuenta_empleado, String hora_registro)
    {
        this.horario_id = horario_id;
        this.estatus = estatus;
        this.firma_maestro = firma_maestro;
        this.firma_jefe_grupo = firma_jefe_grupo;
        this.observacion = observacion;
        this.profesor_id = profesor_id;
        this.cuenta_empleado = cuenta_empleado;
        this.hora_registro = hora_registro;
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("America/Mazatlan"));
        this.fecha = sdf.format(new Date());
    }
}