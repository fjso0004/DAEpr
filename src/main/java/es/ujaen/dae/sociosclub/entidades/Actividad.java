package es.ujaen.dae.sociosclub.entidades;

import jakarta.validation.constraints.*;

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

    @NotNull
    private Usuario direccion;

    private  List<Usuario> socios = new ArrayList<>();
    private  List<Solicitudes> solicitudes = new ArrayList<>();


    public Actividad(String tituloCorto, String descripcion, double precio, int numPlazas, LocalDate fechaCelebracion, LocalDate fechaInicio,
                     LocalDate fechaFin, Usuario direccion, Temporada temporada) {
        this.id = generarIdActividad();
        this.tituloCorto = tituloCorto;
        this.descripcion = descripcion;
        this.precio = precio;
        this.numPlazas = numPlazas;
        this.fechaCelebracion = fechaCelebracion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.direccion = direccion;
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
    public Usuario getDireccion(){ return direccion; }
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

    public void altaSolicitud(Solicitudes solicitud){
        solicitudes.add(solicitud);
    }

    public void borrarSolicitud(Solicitudes solicitud){
        solicitudes.remove(solicitud);
    }

    public List<Solicitudes> getSolicitudes(){
        return this.solicitudes;
    }

    public List<Solicitudes> getSolicitudesPendientes(){
        return this.solicitudes.stream().filter(solicitudes -> solicitudes.getEstado().equals(Solicitudes.EstadoSolicitud.PENDIENTE)).collect(Collectors.toList());
    }

    private long generarIdActividad(){
        id++;
        return id;
    }


}

