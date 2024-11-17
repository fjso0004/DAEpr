package es.ujaen.dae.sociosclub.repositorios;


import es.ujaen.dae.sociosclub.entidades.Usuario;
import es.ujaen.dae.sociosclub.excepciones.UsuarioYaRegistrado;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

@Transactional
@Repository
public class RepositorioUsuario {
    @PersistenceContext
    EntityManager em;

    public void crear(Usuario usuario) {
        if (em.find(Usuario.class, usuario.getDni()) != null) {
            throw new UsuarioYaRegistrado();

        em.persist(usuario);
        em.flush();
        }
    }

}
