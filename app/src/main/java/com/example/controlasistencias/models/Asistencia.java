package com.example.controlasistencias.models;

import com.google.gson.annotations.SerializedName;

public class Asistencia {
    
    @SerializedName("horario_id")
    private int horarioId;
    
    private String tipo;
    
    @SerializedName("firma_maestro")
    private String firmaMaestro;
    
    @SerializedName("firma_jefe")
    private String firmaJefe;
    
    private String observaciones;
    
    @SerializedName("profesor_id")
    private int profesorId;
    
    @SerializedName("numero_cuenta")
    private String numeroCuenta;
    
    private String fecha;

    // Campos adicionales que vienen en el JSON de consulta de asistencias
    @SerializedName("nombre_profesor")
    private String nombreProfesor;

    @SerializedName("hora_inicio")
    private String horaInicio;

    public Asistencia(int horarioId, String tipo, String firmaMaestro, String firmaJefe, String observaciones, int profesorId, String numeroCuenta, String fecha) {
        this.horarioId = horarioId;
        this.tipo = tipo;
        this.firmaMaestro = firmaMaestro;
        this.firmaJefe = firmaJefe;
        this.observaciones = observaciones;
        this.profesorId = profesorId;
        this.numeroCuenta = numeroCuenta;
        this.fecha = fecha;
    }

    public int getHorarioId() { return horarioId; }
    public String getTipo() { return tipo; }
    public String getFecha() { return fecha; }

    // Métodos actualizados para el bloqueo de UI
    public String getNombreIdentificador() {
        return nombreProfesor != null ? nombreProfesor : "";
    }

    public String getHoraInicioIdentificador() {
        return horaInicio != null ? horaInicio : "";
    }
}
