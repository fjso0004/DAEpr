package es.ujaen.dae.sociosclub.repositorios;
import es.ujaen.dae.sociosclub.entidades.Solicitudes;
import es.ujaen.dae.sociosclub.excepciones.SolicitudNoRegistrada;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Transactional
@Repository
public class RepositorioSolicitudes {
    @PersistenceContext
    EntityManager em;

    public void crear(Solicitudes solicitud) {
        System.out.println("Actividad asociada: " + solicitud.getActividad());
        System.out.println("Usuario asociado: " + solicitud.getUsuario());
        em.persist(solicitud);
        em.flush();
    }

    public Optional<Solicitudes> buscarPorId(long id) {
        return Optional.ofNullable(em.find(Solicitudes.class, id));
    }

    //Completar
    public List<Solicitudes> buscarPorActividadId(long actividadId) {
        //return em.createQuery("SELECT s FROM Solicitudes s WHERE s.actividad.id = :actividadId", Solicitudes.class)
        //        .setParameter("actividadId", actividadId)
        //        .getResultList();
        return null;
    }

    public void actualizar(Solicitudes solicitud) {
        em.merge(solicitud);
        em.flush();
    }

    public void eliminar(Solicitudes solicitud) {
        em.remove(em.contains(solicitud) ? solicitud : em.merge(solicitud));
        em.flush();
    }
}

