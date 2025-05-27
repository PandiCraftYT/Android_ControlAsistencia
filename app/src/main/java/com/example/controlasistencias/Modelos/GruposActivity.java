package com.example.controlasistencias;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlasistencias.Api.ApiService;
import com.example.controlasistencias.Api.RetrofitClient;
import com.example.controlasistencias.Modelos.Grupo;
import com.example.controlasistencias.Modelos.GrupoAdapter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GruposActivity extends AppCompatActivity {

    private TextView encabezadoGrupos;
    private TextView relojHora;
    private RecyclerView recyclerGrupos;
    private GrupoAdapter grupoAdapter;
    private Handler handler = new Handler();
    private Runnable relojRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupos);

        encabezadoGrupos = findViewById(R.id.encabezadoGrupos);
        relojHora = findViewById(R.id.relojHora);
        recyclerGrupos = findViewById(R.id.recyclerGrupos);
        recyclerGrupos.setLayoutManager(new LinearLayoutManager(this));

        iniciarReloj();

        String zonaNombre = getIntent().getStringExtra("zonaNombre");

        if (zonaNombre != null) {
            encabezadoGrupos.setText("Grupos del " + zonaNombre);
            obtenerGruposPorZona(zonaNombre);
        } else {
            Toast.makeText(this, "No se recibió el nombre de la zona", Toast.LENGTH_LONG).show();
        }
    }

    private void obtenerGruposPorZona(String zonaNombre) {
        try {
            // Codifica el nombre de la zona (espacios, tildes, etc.)
            String zonaCodificada = URLEncoder.encode(zonaNombre, StandardCharsets.UTF_8.toString());
            String fullUrl = "http://192.168.100.4:8080/api/grupos/porZona/" + zonaCodificada;

            Log.d("URL_DEBUG", "Llamando a: " + fullUrl);  // VERIFICA en Logcat

            ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
            Call<List<Grupo>> call = apiService.getGruposPorZona(fullUrl);

            call.enqueue(new Callback<List<Grupo>>() {
                @Override
                public void onResponse(Call<List<Grupo>> call, Response<List<Grupo>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Grupo> grupos = response.body();
                        if (!grupos.isEmpty()) {
                            grupoAdapter = new GrupoAdapter(GruposActivity.this, grupos);
                            recyclerGrupos.setAdapter(grupoAdapter);
                        } else {
                            Toast.makeText(GruposActivity.this, "No hay grupos en esta zona", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("ERROR_HTTP", "Código: " + response.code());
                        Toast.makeText(GruposActivity.this, "Error del servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Grupo>> call, Throwable t) {
                    Toast.makeText(GruposActivity.this, "Fallo de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error al codificar zona: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void iniciarReloj() {
        relojRunnable = new Runnable() {
            @Override
            public void run() {
                String horaActual = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                relojHora.setText(horaActual);
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(relojRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(relojRunnable);
    }
}
