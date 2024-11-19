package es.ujaen.dae.sociosclub.repositorios;
import es.ujaen.dae.sociosclub.entidades.Temporada;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Transactional
@Repository
public class RepositorioTemporada {
    @PersistenceContext
    EntityManager em;

    public void crear(Temporada temporada) {
        em.persist(temporada);
        em.flush();
    }

    public Optional<Temporada> buscarPorAnio(int anio) {
        return Optional.ofNullable(em.find(Temporada.class, anio));
    }

    //Completar
    public List<Temporada> buscarTodas() {
        //return em.createQuery("SELECT t FROM Temporada t", Temporada.class).getResultList();
        return null;
    }

    public void actualizar(Temporada temporada) {
        em.merge(temporada);
        em.flush();
    }

    public void eliminar(Temporada temporada) {
        em.remove(em.contains(temporada) ? temporada : em.merge(temporada));
        em.flush();
    }
}
