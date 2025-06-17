package es.ujaen.dae.sociosclub.rest.dto;

/**
 * Representa un usuario en el sistema.
 *
 * @param dni DNI del usuario.
 * @param nombre Nombre del usuario.
 * @param apellidos Apellidos del usuario.
 * @param direccion Dirección del usuario.
 * @param tlf Teléfono del usuario.
 * @param email Correo electrónico del usuario.
 * @param administrador Indica si el usuario es administrador.
 * @param cuotaPagada Indica si el usuario ha pagado la cuota.
 * @param clave Clave para registro o autenticación (opcional en respuestas o para cambio de clave).
 */
public record DUsuario(
        String dni,
        String nombre,
        String apellidos,
        String direccion,
        String tlf,
        String email,
        boolean administrador,
        boolean cuotaPagada,
        String clave
) {}