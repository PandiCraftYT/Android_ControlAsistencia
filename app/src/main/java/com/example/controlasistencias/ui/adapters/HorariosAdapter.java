package com.example.controlasistencias.ui.adapters;

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

import com.example.controlasistencias.models.Horario;
import com.example.controlasistencias.R;

import java.util.Date;
import java.util.List;

public class HorariosAdapter extends RecyclerView.Adapter<HorariosAdapter.HorarioViewHolder> {

    private final List<Horario> horarios;
    private final String diaActual;
    private final Context context;

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

        holder.txtNombre.setText(h.getNombre());
        holder.txtAsignatura.setText(h.getAsignatura());
        holder.txtGrupo.setText(h.getGrado_grupo());
        String hora = getHoraDia(h);
        holder.txtHorario.setText(hora != null ? hora : "No disponible");

        holder.chkAsistencia.setOnCheckedChangeListener(null);
        holder.chkRetardo   .setOnCheckedChangeListener(null);
        holder.chkFalta     .setOnCheckedChangeListener(null);

        holder.chkAsistencia.setChecked(false);
        holder.chkRetardo   .setChecked(false);
        holder.chkFalta     .setChecked(false);

        holder.btnFirmarMaestro.setVisibility(View.GONE);
        holder.btnFirmarJefe   .setVisibility(View.GONE);
        holder.panelObservaciones.setVisibility(View.GONE);

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

        holder.btnFirmarMaestro.setOnClickListener(v -> {
            selectedPosition = -1;
            notifyDataSetChanged();
        });

        holder.btnFirmarJefe.setOnClickListener(v -> {
            selectedPosition = -1;
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return horarios.size();
    }

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

    private String getHoraActual() {
        return DateFormat.format("HH:mm:ss", new Date()).toString();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public void setSelection(int position, String estado) {
        this.selectedPosition = position;
        this.estadoActual    = estado;
        notifyDataSetChanged();
    }

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
