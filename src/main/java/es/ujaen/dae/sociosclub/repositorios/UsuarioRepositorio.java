package es.ujaen.dae.sociosclub.repositorios;

import es.ujaen.dae.sociosclub.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, String> {
    Usuario findByDni(String dni);
}