package es.ujaen.dae.sociosclub.repositorios;

import es.ujaen.dae.sociosclub.entidades.Solicitudes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudesRepositorio extends JpaRepository<Solicitudes, Long> {
    List<Solicitudes> findByEstado(Solicitudes.EstadoSolicitud estado);
}

