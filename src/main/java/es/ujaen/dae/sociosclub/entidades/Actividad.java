package es.ujaen.dae.sociosclub.entidades;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Actividad {

    private long id = 0;
    @NotBlank
    private String tituloCorto;

    @NotBlank
    private String descripcion;

    @Positive
    private double precio;

    @Positive
    private int numPlazas;

    @FutureOrPresent
    private LocalDate fechaCelebracion;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @NotNull
    private Temporada temporada;


    private  List<Solicitudes> solicitudes = new ArrayList<>();


    public Actividad(String tituloCorto, String descripcion, double precio, int numPlazas, LocalDate fechaCelebracion, LocalDate fechaInicio,
                     LocalDate fechaFin) {
        this.id = generarIdActividad();
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
    public double getPrecio(){
        return precio;
    }
    public int getNumPlazas(){
        return numPlazas;
    }
    public LocalDate getFechaCelebracion(){
        return fechaCelebracion;
    }
    public LocalDate getFechaInicio(){
        return fechaInicio;
    }
    public LocalDate getFechaFin(){
        return fechaFin;
    }

    public Temporada getTemporada() {
        return temporada;
    }

    public void setTemporada(Temporada temporada) {
        this.temporada = temporada;
    }


public boolean altaSolicitud(Solicitudes solicitud) {
    int totalSolicitantes = 1 + solicitud.getNumAcomp(); 

    if (numPlazas >= totalSolicitantes) {
        solicitudes.add(solicitud);
        solicitud.setActividad(this); 
        numPlazas -= totalSolicitantes; 
        return true; 
    } else {
        System.out.println("No hay suficientes plazas disponibles para esta solicitud.");
        return false; 
    }
}

    public void borrarSolicitud(Solicitudes solicitud){
        solicitud.setActividad(null);
        solicitudes.remove(solicitud);
    }

    public List<Solicitudes> getSolicitudes(){
        return this.solicitudes;
    }

    public List<Solicitudes> getSolicitudesPendientes(){
        return this.solicitudes.stream().filter(solicitudes -> solicitudes.getEstado().equals(Solicitudes.EstadoSolicitud.PENDIENTE)).collect(Collectors.toList());
    }

    public long generarIdActividad(){
        id++;
        return id;
    }

    public long getId(){
        return id;
    }


}

