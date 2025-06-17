package es.ujaen.dae.sociosclub.repositorios;
import es.ujaen.dae.sociosclub.entidades.Actividad;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Transactional
@Repository
public class RepositorioActividad {
    @PersistenceContext
    EntityManager em;

    public void crear(Actividad actividad) {
        em.persist(actividad);
    }

    public Optional<Actividad> buscarPorId(int id) {
        Actividad actividad = em.find(Actividad.class, id);
        if (actividad != null) {
            actividad.getSolicitudes().size();
        }
        return Optional.ofNullable(actividad);
    }

    public List<Actividad> buscarPorFechaCelebracionEntre(LocalDate desde, LocalDate hasta) {
        if (hasta != null) {
            TypedQuery<Actividad> query = em.createQuery(
                    "SELECT a FROM Actividad a WHERE a.fechaCelebracion >= :desde AND a.fechaCelebracion <= :hasta",
                    Actividad.class);
            query.setParameter("desde", desde);
            query.setParameter("hasta", hasta);
            return query.getResultList();
        } else {
            TypedQuery<Actividad> query = em.createQuery(
                    "SELECT a FROM Actividad a WHERE a.fechaCelebracion >= :desde",
                    Actividad.class);
            query.setParameter("desde", desde);
            return query.getResultList();
        }
    }

    public List<Actividad> buscarFuturas() {
        return em.createQuery(
                        "SELECT a FROM Actividad a WHERE a.fechaCelebracion > :hoy", Actividad.class)
                .setParameter("hoy", LocalDate.now())
                .getResultList();
    }

    public List<Actividad> buscarFuturasPorTemporada(int anio) {
        TypedQuery<Actividad> query = em.createQuery(
                "SELECT a FROM Actividad a WHERE a.temporada.anio = :anio AND a.fechaCelebracion > :hoy", Actividad.class);
        query.setParameter("anio", anio);
        query.setParameter("hoy", LocalDate.now());
        return query.getResultList();
    }

    public List<Actividad> buscarTodas() {
        return em.createQuery("SELECT a FROM Actividad a", Actividad.class).getResultList();
    }

    public void actualizar(Actividad actividad) {
        em.merge(actividad);
    }

    public void eliminar(Actividad actividad) {
        em.remove(em.contains(actividad) ? actividad : em.merge(actividad));
    }
}

