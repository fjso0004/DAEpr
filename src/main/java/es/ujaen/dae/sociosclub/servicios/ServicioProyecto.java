package es.ujaen.dae.sociosclub.servicios;

import es.ujaen.dae.sociosclub.entidades.Actividad;
import es.ujaen.dae.sociosclub.entidades.Usuario;

import es.ujaen.dae.sociosclub.excepciones.ClaveIncorrecta;
import es.ujaen.dae.sociosclub.excepciones.UsuarioNoRegistrado;
import es.ujaen.dae.sociosclub.excepciones.UsuarioYaRegistrado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@Validated
public class ServicioProyecto {
    Map<String, Usuario> usuarios;
    Map<Long, Actividad> actividades;

    public ServicioProyecto() {
        usuarios = new TreeMap<>();
        actividades = new TreeMap<>();

    }

    public Usuario crearUsuario(@NotNull @Valid Usuario usuario) {
        if (usuarios.containsKey(usuario.getDni())) {
            throw new UsuarioYaRegistrado();
        }

        usuarios.put(usuario.getDni(), usuario);
        return usuario;
    }

    public Usuario autenticar(@NotBlank String dni, @NotBlank String clave) {
        Usuario usuario = buscarCliente(dni);
        if (usuario == null) {
            throw new UsuarioNoRegistrado();
        }

        if (!usuario.claveValida(clave)){
            throw new ClaveIncorrecta();
        }
        return usuario;
    }

    public Usuario buscarCliente(@NotBlank String dni) {
        Usuario usuario = usuarios.get(dni);
        if (usuario == null) {
            throw new UsuarioNoRegistrado();
        }
        return usuario;
    }

    public String normalizar(String texto){
        String str = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
        return str.trim().toLowerCase();
    }

    public List<Actividad> buscarActividades(@NotBlank String tituloCorto, @NotNull LocalDate fechaInicio, @NotNull LocalDate fechaFin){
        List<Actividad> actividadesFiltradas = new ArrayList<>();
        for (Actividad actividad : actividades.values()) {
            if ()
        }
    }

    private boolean actividadValida(Actividad actividad, String tituloCorto, LocalDate fechaCelebracion){
        return normalizar(actividad.getTituloCorto()).equals(normalizar(tituloCorto)) && actividad.getFechaCelebracion().equals(fechaCelebracion);
    }


}





