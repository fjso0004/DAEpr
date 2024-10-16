package es.ujaen.dae.sociosclub.entidades;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Actividad {
    @NotBlank
    private String tituloCorto;

    @NotBlank
    private String descripcion;

    @Positive
    private double precio;

    @PositiveOrZero
    private int numPlazas;

    @FutureOrPresent
    private LocalDate fechaCelebracion;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @NotNull
    private Temporada temporada;

    private  List<Usuario> socios = new ArrayList<>();
    private  List<Solicitudes> solicitudes = new ArrayList<>();


    public Actividad(String tituloCorto, String descripcion, double precio, int numPlazas, LocalDate fechaCelebracion, LocalDate fechaInicio,
                     LocalDate fechaFin, Temporada temporada) {
        this.tituloCorto = tituloCorto;
        this.descripcion = descripcion;
        this.precio = precio;
        this.numPlazas = numPlazas;
        this.fechaCelebracion = fechaCelebracion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.temporada = temporada;
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

/*  public void setTemporada(Temporada temporada) {
        this.temporada = temporada;
    }
*/
    public List<Usuario> getSocios(){
        return new ArrayList<>(socios);
    }

    public void nuevoSocio(Usuario usuario){
        if (numPlazas > 0) {
            socios.add(usuario);
            numPlazas--;
        }
    }

    public void anadirSolicitud(Solicitudes solicitud){
        solicitudes.add(solicitud);
    }

    public void borrarSolicitud(Solicitudes solicitud){
        solicitudes.remove(solicitud);
    }

    public List<Solicitudes> getSolicitudes(){
        return this.solicitudes;
    }

    public List<Solicitudes> getSolicitudesPendientes(){
        return this.solicitudes.stream().filter(solicitud -> solicitud.getEstado().equals(Solicitudes.estadoSolicitud.PENDIENTE)).collect(Collectors.toList());
    }


}

