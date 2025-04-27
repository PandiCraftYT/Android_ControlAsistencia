package com.example.controlasistencias.Modelos;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlasistencias.HorariosActivity;
import com.example.controlasistencias.R;

import java.util.List;

public class ZonaAdapter extends RecyclerView.Adapter<ZonaAdapter.ZonaViewHolder> {

    private Context context;
    private List<String> zonas;

    public ZonaAdapter(Context context, List<String> zonas) {
        this.context = context;
        this.zonas = zonas;
    }

    @Override
    public ZonaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_zona, parent, false);
        return new ZonaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ZonaViewHolder holder, int position) {
        String zona = zonas.get(position);
        holder.txtZona.setText(zona);

        holder.cardZona.setOnClickListener(v -> {
            Intent intent = new Intent(context, HorariosActivity.class);
            intent.putExtra("zona", zona);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return zonas.size();
    }

    public static class ZonaViewHolder extends RecyclerView.ViewHolder {
        CardView cardZona;
        TextView txtZona;

        public ZonaViewHolder(View itemView) {
            super(itemView);
            cardZona = itemView.findViewById(R.id.cardZona);
            txtZona = itemView.findViewById(R.id.txtZona);
        }
    }
}
