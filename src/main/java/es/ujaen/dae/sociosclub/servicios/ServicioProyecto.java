package es.ujaen.dae.sociosclub.servicios;

import es.ujaen.dae.sociosclub.entidades.Actividad;
import es.ujaen.dae.sociosclub.entidades.Solicitudes;
import es.ujaen.dae.sociosclub.entidades.Temporada;
import es.ujaen.dae.sociosclub.entidades.Usuario;
import es.ujaen.dae.sociosclub.excepciones.*;
import es.ujaen.dae.sociosclub.repositorios.RepositorioActividad;
import es.ujaen.dae.sociosclub.repositorios.RepositorioSolicitudes;
import es.ujaen.dae.sociosclub.repositorios.RepositorioTemporada;
import es.ujaen.dae.sociosclub.repositorios.RepositorioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Validated
public class ServicioProyecto {

    @Autowired
    private RepositorioUsuario repositorioUsuario;
    @Autowired
    private RepositorioActividad repositorioActividad;
    @Autowired
    private RepositorioSolicitudes repositorioSolicitudes;
    @Autowired
    private RepositorioTemporada repositorioTemporada;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public ServicioProyecto() {}

    /**
     * Crea un usuario: codifica la clave, persiste y devuelve la entidad persistida.
     * Lanza UsuarioYaRegistrado si ya existe.
     */
    @Transactional
    public Usuario crearUsuario(Usuario usuario) {
        // Verificar duplicado
        if (repositorioUsuario.buscarPorDni(usuario.getDni()).isPresent()) {
            throw new UsuarioYaRegistrado();
        }
        // Codificar la clave recibida en texto plano
        String claveCodificada = passwordEncoder.encode(usuario.getClave());
        usuario.setClave(claveCodificada);
        // El campo usuario.isAdministrador() ya viene del DTO mapeado
        repositorioUsuario.crear(usuario);
        return usuario;
    }

    /**
     * Buscar usuario por DNI, devuelve Optional<Usuario>.
     */
    public Optional<Usuario> buscarUsuarioPorDni(@NotBlank String dni) {
        return repositorioUsuario.buscarPorDni(dni);
    }

    /**
     * Busca usuario y lanza UsuarioNoRegistrado si no existe.
     */
    @Transactional(readOnly = true)
    public Usuario buscarUsuario(String dni) {
        return repositorioUsuario.buscarPorDni(dni)
                .orElseThrow(UsuarioNoRegistrado::new);
    }

    /**
     * Login: verifica existencia y coincidencia de clave. Lanza UsuarioNoRegistrado o ClaveoUsuarioIncorrecto.
     */
    public boolean login(@NotBlank String dni, @NotBlank String clave) {
        Usuario usuario = repositorioUsuario.buscarPorDni(dni)
                .orElseThrow(UsuarioNoRegistrado::new);
        // Verificar con passwordEncoder
        if (!passwordEncoder.matches(clave, usuario.getClave())) {
            throw new ClaveoUsuarioIncorrecto("Usuario o clave incorrecto");
        }
        return true;
    }

    /**
     * Crea una actividad y la enlaza a temporada (crea temporada si no existe).
     * Devuelve la entidad Actividad persistida.
     */
    @Transactional
    public Actividad crearActividad(@NotBlank String tituloCorto,
                                    @NotBlank String descripcion,
                                    @Positive double precio,
                                    @Positive int numPlazas,
                                    @FutureOrPresent LocalDate fechaCelebracion,
                                    @FutureOrPresent LocalDate fechaInicio,
                                    @FutureOrPresent LocalDate fechaFinal) {
        int anioTemporada = fechaCelebracion.getYear();
        Temporada temporada = repositorioTemporada.buscarPorAnio(anioTemporada)
                .orElseGet(() -> {
                    Temporada t = new Temporada(anioTemporada);
                    repositorioTemporada.crear(t);
                    return t;
                });
        Actividad actividad = new Actividad(tituloCorto, descripcion, precio, numPlazas,
                fechaCelebracion, fechaInicio, fechaFinal);
        // Enlazar actividad a temporada en memoria
        temporada.agregarActividad(actividad);
        // Persistir actividad
        repositorioActividad.crear(actividad);
        return actividad;
    }

    /**
     * Busca todas las temporadas.
     */
    @Transactional(readOnly = true)
    public List<Temporada> buscarTodasTemporadas() {
        return repositorioTemporada.buscarTodas();
    }

    /**
     * Busca actividades futuras. Asume repositorioActividad.buscarFuturas() implementa la lógica.
     */
    @Transactional(readOnly = true)
    public List<Actividad> buscarActividades() {
        return repositorioActividad.buscarFuturas();
    }

    /**
     * Busca actividad por id. Lanza ActividadNoRegistrada si no existe.
     */
    @Transactional(readOnly = true)
    public Actividad buscarActividadPorId(int idActividad) {
        return repositorioActividad.buscarPorId(idActividad)
                .orElseThrow(ActividadNoRegistrada::new);
    }

    /**
     * Crea una solicitud: lanza ActividadNoRegistrada o UsuarioNoRegistrado o UsuarioYaRegistrado.
     * Devuelve la entidad Solicitudes creada.
     */
    @Transactional
    public Solicitudes crearSolicitud(int idActividad, @NotBlank String dniSocio,
                                      @Min(0) @Max(5) int num_acomp) {
        Actividad actividad = repositorioActividad.buscarPorId(idActividad)
                .orElseThrow(ActividadNoRegistrada::new);
        Usuario socio = repositorioUsuario.buscarPorDni(dniSocio)
                .orElseThrow(UsuarioNoRegistrado::new);
        // Verificar duplicado: si ya hay solicitud para este usuario en la misma actividad
        boolean ya = actividad.getSolicitudes().stream()
                .anyMatch(s -> s.getUsuario().getDni().equals(dniSocio));
        if (ya) {
            throw new UsuarioYaRegistrado();
        }
        Solicitudes solicitud = new Solicitudes(actividad, socio, num_acomp);
        repositorioSolicitudes.crear(solicitud);
        // Enlazar en memoria
        actividad.altaSolicitud(solicitud);
        repositorioActividad.actualizar(actividad);
        return solicitud;
    }

    /**
     * Modificar número de acompañantes en solicitud existente. Lanza SolicitudNoRegistrada, ActividadNoRegistrada, PlazasNoDisponibles.
     */
    @Transactional
    public void modificarSolicitudNumAcomp(long idSolicitud, int numAcomp) {
        Solicitudes solicitud = repositorioSolicitudes.buscarPorId(idSolicitud)
                .orElseThrow(SolicitudNoRegistrada::new);
        Actividad actividad = solicitud.getActividad();
        if (actividad == null) {
            throw new ActividadNoRegistrada();
        }
        int oldNum = solicitud.getNumAcomp();
        int diff = (1 + numAcomp) - (1 + oldNum);
        if (diff > actividad.getNumPlazas()) {
            throw new PlazasNoDisponibles();
        }
        solicitud.setNumAcomp(numAcomp);
        actividad.setNumPlazas(actividad.getNumPlazas() - diff);
        repositorioSolicitudes.actualizar(solicitud);
        repositorioActividad.actualizar(actividad);
    }

    /**
     * Asigna plaza(s) a una solicitud: cambia estado y decrementa plazas. Lanza ActividadNoRegistrada si no existe.
     */
    @Transactional
    public void asignarPlaza(@NotNull Solicitudes solicitud, @Positive int plazasAsignadas) {
        Actividad actividad = solicitud.getActividad();
        if (actividad == null) {
            throw new ActividadNoRegistrada();
        }
        actividad.asignarPlaza(solicitud, plazasAsignadas);
        repositorioActividad.actualizar(actividad);
        repositorioSolicitudes.actualizar(solicitud);
    }

    /**
     * Obtiene solicitudes pendientes de una actividad (por idActividad).
     * Lanza ActividadNoRegistrada si no existe.
     */
    @Transactional(readOnly = true)
    public List<Solicitudes> obtenerSolicitudesPendientes(@Positive int idActividad) {
        Actividad actividad = repositorioActividad.buscarPorId(idActividad)
                .orElseThrow(ActividadNoRegistrada::new);
        return actividad.getSolicitudesPendientes();
    }

    /**
     * Crea una temporada si no existe; lanza TemporadaYaExistente si ya existía.
     */
    @Transactional
    public Temporada crearTemporada(int anio) {
        Optional<Temporada> temporadaOpt = repositorioTemporada.buscarPorAnio(anio);
        if (temporadaOpt.isPresent()) {
            throw new TemporadaYaExistente();
        }
        Temporada nuevaTemporada = new Temporada(anio);
        repositorioTemporada.crear(nuevaTemporada);
        return nuevaTemporada;
    }

    /**
     * Borra solicitud por id. Lanza SolicitudNoRegistrada si no existe.
     */
    @Transactional
    public void borrarSolicitud(long idSolicitud) {
        Solicitudes solicitud = repositorioSolicitudes.buscarPorId(idSolicitud)
                .orElseThrow(SolicitudNoRegistrada::new);
        Actividad actividad = solicitud.getActividad();
        if (actividad != null) {
            actividad.borrarSolicitud(solicitud);
            repositorioActividad.actualizar(actividad);
        }
        repositorioSolicitudes.eliminar(solicitud);
    }

    /**
     * Buscar solicitud por id. Lanza SolicitudNoRegistrada si no existe.
     */
    @Transactional(readOnly = true)
    public Solicitudes buscarSolicitudPorId(long idSolicitud) {
        return repositorioSolicitudes.buscarPorId(idSolicitud)
                .orElseThrow(SolicitudNoRegistrada::new);
    }

    /**
     * Marca cuota pagada para un usuario (solo admin vía controlador). Lanza UsuarioNoRegistrado si no existe.
     */
    @Transactional
    public void marcarCuotaPagada(String dni) {
        Usuario usuario = repositorioUsuario.buscarPorDni(dni)
                .orElseThrow(UsuarioNoRegistrado::new);
        if (!usuario.isCuotaPagada()) {
            usuario.setCuotaPagada(true);
            repositorioUsuario.actualizar(usuario);
        }
    }

    /**
     * Busca actividades futuras de una temporada. Lanza TemporadaNoEncontrada si no existe.
     */
    @Transactional(readOnly = true)
    public List<Actividad> buscarActividadesPorTemporada(int anio) {
        repositorioTemporada.buscarPorAnio(anio)
                .orElseThrow(() -> new TemporadaNoEncontrada("Temporada no encontrada"));
        return repositorioActividad.buscarFuturasPorTemporada(anio);
    }

    /**
     * Busca actividades por rango de fechas de celebración, desde (o hoy si null) hasta fechaHasta (puede ser null).
     */
    @Transactional(readOnly = true)
    public List<Actividad> buscarActividadesPorRango(LocalDate fechaDesde, LocalDate fechaHasta) {
        LocalDate desde = (fechaDesde != null) ? fechaDesde : LocalDate.now();
        return repositorioActividad.buscarPorFechaCelebracionEntre(desde, fechaHasta);
    }

    /**
     * Lista solicitudes de una actividad (admin). Lanza ActividadNoRegistrada si no existe.
     */
    @Transactional(readOnly = true)
    public List<Solicitudes> listarSolicitudesDeActividad(int idActividad) {
        repositorioActividad.buscarPorId(idActividad)
                .orElseThrow(ActividadNoRegistrada::new);
        return repositorioSolicitudes.listarSolicitudesPorActividad(idActividad);
    }

    /**
     * Actualiza usuario según DTO DUsuario (admin). Lanza UsuarioNoRegistrado si no existe.
     */
    @Transactional
    public void actualizarUsuarioDesdeDto(es.ujaen.dae.sociosclub.rest.dto.DUsuario dto) {
        Usuario usuario = repositorioUsuario.buscarPorDni(dto.dni())
                .orElseThrow(UsuarioNoRegistrado::new);
        usuario.setNombre(dto.nombre());
        usuario.setApellidos(dto.apellidos());
        usuario.setDireccion(dto.direccion());
        usuario.setTlf(dto.tlf());
        usuario.setEmail(dto.email());
        usuario.setAdministrador(dto.administrador());
        usuario.setCuotaPagada(dto.cuotaPagada());
        if (dto.clave() != null && !dto.clave().isBlank()) {
            String encoded = passwordEncoder.encode(dto.clave());
            usuario.setClave(encoded);
        }
        repositorioUsuario.actualizar(usuario);
    }

    /**
     * Comprueba si el usuario con DNI dado es el propietario de la solicitud de id dado.
     * Devuelve false si la solicitud no existe o no coincide.
     */
    @Transactional(readOnly = true)
    public boolean esPropietarioSolicitud(long idSolicitud, String dniUsuario) {
        return repositorioSolicitudes.buscarPorId(idSolicitud)
                .map(s -> s.getUsuario().getDni().equals(dniUsuario))
                .orElse(false);
    }
}
