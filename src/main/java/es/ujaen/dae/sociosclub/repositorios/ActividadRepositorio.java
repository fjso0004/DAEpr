package es.ujaen.dae.sociosclub.repositorios;

import es.ujaen.dae.sociosclub.entidades.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActividadRepositorio extends JpaRepository<Actividad, Long> {
    List<Actividad> findByTituloCorto(String tituloCorto);
}
