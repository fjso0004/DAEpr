package es.ujaen.dae.sociosclub.entidades;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
public class Solicitudes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id = 0;

    @Min(value = 0)
    @Max(value = 5)
    private int num_acomp;

    private LocalDate fechaSolicitud;

    public enum EstadoSolicitud {
        PENDIENTE, ACEPTADA, RECHAZADA
    }

    private EstadoSolicitud estado;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_actividad")
    private Actividad actividad;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_dni", nullable = false)
    private Usuario usuario;

    public Solicitudes() {}

    public Solicitudes(Actividad actividad, Usuario usuario, int num_acomp) {
        this.actividad = actividad;
        this.usuario = usuario;
        this.num_acomp = num_acomp;
        this.fechaSolicitud = LocalDate.now();

        if (usuario.getCuota() && actividad.getNumPlazas() >= (1 + num_acomp)) {
            this.estado = EstadoSolicitud.ACEPTADA;
        } else {
            this.estado = EstadoSolicitud.PENDIENTE;
        }
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
    public void setActividad(Actividad actividad) {
        this.actividad = actividad;
    }
    public Actividad getActividad() { return actividad; }
    public Usuario getUsuario() { return usuario; }
    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

}
