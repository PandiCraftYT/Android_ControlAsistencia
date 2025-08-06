package com.example.controlasistencias.Modelos;
import com.google.gson.annotations.SerializedName;
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
    private String fecha; // formato esperado: "2025-06-09"

    public int getProfesorId() {
        return profesor_id; // ← este es el correcto
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
        this.fecha = fecha;
        this.profesor_id = profesor_id;
        this.cuenta_empleado = cuenta_empleado;
        this.hora_registro = hora_registro;
    }

    // Getters (opcional si no los usas)
}