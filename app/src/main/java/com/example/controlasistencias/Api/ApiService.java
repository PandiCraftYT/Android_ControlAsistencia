package com.example.controlasistencias.Api;

import com.example.controlasistencias.Modelos.Horario;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {

    // Obtener las zonas
    @GET("api/zonas")
    Call<List<String>> getZonas();

    // Obtener los horarios por zona
    @GET("api/horarios/{zona}")
    Call<List<Horario>> getHorariosPorZona(@Path("zona") String zona);
}
