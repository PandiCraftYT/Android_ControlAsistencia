package com.example.controlasistencias.Api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // Asegúrate de que termine en / y llegue solo hasta /api/
// Quitamos api/ y ponemos android/
// BIEN: Termina en /android/ y no incluye la ruta específica
    private static final String BASE_URL = "https://preparatoria.charlystudio.org/android/";
    private static Retrofit retrofit;

    public static Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
