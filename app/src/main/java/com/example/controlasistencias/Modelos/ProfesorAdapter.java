package com.example.controlasistencias.Modelos;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlasistencias.R;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ProfesorAdapter extends RecyclerView.Adapter<ProfesorAdapter.ViewHolder> {

    private Context context;
    private List<Profesor> listaProfesores;
    private ProfesorSeleccionado listener;

    public interface ProfesorSeleccionado {
        void onScanRequested(Profesor profesor, String tipoAsistencia, EditText campoObservacion);
        void onQRResultado(Profesor profesor, String contenidoQR);
        String getNumeroCuenta();
        Profesor getProfesor();
    }
    private List<Integer> indicesActivos = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;
    private Set<Integer> profesoresEscaneados = new HashSet<>();

    public ProfesorAdapter(Context context, List<Profesor> listaProfesores, ProfesorSeleccionado listener) {
        this.context = context;
        this.listaProfesores = listaProfesores;
        this.listener = listener;
        actualizarIndicesActivos();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime ahora = LocalTime.now();

        for (int i = 0; i < listaProfesores.size(); i++) {
            Profesor p = listaProfesores.get(i);
            try {
                LocalTime inicio = LocalTime.parse(p.getHoraInicio(), formatter);
                LocalTime fin = LocalTime.parse(p.getHoraFin(), formatter);

                if (!ahora.isBefore(inicio) && !ahora.isAfter(fin)) {
                    indicesActivos.add(i); // esta clase está en curso
                }
            } catch (Exception e) {
                Log.e("ADAPTER", "Error al analizar horario: " + e.getMessage());
            }
        }

    }
    private void actualizarIndicesActivos() {
        indicesActivos.clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime ahora = LocalTime.now();

        for (int i = 0; i < listaProfesores.size(); i++) {
            Profesor p = listaProfesores.get(i);
            try {
                LocalTime inicio = LocalTime.parse(p.getHoraInicio(), formatter);
                LocalTime fin = LocalTime.parse(p.getHoraFin(), formatter);

                if (!ahora.isBefore(inicio) && !ahora.isAfter(fin)) {
                    indicesActivos.add(i);
                }
            } catch (Exception e) {
                Log.e("ADAPTER", "Error al analizar horario: " + e.getMessage());
            }
        }
    }
    public void iniciarActualizacionPeriodica() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                actualizarIndicesActivos(); // vuelve a calcular
                notifyDataSetChanged(); // actualiza UI
                handler.postDelayed(this, 60 * 1000); // cada minuto
            }
        };
        handler.post(refreshRunnable);
    }

    public void detenerActualizacion() {
        handler.removeCallbacks(refreshRunnable);
    }

    @NonNull
    @Override
    public ProfesorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_profesor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Profesor profesor = listaProfesores.get(position);

        holder.textNombre.setText(profesor.getNombre());
        holder.txtHorario.setText("Horario: " + profesor.getHoraInicio() + " - " + profesor.getHoraFin());
        holder.txtMateria.setText("Materia: " + profesor.getMateria());

        boolean estaActivo = indicesActivos.contains(position);

        if (estaActivo) {
            holder.itemView.setAlpha(1.0f);
            holder.itemView.setEnabled(true);

            holder.itemView.setOnClickListener(v -> {
                if (profesoresEscaneados.contains(profesor.getId())) {
                    // Validar si todavía está en rango de horario
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                        LocalTime ahora = LocalTime.now();
                        LocalTime inicio = LocalTime.parse(profesor.getHoraInicio(), formatter);
                        LocalTime fin = LocalTime.parse(profesor.getHoraFin(), formatter);

                        if (!ahora.isBefore(inicio) && !ahora.isAfter(fin)) {
                            // Mostrar diálogo si está en horario
                            new AlertDialog.Builder(holder.itemView.getContext())
                                    .setTitle("¿Qué pasó?")
                                    .setMessage("Este profesor ya fue registrado. Si necesitas ayuda, acude al departamento de ASISTENCIA")
                                    .show();
                        }

                    } catch (Exception e) {
                        Log.e("ADAPTER", "Error validando horario reentrada: " + e.getMessage());
                    }

                } else {
                    // Normal: expandir
                    holder.layoutExpandable.setVisibility(
                            holder.layoutExpandable.getVisibility() == View.GONE ? View.VISIBLE : View.GONE
                    );
                }
            });


            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalTime ahora = LocalTime.now();

            try {
                LocalTime inicio = LocalTime.parse(profesor.getHoraInicio(), formatter);
                LocalTime fin = LocalTime.parse(profesor.getHoraFin(), formatter);
                long minutosPasados = Duration.between(inicio, ahora).toMinutes();

                // Mostrar todos
                holder.radioAsistencia.setVisibility(View.VISIBLE);
                holder.radioRetardo.setVisibility(View.VISIBLE);
                holder.radioFalta.setVisibility(View.VISIBLE);

                // Reset de botones
                holder.radioAsistencia.setEnabled(true);
                holder.radioRetardo.setEnabled(true);
                holder.radioFalta.setEnabled(true);
                holder.editObservaciones.setEnabled(false);
                holder.btnScan.setEnabled(true);

                boolean opcionesDisponibles = true;

                if (ahora.isBefore(inicio)) {
                    // Clase aún no comienza
                    holder.radioAsistencia.setEnabled(false);
                    holder.radioRetardo.setEnabled(false);
                    holder.radioFalta.setEnabled(false);
                    holder.btnScan.setEnabled(false);
                    opcionesDisponibles = false;
                } else if (!ahora.isAfter(fin)) {
                    // Clase en curso
                    if (minutosPasados <= 10) {
                        // Todo habilitado
                    } else if (minutosPasados <= 25) {
                        holder.radioAsistencia.setVisibility(View.GONE);
                        holder.radioAsistencia.setEnabled(false);

                        // Si estaba seleccionado, lo quitamos
                        if (holder.radioGroup.getCheckedRadioButtonId() == R.id.radioAsistencia) {
                            holder.radioGroup.clearCheck();
                            holder.btnScan.setEnabled(false);
                        }

                    } else {
                        // Solo falta
                        holder.radioAsistencia.setVisibility(View.GONE);
                        holder.radioRetardo.setVisibility(View.GONE);
                        holder.radioAsistencia.setEnabled(false);
                        holder.radioRetardo.setEnabled(false);
                        holder.editObservaciones.setEnabled(true); // Permitir observación

                        // Si estaban seleccionados otros, limpiar
                        int checkedId = holder.radioGroup.getCheckedRadioButtonId();
                        if (checkedId == R.id.radioAsistencia || checkedId == R.id.radioRetardo) {
                            holder.radioGroup.clearCheck();
                            holder.btnScan.setEnabled(false);
                        }
                    }
                } else {
                    // Clase terminó
                    holder.radioAsistencia.setEnabled(false);
                    holder.radioRetardo.setEnabled(false);
                    holder.radioFalta.setEnabled(false);
                    holder.btnScan.setEnabled(false);
                    opcionesDisponibles = false;
                }

            } catch (Exception e) {
                Log.e("ADAPTER", "Error parseando hora: " + e.getMessage());
            }

            // Escuchar cambios en los radios
            holder.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                boolean habilitarScan = false;

                if (checkedId == R.id.radioFalta) {
                    holder.editObservaciones.setEnabled(true);
                    habilitarScan = true;
                } else {
                    holder.editObservaciones.setEnabled(false);
                    if (checkedId != -1) habilitarScan = true;
                }

                holder.btnScan.setEnabled(habilitarScan);
            });

        } else {
            holder.itemView.setAlpha(0.5f);
            holder.itemView.setEnabled(false);
            holder.itemView.setOnClickListener(null);
            holder.layoutExpandable.setVisibility(View.GONE);
            holder.radioGroup.clearCheck();
            holder.editObservaciones.setEnabled(false);
            holder.editObservaciones.setText("");
            holder.btnScan.setEnabled(false);
        }

        // Escanear QR
        holder.btnScan.setOnClickListener(v -> {
            if (!estaActivo) return;

            int checkedId = holder.radioGroup.getCheckedRadioButtonId();
            String tipoSeleccionado = null;

            if (checkedId == R.id.radioAsistencia) tipoSeleccionado = "ASISTENCIA";
            else if (checkedId == R.id.radioRetardo) tipoSeleccionado = "RETARDO";
            else if (checkedId == R.id.radioFalta) tipoSeleccionado = "FALTA";

            if (tipoSeleccionado == null) {
                Toast.makeText(holder.itemView.getContext(), "⚠️ Selecciona un tipo de asistencia", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verificación de tiempo ANTES de enviar
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                LocalTime ahora = LocalTime.now();
                LocalTime inicio = LocalTime.parse(profesor.getHoraInicio(), formatter);
                LocalTime fin = LocalTime.parse(profesor.getHoraFin(), formatter);
                long minutosPasados = Duration.between(inicio, ahora).toMinutes();

                if (ahora.isBefore(inicio) || ahora.isAfter(fin)) {
                    Toast.makeText(holder.itemView.getContext(), "⛔ Fuera del horario de clase", Toast.LENGTH_SHORT).show();
                    return;
                }

                switch (tipoSeleccionado) {
                    case "ASISTENCIA":
                        if (minutosPasados > 10) {
                            Toast.makeText(holder.itemView.getContext(), "⏰ Tiempo de asistencia agotado", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        break;

                    case "RETARDO":
                        if (minutosPasados <= 10 || minutosPasados > 25) {
                            Toast.makeText(holder.itemView.getContext(), "⏰ Solo se permite retardo entre 11 y 25 minutos", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        break;

                    case "FALTA":
                        if (minutosPasados <= 25) {
                            Toast.makeText(holder.itemView.getContext(), "⏰ La falta solo se permite después de 25 minutos", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        break;
                }

            } catch (Exception e) {
                Log.e("ADAPTER", "Error en validación de tiempo al escanear: " + e.getMessage());
                Toast.makeText(holder.itemView.getContext(), "⚠️ Error al validar el horario", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Validación exitosa
            listener.onScanRequested(profesor, tipoSeleccionado, holder.editObservaciones);
        });
// ✅ Guardar como ya escaneado
        profesoresEscaneados.add(profesor.getId());

// ✅ Desactivar elementos visuales
        holder.btnScan.setEnabled(false);
        holder.itemView.setAlpha(0.5f);
        holder.radioGroup.setEnabled(false);
        holder.radioAsistencia.setEnabled(false);
        holder.radioRetardo.setEnabled(false);
        holder.radioFalta.setEnabled(false);
        holder.editObservaciones.setEnabled(false);

    }




    private String obtenerHorarioActual(Profesor profesor) {
        LocalTime ahora = LocalTime.now();

        Calendar calendario = Calendar.getInstance();
        int dia = calendario.get(Calendar.DAY_OF_WEEK);
        String horaActual = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendario.getTime());

        String bloques = "";
        switch (dia) {
            case Calendar.MONDAY: bloques = profesor.getLunes(); break;
            case Calendar.TUESDAY: bloques = profesor.getMartes(); break;
            case Calendar.WEDNESDAY: bloques = profesor.getMiercoles(); break;
            case Calendar.THURSDAY: bloques = profesor.getJueves(); break;
            case Calendar.FRIDAY: bloques = profesor.getViernes(); break;
        }

        Log.d("HORARIO_DEBUG", "📅 Día actual: " + dia + ", hora actual: " + horaActual);
        Log.d("HORARIO_DEBUG", "Bloques del día: \n" + bloques);

        if (bloques == null || bloques.isEmpty()) return "";

        String[] lineas = bloques.split("\n");
        for (String linea : lineas) {
            Log.d("HORARIO_DEBUG", "Analizando línea: " + linea);
            String[] partes = linea.trim().split(" ", 2);
            if (partes.length < 2) continue;

            String[] rango = partes[0].split("-");
            if (rango.length != 2) continue;

            String inicio = rango[0];
            String fin = rango[1];
            String materia = partes[1];

            Log.d("HORARIO_DEBUG", "Comparando: " + horaActual + " >= " + inicio + " && <= " + fin);

            if (horaActual.compareTo(inicio) >= 0 && horaActual.compareTo(fin) <= 0) {
                Log.d("HORARIO_DEBUG", "✅ Coincidencia encontrada: " + inicio + " a " + fin + " " + materia);
                return inicio + " a " + fin + " " + materia;
            }
        }

        Log.d("HORARIO_DEBUG", "❌ No se encontró coincidencia de horario");
        return "";
    }


    // Soporta tanto "HH:mm" como "HH:mm:ss"
    private LocalTime parseHoraFlexible(String hora) {
        try {
            if (hora.length() == 5) { // HH:mm
                return LocalTime.parse(hora);
            } else if (hora.length() == 8) { // HH:mm:ss
                return LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm:ss"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    @Override
    public int getItemCount() {
        return listaProfesores.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textNombre;
        RadioGroup radioGroup;
        RadioButton radioAsistencia, radioRetardo, radioFalta;
        EditText editObservaciones;
        View btnScan;
        LinearLayout layoutExpandable; // 🔹 Sección expandible
        TextView txtHorario;
        TextView txtMateria;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombre = itemView.findViewById(R.id.textNombre);
            radioGroup = itemView.findViewById(R.id.radioGroup);
            textNombre = itemView.findViewById(R.id.textNombre);
            radioAsistencia = itemView.findViewById(R.id.radioAsistencia);
            radioRetardo = itemView.findViewById(R.id.radioRetardo);
            radioFalta = itemView.findViewById(R.id.radioFalta);
            editObservaciones = itemView.findViewById(R.id.editObservaciones);
            btnScan = itemView.findViewById(R.id.btnScan);
            layoutExpandable = itemView.findViewById(R.id.layoutExpandable);
            txtHorario = itemView.findViewById(R.id.txtHorario);
            txtMateria = itemView.findViewById(R.id.txtMateria);
        }
    }
}
