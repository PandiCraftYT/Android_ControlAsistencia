package com.example.controlasistencias.Modelos;

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

    public int getHorarioId() {
        return horarioId;
    }

    public Profesor() {
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }
}
