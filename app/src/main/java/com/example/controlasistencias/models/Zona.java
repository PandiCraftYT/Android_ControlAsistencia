package com.example.controlasistencias.models;

public class Zona {
    private int id;
    private String nombre;

    public Zona(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
}
