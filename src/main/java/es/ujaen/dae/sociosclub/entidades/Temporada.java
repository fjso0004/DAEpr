package es.ujaen.dae.sociosclub.entidades;
import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
public class Temporada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int anio;

    @OneToMany
    private List<Actividad> actividades = new ArrayList<>();

    public Temporada() {

    }

    public Temporada(int anio) {
        this.anio = anio;
        this.actividades = new ArrayList<>();
    }


    public void agregarActividad(Actividad actividad) {
        actividades.add(actividad);
        actividad.setTemporada(this);
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public List<Actividad> getActividades() {
        return actividades;
    }

    public void setActividades(List<Actividad> actividades) {
        this.actividades = actividades;
    }
}
