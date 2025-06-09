package com.example.controlasistencias.Modelos;

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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlasistencias.R;

import java.util.List;

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

    public ProfesorAdapter(Context context, List<Profesor> listaProfesores, ProfesorSeleccionado listener) {
        this.context = context;
        this.listaProfesores = listaProfesores;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfesorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_profesor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfesorAdapter.ViewHolder holder, int position) {
        Profesor profesor = listaProfesores.get(position);
        holder.textNombre.setText(profesor.getNombre());

        // Reset inicial
        holder.radioGroup.clearCheck();
        holder.editObservaciones.setText("");
        holder.editObservaciones.setEnabled(false);
        holder.btnScan.setEnabled(false);
        holder.layoutExpandable.setVisibility(View.GONE);

        // Expandir/colapsar
        holder.itemView.setOnClickListener(v -> {
            holder.layoutExpandable.setVisibility(
                    holder.layoutExpandable.getVisibility() == View.GONE ? View.VISIBLE : View.GONE
            );
        });

        // Habilitar campos al seleccionar tipo
        holder.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            holder.btnScan.setEnabled(true);
            holder.editObservaciones.setEnabled(checkedId == R.id.radioFalta);
        });

        // 👉 ESCANEAR QR
        holder.btnScan.setOnClickListener(v -> {
            int checkedId = holder.radioGroup.getCheckedRadioButtonId();
            String tipoSeleccionado = null;

            if (checkedId == R.id.radioAsistencia) {
                tipoSeleccionado = "ASISTENCIA";
            } else if (checkedId == R.id.radioRetardo) {
                tipoSeleccionado = "RETARDO";
            } else if (checkedId == R.id.radioFalta) {
                tipoSeleccionado = "FALTA";
            }

            if (tipoSeleccionado != null) {
                Log.d("ADAPTER", "✔ Escaneo solicitado para: " + profesor.getNombre());
                listener.onScanRequested(profesor, tipoSeleccionado, holder.editObservaciones);
            } else {
                Toast.makeText(holder.itemView.getContext(), "⚠️ Selecciona un tipo de asistencia", Toast.LENGTH_SHORT).show();
            }
        });
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombre = itemView.findViewById(R.id.textNombre);
            radioGroup = itemView.findViewById(R.id.radioGroup);
            radioAsistencia = itemView.findViewById(R.id.radioAsistencia);
            radioRetardo = itemView.findViewById(R.id.radioRetardo);
            radioFalta = itemView.findViewById(R.id.radioFalta);
            editObservaciones = itemView.findViewById(R.id.editObservaciones);
            btnScan = itemView.findViewById(R.id.btnScan);
            layoutExpandable = itemView.findViewById(R.id.layoutExpandable); // 🔹 ID del contenedor oculto
        }
    }
}
