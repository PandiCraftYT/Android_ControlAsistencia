package com.example.controlasistencias.models.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AsistenciaDao {
    @Insert
    void insertar(AsistenciaPendiente asistencia);

    @Query("SELECT * FROM asistencias_pendientes")
    List<AsistenciaPendiente> obtenerTodas();

    @Delete
    void eliminar(AsistenciaPendiente asistencia);
}
