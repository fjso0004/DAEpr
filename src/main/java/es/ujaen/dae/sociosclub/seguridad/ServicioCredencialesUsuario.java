package es.ujaen.dae.sociosclub.seguridad;

import es.ujaen.dae.sociosclub.entidades.Usuario;
import es.ujaen.dae.sociosclub.servicios.ServicioProyecto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ServicioCredencialesUsuario implements UserDetailsService {

    @Autowired
    ServicioProyecto servicioProyecto;

    @Override
    public UserDetails loadUserByUsername(String dni) throws UsernameNotFoundException {
        Usuario usuario = servicioProyecto.buscarUsuarioPorDni(dni)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return User.withUsername(usuario.getDni())
                .password(usuario.getClave())
                .roles(usuario.getDni().equals("12345678Z") ? "ADMIN" : "USUARIO")
                .build();
    }
}