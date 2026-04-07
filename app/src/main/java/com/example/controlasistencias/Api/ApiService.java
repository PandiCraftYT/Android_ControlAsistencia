package com.example.controlasistencias.Api;

import com.example.controlasistencias.models.Asistencia;
import com.example.controlasistencias.models.Grupo;
import com.example.controlasistencias.models.Horario;
import com.example.controlasistencias.models.Profesor;

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

    @GET("grupos/porZona/{zona}")
    Call<List<Grupo>> getGruposPorZona(@Path("zona") String zona);

    @POST("asistencias/registrar")
    Call<Void> registrarAsistencia(@Body Asistencia asistencia);

    @GET("jefesgrupo/porGrupo/{grupoId}")
    Call<List<String>> getJefesGrupoPorGrupo(@Path("grupoId") int grupoId);

    @GET("asistencias/porGrupo/{grupoId}")
    Call<List<Asistencia>> getAsistenciasPorGrupo(@Path("grupoId") int grupoId);

    @GET("profesores/porGrupo/{grupoId}")
    Call<List<Profesor>> getProfesoresPorGrupo(@Path("grupoId") int grupoId);

    @GET
    Call<List<Grupo>> getGruposPorZonaUrl(@Url String url);

    @GET
    Call<List<Profesor>> getProfesoresPorGrupoUrl(@Url String url);
}
