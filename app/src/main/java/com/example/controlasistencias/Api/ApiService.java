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
    Call<List<String>> getZonas(); // Regresamos a String

    // 2. Obtener los horarios por zona (Quitamos 'api/' porque ya está en la BASE_URL)
    @GET("horarios/{zona}")
    Call<List<Horario>> getHorariosPorZona(@Path("zona") String zona);

    @GET("grupos/{zonaId}")
    Call<List<Grupo>> getGruposPorZona(@Path("zonaId") int zonaId);

    // 3. Registro de asistencia (Quitamos la barra inicial y el 'api/')
    @POST("asistencias/registrar")
    Call<Void> registrarAsistencia(@Body Asistencia asistencia);

    @GET("jefesgrupo/porGrupo/{grupoId}")
    Call<List<String>> getJefesGrupoPorGrupo(@Path("grupoId") int grupoId);

    @GET("asistencias/porGrupo/{grupoId}")
    Call<List<Asistencia>> getAsistenciasPorGrupo(@Path("grupoId") int grupoId);

    // Rutas dinámicas (Estas se quedan igual porque reciben la URL completa)
    @GET
    Call<List<Grupo>> getGruposPorZona(@Url String url);

    @GET
    Call<List<Profesor>> getProfesoresPorGrupo(@Url String url);
}
