package com.example.controlasistencias.ui.activities;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlasistencias.Api.ApiService;
import com.example.controlasistencias.Api.RetrofitClient;
import com.example.controlasistencias.models.Grupo;
import com.example.controlasistencias.ui.adapters.GrupoAdapter;
import com.example.controlasistencias.R;
import com.example.controlasistencias.Utils.AppUtils;
import com.example.controlasistencias.Utils.DialogUtils;

import java.util.List;

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
    private String zonaNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupos);

        encabezadoGrupos = findViewById(R.id.encabezadoGrupos);
        relojHora = findViewById(R.id.relojHora);
        recyclerGrupos = findViewById(R.id.recyclerGrupos);
        recyclerGrupos.setLayoutManager(new LinearLayoutManager(this));

        iniciarReloj();

        zonaNombre = getIntent().getStringExtra("zonaNombre");

        if (zonaNombre != null) {
            encabezadoGrupos.setText("Grupos del " + zonaNombre);
            obtenerGruposPorZona();
        } else {
            Toast.makeText(this, "No se recibió el nombre de la zona", Toast.LENGTH_LONG).show();
        }
    }

    private void obtenerGruposPorZona() {
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        apiService.getGruposPorZona(zonaNombre).enqueue(new Callback<List<Grupo>>() {
            @Override
            public void onResponse(Call<List<Grupo>> call, Response<List<Grupo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Grupo> grupos = response.body();
                    if (!grupos.isEmpty()) {
                        grupoAdapter = new GrupoAdapter(GruposActivity.this, grupos, zonaNombre);
                        recyclerGrupos.setAdapter(grupoAdapter);
                    } else {
                        Toast.makeText(GruposActivity.this, "No hay grupos en esta zona", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    DialogUtils.mostrarErrorConexion(GruposActivity.this, () -> obtenerGruposPorZona());
                }
            }

            @Override
            public void onFailure(Call<List<Grupo>> call, Throwable t) {
                DialogUtils.mostrarErrorConexion(GruposActivity.this, () -> obtenerGruposPorZona());
            }
        });
    }

    private void iniciarReloj() {
        relojRunnable = new Runnable() {
            @Override
            public void run() {
                relojHora.setText(AppUtils.getHoraActualMazatlan());
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
