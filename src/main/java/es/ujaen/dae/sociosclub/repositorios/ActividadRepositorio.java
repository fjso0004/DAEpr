package es.ujaen.dae.sociosclub.repositorios;

import es.ujaen.dae.sociosclub.entidades.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ActividadRepositorio extends JpaRepository<Actividad, Long> {
    List<Actividad> findByTituloCortoAndFechaCelebracion(String tituloCorto, LocalDate fechaCelebracion);
}
