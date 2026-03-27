package com.example.controlasistencias;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlasistencias.Api.ApiService;
import com.example.controlasistencias.Api.RetrofitClient;
import com.example.controlasistencias.Modelos.GruposActivity;
import com.example.controlasistencias.Modelos.ZonaAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    private Map<String, String> contraseñasPorZona;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        zonasRecyclerView = findViewById(R.id.zonasRecyclerView);
        relojHora = findViewById(R.id.relojHora);

        zonasRecyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columnas

        iniciarRelojEnVivo();
        inicializarContraseñas();

        Log.d(TAG, "Iniciando solicitud a la API para obtener zonas.");

        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);

        apiService.getZonas().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> zonas = response.body();

                    zonaAdapter = new ZonaAdapter(MainActivity.this, zonas, zonaSeleccionada -> {
                        solicitarContraseña(zonaSeleccionada);
                    });

                    zonasRecyclerView.setAdapter(zonaAdapter);
                } else {
                    Toast.makeText(MainActivity.this, "Error en la respuesta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Fallo en conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void iniciarRelojEnVivo() {
        runnable = new Runnable() {
            @Override
            public void run() {
                String horaActual = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                relojHora.setText(horaActual);
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

        new MaterialAlertDialogBuilder(this)
                .setTitle("Acceso restringido")
                .setMessage("Ingresa la contraseña para: " + zonaSeleccionada)
                .setView(view)
                .setPositiveButton("Ingresar", (dialog, which) -> {
                    String contraseñaIngresada = input.getText().toString().trim();
                    if (verificarContraseña(zonaSeleccionada, contraseñaIngresada)) {
                        int zonaId = obtenerIdZonaDesdeNombre(zonaSeleccionada);
                        if (zonaId != -1) {
                            Intent intent = new Intent(MainActivity.this, GruposActivity.class);
                            intent.putExtra("zonaNombre", zonaSeleccionada);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Zona no reconocida.", Toast.LENGTH_SHORT).show();
                        }
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

    private int obtenerIdZonaDesdeNombre(String zonaNombre) {
        switch (zonaNombre.toLowerCase()) {
            case "departamentos": return 1;
            case "edificio 1": return 2;
            case "edificio 2": return 3;
            case "edificio 3": return 4;
            case "edificio 4": return 5;
            case "sotano": return 6;
            default: return -1;
        }
    }
}