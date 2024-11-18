package es.ujaen.dae.sociosclub.repositorios;


import es.ujaen.dae.sociosclub.entidades.Usuario;
import es.ujaen.dae.sociosclub.excepciones.UsuarioYaRegistrado;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Transactional
@Repository
public class RepositorioUsuario {
    @PersistenceContext
    EntityManager em;

    public void crear(Usuario usuario) {
        if (em.find(Usuario.class, usuario.getDni()) != null) {
            throw new UsuarioYaRegistrado();
        }
        em.persist(usuario);
        em.flush();
    }

    public Optional<Usuario> buscarPorDni(String dni) {
        return Optional.ofNullable(em.find(Usuario.class, dni));
    }

    public void actualizar(Usuario usuario) {
        em.merge(usuario);
        em.flush();
    }

    public void eliminar(Usuario usuario) {
        em.remove(em.contains(usuario) ? usuario : em.merge(usuario));
        em.flush();
    }

}