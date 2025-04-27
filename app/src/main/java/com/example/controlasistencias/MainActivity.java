package com.example.controlasistencias;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.controlasistencias.Api.ApiService;
import com.example.controlasistencias.Api.RetrofitClient;
import com.example.controlasistencias.R;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private LinearLayout zonasLayout;

    // TAG para loguear en Logcat
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        zonasLayout = findViewById(R.id.zonasLayout);

        // Obtener zonas desde la API
        Log.d(TAG, "Iniciando solicitud a la API para obtener zonas.");

        RetrofitClient.getInstance().create(ApiService.class).getZonas().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                // Log para verificar la respuesta HTTP
                Log.d(TAG, "Código de respuesta de la API: " + response.code());

                if (response.isSuccessful()) {
                    List<String> zonas = response.body();

                    // Log para verificar el cuerpo de la respuesta
                    Log.d(TAG, "Respuesta exitosa: " + zonas);

                    // Crear botones para cada zona
                    if (zonas != null) {
                        for (String zona : zonas) {
                            Button button = new Button(MainActivity.this);
                            button.setText(zona);
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Redirigir a la actividad de horarios filtrados por zona
                                    Log.d(TAG, "Zona seleccionada: " + zona);
                                    Intent intent = new Intent(MainActivity.this, HorariosActivity.class);
                                    intent.putExtra("zona", zona);
                                    startActivity(intent);
                                }
                            });
                            zonasLayout.addView(button);
                        }
                    } else {
                        Log.d(TAG, "La respuesta está vacía (zonas es null).");
                    }
                } else {
                    // Log para cuando la respuesta no es exitosa
                    Log.e(TAG, "Error al obtener zonas: Código de respuesta no exitoso: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                // Log para el error de conexión o fallo en la solicitud
                Log.e(TAG, "Error al obtener zonas: " + t.getMessage());
            }
        });
    }
}
