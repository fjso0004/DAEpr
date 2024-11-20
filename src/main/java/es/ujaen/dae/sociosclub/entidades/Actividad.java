package es.ujaen.dae.sociosclub.entidades;
import es.ujaen.dae.sociosclub.excepciones.ActividadNoRegistrada;
import es.ujaen.dae.sociosclub.excepciones.PlazasNoDisponibles;
import es.ujaen.dae.sociosclub.excepciones.SolicitudNoRegistrada;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank
    private String tituloCorto;

    @NotBlank
    private String descripcion;

    @Positive
    private double precio;

    @Positive
    private int numPlazas;

    @NotBlank
    private LocalDate fechaCelebracion;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "id-temporada")
    private Temporada temporada;

    @OneToMany(mappedBy = "actividad",fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Solicitudes> solicitudes = new ArrayList<>();

    public Actividad() {

    }

    public Actividad(String tituloCorto, String descripcion, double precio, int numPlazas, LocalDate fechaCelebracion, LocalDate fechaInicio,
                     LocalDate fechaFin) {
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
    public void setNumPlazas(int numPlazas){this.numPlazas = numPlazas;}
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


    public void altaSolicitud(Solicitudes solicitud) {
        int totalSolicitantes = 1 + solicitud.getNumAcomp();

        if (numPlazas >= totalSolicitantes) {
            solicitud.setActividad(this);
            this.solicitudes.add(solicitud);
            this.numPlazas -= totalSolicitantes;
        } else {
            throw new PlazasNoDisponibles();
        }
    }

    public void borrarSolicitud(Solicitudes solicitud){
        if (this.solicitudes.remove(solicitud)) {
            solicitud.setActividad(null);
        }
    }

    public List<Solicitudes> getSolicitudes(){
        return this.solicitudes;
    }

    public List<Solicitudes> getSolicitudesPendientes(){
        return this.solicitudes.stream().filter(solicitudes -> solicitudes.getEstado().equals(Solicitudes.EstadoSolicitud.PENDIENTE)).collect(Collectors.toList());
    }

    public int getId(){
        return id;
    }

}
