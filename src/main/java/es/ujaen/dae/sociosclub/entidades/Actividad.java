package es.ujaen.dae.sociosclub.entidades;

import java.util.Date;

public class Actividad {
    private String tituloCorto;
    private String descripcion;
    private float precio;
    private int numPlazas;
    private Date fechaCelebracion;
    private Date fechaInicio;
    private Date fechaFin;
    private Temporada temporada;


    public Actividad(String tituloCorto, String descripcion, float precio, int numPlazas, Date fechaCelebracion, Date fechaInicio, Date fechaFin) {
        this.tituloCorto = tituloCorto;
        this.descripcion = descripcion;
        this.precio = precio;
        this.numPlazas = numPlazas;
        this.fechaCelebracion = fechaCelebracion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

   
    public String getTituloCorto(){
        return tituloCorto;
    }

    public String getDescripcion(){
        return descripcion;
    }
    public float getPrecio(){
        return precio;
    }
    public int getNumPlazas(){
        return numPlazas;
    }
    public Date getFechaCelebracion(){
        return fechaCelebracion;
    }
    public Date getFechaInicio(){
        return fechaInicio;
    }
    public Date getFechaFin(){
        return fechaFin;
    }

    public Temporada getTemporada() {
        return temporada;
    }

    public void setTemporada(Temporada temporada) {
        this.temporada = temporada;
    }
}
