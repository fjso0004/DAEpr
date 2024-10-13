package es.ujaen.dae.sociosclub.entidades;

import java.util.List;
import java.util.ArrayList;

public class Temporada {
    private int ano;
    private List<Actividad> actividades;


    public Temporada(int ano) {
        this.ano = ano;
        this.actividades = new ArrayList<>(); 
    }


    public void agregarActividad(Actividad actividad) {
        actividades.add(actividad);
        actividad.setTemporada(this);
    }


    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public List<Actividad> getActividades() {
        return actividades;
    }

    public void setActividades(List<Actividad> actividades) {
        this.actividades = actividades;
    }
}
