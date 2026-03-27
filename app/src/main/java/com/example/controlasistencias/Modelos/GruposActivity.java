package com.example.controlasistencias.Modelos;

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
import com.example.controlasistencias.R;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
            // Sin codificar, para que Retrofit lo maneje
            String fullUrl = "https://preparatoria.charlystudio.org/android/grupos/porZona/" + zonaNombre;
            Log.d("API_GRUPOS", "🚀 Buscando grupos en: " + fullUrl);

            ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
            Call<List<Grupo>> call = apiService.getGruposPorZona(fullUrl);

            call.enqueue(new Callback<List<Grupo>>() {
                @Override
                public void onResponse(Call<List<Grupo>> call, Response<List<Grupo>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Grupo> grupos = response.body();
                        Log.d("API_GRUPOS", "✅ Éxito! Recibí " + grupos.size() + " grupos.");

                        if (!grupos.isEmpty()) {
                            // Imprimimos el primer grupo para confirmar que no vienen vacíos
                            Log.d("API_GRUPOS", "📌 Primer grupo: " + grupos.get(0).getGradoGrupo());

                            grupoAdapter = new GrupoAdapter(GruposActivity.this, grupos, zonaNombre);
                            recyclerGrupos.setAdapter(grupoAdapter);
                        } else {
                            Log.w("API_GRUPOS", "⚠️ La base de datos no tiene grupos para esta zona.");
                            Toast.makeText(GruposActivity.this, "No hay grupos en esta zona", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("API_GRUPOS", "❌ Error del servidor: " + response.code());
                        Toast.makeText(GruposActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Grupo>> call, Throwable t) {
                    Log.e("API_GRUPOS", "💥 Fallo catastrófico: " + t.getMessage());
                    Toast.makeText(GruposActivity.this, "Fallo: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            Log.e("API_GRUPOS", "💥 Excepción: " + e.getMessage());
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
