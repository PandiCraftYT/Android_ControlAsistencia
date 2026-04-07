package com.example.controlasistencias.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Profesor {
    private int id;
    private String nombre;

    @SerializedName("cuenta_empleado")
    @Expose
    private String numeroCuenta;

    @SerializedName("horario_id")
    private int horarioId;

    @SerializedName("zona")
    private String zona;

    @SerializedName("materia")
    private String materia;

    @SerializedName("lunes")
    private String lunes;

    @SerializedName("martes")
    private String martes;

    @SerializedName("miercoles")
    private String miercoles;

    @SerializedName("jueves")
    private String jueves;

    @SerializedName("viernes")
    private String viernes;

    @SerializedName("dia")
    private String dia;

    @SerializedName("hora_inicio")
    private String horaInicio;

    @SerializedName("hora_fin")
    private String horaFin;

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getNumeroCuenta() { return numeroCuenta; }
    public int getHorarioId() { return horarioId; }
    public String getZona() { return zona; }
    public String getMateria() { return materia; }
    public String getLunes() { return lunes; }
    public String getMartes() { return martes; }
    public String getMiercoles() { return miercoles; }
    public String getJueves() { return jueves; }
    public String getViernes() { return viernes; }
    public String getDia() { return dia; }
    public String getHoraInicio() { return horaInicio; }
    public String getHoraFin() { return horaFin; }
}
