package com.example.controlasistencias.Api;

import com.example.controlasistencias.Modelos.Asistencia;
import com.example.controlasistencias.Modelos.Grupo;
import com.example.controlasistencias.Modelos.Horario;
import com.example.controlasistencias.Modelos.Profesor;
import com.example.controlasistencias.Modelos.Zona;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface ApiService {
    @GET("zonas")
    Call<List<String>> getZonas();

    @GET("horarios/{zona}")
    Call<List<Horario>> getHorariosPorZona(@Path("zona") String zona);

    @GET("grupos/{zonaId}")
    Call<List<Grupo>> getGruposPorZona(@Path("zonaId") int zonaId);

    @POST("asistencias/registrar")
    Call<Void> registrarAsistencia(@Body Asistencia asistencia);

    @GET("jefesgrupo/porGrupo/{grupoId}")
    Call<List<String>> getJefesGrupoPorGrupo(@Path("grupoId") int grupoId);

    // Ruta corregida para buscar asistencias
    @GET("asistencias/porGrupo/{grupoId}")
    Call<List<Asistencia>> getAsistenciasPorGrupo(@Path("grupoId") int grupoId);

    @GET
    Call<List<Grupo>> getGruposPorZona(@Url String url);

    @GET
    Call<List<Profesor>> getProfesoresPorGrupo(@Url String url);
}
