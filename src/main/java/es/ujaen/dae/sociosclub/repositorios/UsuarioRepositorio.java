package es.ujaen.dae.sociosclub.repositorios;

import es.ujaen.dae.sociosclub.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepositorio extends JpaRepository<Usuario, String> {
    Usuario findByDni(String dni);
}