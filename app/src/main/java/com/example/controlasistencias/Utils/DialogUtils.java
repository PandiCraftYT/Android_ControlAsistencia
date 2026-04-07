package com.example.controlasistencias.Utils;

import android.app.Activity;
import android.content.Context;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DialogUtils {

    public interface OnRetryListener {
        void onRetry();
    }

    public static void mostrarErrorConexion(Context context, OnRetryListener listener) {
        // Validación de seguridad: Si el contexto es una actividad, verificar que no esté finalizando
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) {
                return; // No intentar mostrar el diálogo si la pantalla ya no existe
            }
        }

        try {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Problema de conexión")
                    .setMessage("Parece que tienes problemas de conexión. Por favor, verifica tu internet e intenta de nuevo.")
                    .setCancelable(false)
                    .setPositiveButton("Reintentar", (dialog, which) -> {
                        if (listener != null) {
                            listener.onRetry();
                        }
                    })
                    .setNegativeButton("Cerrar", (dialog, which) -> dialog.dismiss())
                    .show();
        } catch (Exception e) {
            // Si por alguna razón falla el despliegue del diálogo, ignoramos para evitar el cierre forzado
            e.printStackTrace();
        }
    }
}
