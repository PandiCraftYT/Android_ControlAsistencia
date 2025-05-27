package com.example.controlasistencias.Modelos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import com.example.controlasistencias.R;

import java.util.List;

public class GrupoAdapter extends RecyclerView.Adapter<GrupoAdapter.GrupoViewHolder> {
    private Context context;
    private List<Grupo> listaGrupos;

    public GrupoAdapter(Context context, List<Grupo> listaGrupos) {
        this.context = context;
        this.listaGrupos = listaGrupos;
    }

    @NonNull
    @Override
    public GrupoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grupo, parent, false);
        return new GrupoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GrupoViewHolder holder, int position) {
        Grupo grupo = listaGrupos.get(position);
        holder.textViewGrupo.setText(grupo.getGradoGrupo());
    }

    @Override
    public int getItemCount() {
        return listaGrupos.size();
    }

    public static class GrupoViewHolder extends RecyclerView.ViewHolder {
        TextView textViewGrupo;

        public GrupoViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGrupo = itemView.findViewById(R.id.textViewGrupo);
        }
    }
}

