package es.ujaen.dae.sociosclub.repositorios;

import es.ujaen.dae.sociosclub.entidades.Temporada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemporadaRepositorio extends JpaRepository<Temporada, Integer> {
    Temporada findByAnio(int anio);
}
