package com.example.controlasistencias.Api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://preparatoria.charlystudio.org/android/";
    private static Retrofit retrofit;

    public static Retrofit getInstance() {
        if (retrofit == null) {
            // Creamos un interceptor para ver las peticiones en el Logcat (depuración)
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Configuramos el cliente OkHttp con tiempos de espera (timeouts)
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS) // Tiempo para conectar
                    .readTimeout(30, TimeUnit.SECONDS)    // Tiempo para recibir datos
                    .writeTimeout(30, TimeUnit.SECONDS)   // Tiempo para enviar datos
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client) // Asignamos el cliente configurado
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
