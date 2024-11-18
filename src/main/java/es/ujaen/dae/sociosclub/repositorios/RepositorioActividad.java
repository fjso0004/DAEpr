package es.ujaen.dae.sociosclub.repositorios;

import es.ujaen.dae.sociosclub.entidades.Actividad;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Transactional
@Repository
public class RepositorioActividad {
    @PersistenceContext
    EntityManager em;

    public void crear(Actividad actividad) {
        em.persist(actividad);
        em.flush();
    }

    public Optional<Actividad> buscarPorId(long id) {
        return Optional.ofNullable(em.find(Actividad.class, id));
    }

    //Completar
    public List<Actividad> buscarTodas() {
        return null;
    }

    public void actualizar(Actividad actividad) {
        em.merge(actividad);
        em.flush();
    }

    public void eliminar(Actividad actividad) {
        em.remove(em.contains(actividad) ? actividad : em.merge(actividad));
        em.flush();
    }
}

