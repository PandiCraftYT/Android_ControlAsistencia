package com.example.controlasistencias;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlasistencias.Modelos.Horario;

import java.util.Date;
import java.util.List;

public class HorariosAdapter extends RecyclerView.Adapter<HorariosAdapter.HorarioViewHolder> {

    private final List<Horario> horarios;
    private final String diaActual;
    private final Context context;

    // Estado interno para selección
    private int selectedPosition = -1;
    private String estadoActual = "";

    public HorariosAdapter(List<Horario> horarios, String diaActual, Context context) {
        this.horarios  = horarios;
        this.diaActual = diaActual;
        this.context   = context;
    }

    @NonNull
    @Override
    public HorarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_horario, parent, false);
        return new HorarioViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HorarioViewHolder holder, int position) {
        Horario h = horarios.get(position);

        // 1) Poblamos datos
        holder.txtNombre.setText(h.getNombre());
        holder.txtAsignatura.setText(h.getAsignatura());
        holder.txtGrupo.setText(h.getGrado_grupo());
        String hora = getHoraDia(h);
        holder.txtHorario.setText(hora != null ? hora : "No disponible");

        // 2) Reset UI antes de reutilizar
        holder.chkAsistencia.setOnCheckedChangeListener(null);
        holder.chkRetardo   .setOnCheckedChangeListener(null);
        holder.chkFalta     .setOnCheckedChangeListener(null);

        holder.chkAsistencia.setChecked(false);
        holder.chkRetardo   .setChecked(false);
        holder.chkFalta     .setChecked(false);

        holder.btnFirmarMaestro.setVisibility(View.GONE);
        holder.btnFirmarJefe   .setVisibility(View.GONE);
        holder.panelObservaciones.setVisibility(View.GONE);

        // 3) Si ésta es la fila seleccionada, muestro el estado
        if (position == selectedPosition) {
            switch (estadoActual) {
                case "asistencia":
                    holder.chkAsistencia.setChecked(true);
                    holder.btnFirmarMaestro.setVisibility(View.VISIBLE);
                    break;
                case "retardo":
                    holder.chkRetardo.setChecked(true);
                    holder.btnFirmarMaestro.setVisibility(View.VISIBLE);
                    holder.panelObservaciones.setVisibility(View.VISIBLE);
                    holder.txtTituloObservaciones.setText("Hora de firma:");
                    holder.editObservacion.setText(getHoraActual());
                    holder.editObservacion.setEnabled(false);
                    break;
                case "falta":
                    holder.chkFalta.setChecked(true);
                    holder.btnFirmarJefe.setVisibility(View.VISIBLE);
                    holder.panelObservaciones.setVisibility(View.VISIBLE);
                    holder.txtTituloObservaciones.setText("Observación y hora:");
                    holder.editObservacion.setText("Falta firmada a las " + getHoraActual());
                    holder.editObservacion.setEnabled(false);
                    break;
            }
        }

        // 4) Listeners de checkboxes: actualizan posición + estado
        holder.chkAsistencia.setOnClickListener(v -> {
            selectedPosition = position;
            estadoActual = "asistencia";
            notifyDataSetChanged();
        });
        holder.chkRetardo.setOnClickListener(v -> {
            selectedPosition = position;
            estadoActual = "retardo";
            notifyDataSetChanged();
        });
        holder.chkFalta.setOnClickListener(v -> {
            selectedPosition = position;
            estadoActual = "falta";
            notifyDataSetChanged();
        });

        // 5) Firmar Maestro
        holder.btnFirmarMaestro.setOnClickListener(v -> {
            // TODO: guardar asistencia/retardo en BD aquí
            selectedPosition = -1;
            notifyDataSetChanged();
        });

        // 6) Firmar Jefe
        holder.btnFirmarJefe.setOnClickListener(v -> {
            // TODO: guardar falta y firma jefe en BD aquí
            selectedPosition = -1;
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return horarios.size();
    }

    /** Devuelve la hora del día actual para este horario */
    private String getHoraDia(Horario h) {
        switch (diaActual.toLowerCase()) {
            case "lunes":     return h.getLunes();
            case "martes":    return h.getMartes();
            case "miércoles": return h.getMiercoles();
            case "jueves":    return h.getJueves();
            case "viernes":   return h.getViernes();
            default:          return null;
        }
    }

    /** Hora formateada HH:mm:ss */
    private String getHoraActual() {
        return DateFormat.format("HH:mm:ss", new Date()).toString();
    }

    // ===== Métodos para guardar / restaurar estado tras rotación =====

    /** @return posición del ítem seleccionado (-1 si ninguno) */
    public int getSelectedPosition() {
        return selectedPosition;
    }

    /** @return estado actual ("asistencia", "retardo", "falta" o "") */
    public String getEstadoActual() {
        return estadoActual;
    }

    /**
     * Restaura el ítem seleccionado y su estado.
     * Debe llamarse antes de asignar el adapter al RecyclerView.
     */
    public void setSelection(int position, String estado) {
        this.selectedPosition = position;
        this.estadoActual    = estado;
        notifyDataSetChanged();
    }

    /** ViewHolder interno */
    static class HorarioViewHolder extends RecyclerView.ViewHolder {
        TextView    txtNombre, txtAsignatura, txtGrupo, txtHorario, txtTituloObservaciones;
        CheckBox    chkAsistencia, chkRetardo, chkFalta;
        ImageButton btnFirmarMaestro, btnFirmarJefe;
        EditText    editObservacion;
        LinearLayout panelObservaciones;

        HorarioViewHolder(View itemView) {
            super(itemView);
            txtNombre   = itemView.findViewById(R.id.txtNombre);
            txtAsignatura = itemView.findViewById(R.id.txtAsignatura);
            txtGrupo    = itemView.findViewById(R.id.txtGrupo);
            txtHorario  = itemView.findViewById(R.id.txtHorario);

            chkAsistencia    = itemView.findViewById(R.id.btnAsistencia);
            chkRetardo       = itemView.findViewById(R.id.btnRetardo);
            chkFalta         = itemView.findViewById(R.id.btnFalta);

            btnFirmarMaestro = itemView.findViewById(R.id.btnFirmarMaestro);
            btnFirmarJefe    = itemView.findViewById(R.id.btnFirmarJefe);

            panelObservaciones     = itemView.findViewById(R.id.panelObservaciones);
            txtTituloObservaciones = itemView.findViewById(R.id.txtTituloObservaciones);
            editObservacion        = itemView.findViewById(R.id.editObservacion);
        }
    }
}
