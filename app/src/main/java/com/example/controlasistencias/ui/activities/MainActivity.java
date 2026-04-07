package com.example.controlasistencias.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlasistencias.Api.ApiService;
import com.example.controlasistencias.Api.RetrofitClient;
import com.example.controlasistencias.R;
import com.example.controlasistencias.ui.adapters.ZonaAdapter;
import com.example.controlasistencias.Utils.AppUtils;
import com.example.controlasistencias.Utils.DialogUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView zonasRecyclerView;
    private ZonaAdapter zonaAdapter;
    private TextView relojHora;
    private ProgressBar progressBar;
    private Handler handler = new Handler();
    private Runnable runnable;
    private static final String TAG = "MainActivity";

    private Map<String, String> contraseñasPorZona;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        zonasRecyclerView = findViewById(R.id.zonasRecyclerView);
        relojHora = findViewById(R.id.relojHora);
        progressBar = findViewById(R.id.progressBar);

        // Cargamos el número de columnas desde los recursos (dinámico para tabletas)
        int columns = getResources().getInteger(R.integer.zona_columns);
        zonasRecyclerView.setLayoutManager(new GridLayoutManager(this, columns));

        iniciarRelojEnVivo();
        inicializarContraseñas();
        obtenerZonas();
    }

    private void obtenerZonas() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        apiService.getZonas().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<String> zonas = response.body();
                    zonaAdapter = new ZonaAdapter(MainActivity.this, zonas, zonaSeleccionada -> {
                        solicitarContraseña(zonaSeleccionada);
                    });
                    zonasRecyclerView.setAdapter(zonaAdapter);
                } else {
                    DialogUtils.mostrarErrorConexion(MainActivity.this, () -> obtenerZonas());
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                DialogUtils.mostrarErrorConexion(MainActivity.this, () -> obtenerZonas());
            }
        });
    }

    private void iniciarRelojEnVivo() {
        runnable = new Runnable() {
            @Override
            public void run() {
                relojHora.setText(AppUtils.getHoraActualMazatlan());
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    private void inicializarContraseñas() {
        contraseñasPorZona = new HashMap<>();
        contraseñasPorZona.put("Departamentos", "1234");
        contraseñasPorZona.put("Edificio 1", "abcd");
        contraseñasPorZona.put("Edificio 2", "2222");
        contraseñasPorZona.put("Edificio 3", "3333");
        contraseñasPorZona.put("Edificio 4", "4444");
        contraseñasPorZona.put("Sotano", "5555");
    }

    private void solicitarContraseña(String zonaSeleccionada) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_password, null);
        TextInputEditText input = view.findViewById(R.id.editTextPassword);

        new MaterialAlertDialogBuilder(this, R.style.UASDialogTheme)
                .setTitle("Acceso restringido")
                .setMessage("Ingresa la contraseña para: " + zonaSeleccionada)
                .setView(view)
                .setPositiveButton("Ingresar", (dialog, which) -> {
                    String contraseñaIngresada = input.getText().toString().trim();
                    if (verificarContraseña(zonaSeleccionada, contraseñaIngresada)) {
                        Intent intent = new Intent(MainActivity.this, GruposActivity.class);
                        intent.putExtra("zonaNombre", zonaSeleccionada);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private boolean verificarContraseña(String zona, String contraseñaIngresada) {
        String contraseñaCorrecta = contraseñasPorZona.get(zona);
        return contraseñaCorrecta != null && contraseñaCorrecta.equals(contraseñaIngresada);
    }
}
