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
    }

    public Optional<Actividad> buscarPorId(int id) {
        Actividad actividad = em.find(Actividad.class, id);
        if (actividad != null) {
            actividad.getSolicitudes().size();
        }
        return Optional.ofNullable(actividad);
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

