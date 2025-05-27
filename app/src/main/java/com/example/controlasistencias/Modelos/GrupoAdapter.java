package com.example.controlasistencias.Modelos;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlasistencias.ProfesoresActivity;
import com.example.controlasistencias.R;

import java.util.List;

public class GrupoAdapter extends RecyclerView.Adapter<GrupoAdapter.ViewHolder> {

    private Context context;
    private List<Grupo> listaGrupos;

    public GrupoAdapter(Context context, List<Grupo> listaGrupos) {
        this.context = context;
        this.listaGrupos = listaGrupos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_grupo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Grupo grupo = listaGrupos.get(position);
        holder.textGrupo.setText(grupo.getGradoGrupo());

        // ✅ Aquí es donde se manda el grupoId
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProfesoresActivity.class);
            intent.putExtra("grupoId", grupo.getId()); // 👈 Asegúrate de que grupo.getId() sea un INT válido
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaGrupos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textGrupo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textGrupo = itemView.findViewById(R.id.textViewGrupo); // 👈 Asegúrate que el ID exista en item_grupo.xml
        }
    }
}
