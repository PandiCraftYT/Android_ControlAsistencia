package com.example.controlasistencias.Modelos;

import com.google.gson.annotations.SerializedName;

public class Grupo {
    @SerializedName("id")
    private String id;

    @SerializedName("grado_grupo")
    private String gradoGrupo;

    public String getId() { return id; }
    public String getGradoGrupo() { return gradoGrupo; }
}


