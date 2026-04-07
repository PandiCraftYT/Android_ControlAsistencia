package com.example.controlasistencias.models.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "asistencias_pendientes")
public class AsistenciaPendiente {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int horarioId;
    public String tipo;
    public String firmaMaestro;
    public String firmaJefe;
    public String observaciones;
    public int profesorId;
    public String numeroCuenta;
    public String fecha;

    public AsistenciaPendiente(int horarioId, String tipo, String firmaMaestro, String firmaJefe, String observaciones, int profesorId, String numeroCuenta, String fecha) {
        this.horarioId = horarioId;
        this.tipo = tipo;
        this.firmaMaestro = firmaMaestro;
        this.firmaJefe = firmaJefe;
        this.observaciones = observaciones;
        this.profesorId = profesorId;
        this.numeroCuenta = numeroCuenta;
        this.fecha = fecha;
    }
}
