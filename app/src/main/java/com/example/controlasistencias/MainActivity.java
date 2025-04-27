package com.example.controlasistencias;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlasistencias.Api.ApiService;
import com.example.controlasistencias.Api.RetrofitClient;
import com.example.controlasistencias.Modelos.ZonaAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView zonasRecyclerView;
    private ZonaAdapter zonaAdapter;
    private TextView relojHora;
    private Handler handler = new Handler();
    private Runnable runnable;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        zonasRecyclerView = findViewById(R.id.zonasRecyclerView);
        relojHora = findViewById(R.id.relojHora);

        zonasRecyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columnas

        iniciarRelojEnVivo();

        Log.d(TAG, "Iniciando solicitud a la API para obtener zonas.");

        RetrofitClient.getInstance().create(ApiService.class).getZonas().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                Log.d(TAG, "Código de respuesta de la API: " + response.code());

                if (response.isSuccessful()) {
                    List<String> zonas = response.body();
                    Log.d(TAG, "Respuesta exitosa: " + zonas);

                    if (zonas != null) {
                        zonaAdapter = new ZonaAdapter(MainActivity.this, zonas);
                        zonasRecyclerView.setAdapter(zonaAdapter);
                    } else {
                        Log.d(TAG, "La respuesta está vacía (zonas es null).");
                    }
                } else {
                    Log.e(TAG, "Error al obtener zonas: Código de respuesta no exitoso: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Log.e(TAG, "Error al obtener zonas: " + t.getMessage());
            }
        });
    }

    private void iniciarRelojEnVivo() {
        runnable = new Runnable() {
            @Override
            public void run() {
                String horaActual = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                relojHora.setText(horaActual);
                handler.postDelayed(this, 1000); // actualizar cada segundo
            }
        };
        handler.post(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}
