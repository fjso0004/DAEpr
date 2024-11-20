package es.ujaen.dae.sociosclub.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class Solicitudes {

    public enum EstadoSolicitud {
        PENDIENTE, ACEPTADA, RECHAZADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(value = 0)
    @Max(value = 5)
    private int num_acomp;

    @NotBlank
    private LocalDate fechaSolicitud;

    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estado;

    @ManyToOne
    @JoinColumn(name = "actividad_id")
    private Actividad actividad;

    @ManyToOne
    @JoinColumn(name = "usuario_dni")
    private Usuario usuario;


    public Solicitudes() {
        this.id = generarIdSolicitud();
        this.num_acomp = num_acomp;
        this.estado = EstadoSolicitud.PENDIENTE;
        this.fechaSolicitud = LocalDate.now();
    }

    public Solicitudes(Actividad actividad, Usuario usuario ) {
        this();
        this.actividad = actividad;
        this.usuario = usuario;
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
