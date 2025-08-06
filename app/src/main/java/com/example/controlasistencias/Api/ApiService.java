package com.example.controlasistencias.Api;

import com.example.controlasistencias.Modelos.Asistencia;
import com.example.controlasistencias.Modelos.Grupo;
import com.example.controlasistencias.Modelos.Horario;
import com.example.controlasistencias.Modelos.Profesor;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface ApiService {

    // Obtener las zonas
    @GET("zonas")
    Call<List<String>> getZonas();


    // Obtener los horarios por zona
    @GET("api/horarios/{zona}")
    Call<List<Horario>> getHorariosPorZona(@Path("zona") String zona);
    @GET("grupos/{zonaId}")
    Call<List<Grupo>> getGruposPorZona(@Path("zonaId") int zonaId);

    @GET
    Call<List<Grupo>> getGruposPorZona(@Url String url);

    @GET
    Call<List<Profesor>> getProfesoresPorGrupo(@Url String url);

    @POST("/api/asistencias/registrar")
    Call<Void> registrarAsistencia(@Body Asistencia asistencia);

    @GET("jefesgrupo/porGrupo/{grupoId}")
    Call<List<String>> getJefesGrupoPorGrupo(@Path("grupoId") int grupoId);

    @GET("api/asistencias/porGrupo/{grupoId}")
    Call<List<Asistencia>> getAsistenciasPorGrupo(@Path("grupoId") int grupoId);




}
