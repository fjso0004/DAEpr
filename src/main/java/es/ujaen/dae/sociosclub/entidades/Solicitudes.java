package es.ujaen.dae.sociosclub.entidades;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class Solicitudes {

    private long id = 0;

    @Min(value = 0)
    @Max(value = 5)
    private int num_acomp;

    private LocalDate fechaSolicitud;

    public enum EstadoSolicitud {
        PENDIENTE, ACEPTADA, RECHAZADA
    }

    private EstadoSolicitud estado;

    @NotNull
    private Actividad actividad;

    @NotNull
    private Usuario usuario;


    public Solicitudes() {
        this.id = generarIdSolicitud();
        this.num_acomp = num_acomp;
        this.estado = EstadoSolicitud.PENDIENTE;
        this.fechaSolicitud = LocalDate.now();
    }

    public Solicitudes(Actividad actividad, Usuario usuario, int num_acomp, EstadoSolicitud estado) {
        this();
        this.actividad = actividad;
        this.usuario = usuario;
        this.num_acomp = num_acomp;
        this.estado = estado;
    }

    public long getId() {
        return id;
    }
    public int getNumAcomp() {
        return num_acomp;
    }
    public void setNumAcomp(int num_acomp) {
        this.num_acomp = num_acomp;
    }
    public LocalDate getFechaSolicitud() { return fechaSolicitud; }
    public Actividad getActividad() { return actividad; }
    public Usuario getUsuario() { return usuario; }

    public EstadoSolicitud getEstado() { return estado; }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    private long generarIdSolicitud(){
        id++;
        return id;
    }
}
