# DAEpr

## Corrección extraordinaria por Francisco Javier Sánchez Olmo

### issue 1: 

Este GET sin filtros es poco útil. Devolvería actividades de años pasados ya cerradas. Además el resultado iría creciendo con el tiempo de forma indefinida.
@GetMapping("/actividades")
public ResponseEntity<List<DActividad>> listarActividades() {
...

#### solución: 

Listado de actividades

GET /sociosclub/actividades devuelve solo actividades con fechaCelebracion > hoy.

Permite filtros opcionales:

?anio=YYYY → actividades futuras en la temporada del año YYYY.

?fechaDesde=YYYY-MM-DD&fechaHasta=YYYY-MM-DD → actividades cuya fecha de celebración está dentro del rango.

Se valida fechaDesde <= fechaHasta y se manejan errores 400 o 404 (por ejemplo, temporada no existente).

Así evitamos devolver actividades ya celebradas y resultados de volumen creciente sin control.

### issue 2:

No se puede usar un request param para subir información en un POST. Estos parámetros solo se usan en los GET para filtrado de consultas (precisamente lo que no habéis hecho en el servicio anterior). Este es de los errores graves que se remarcaron en la teoría (mirad las transparencias):
@PostMapping("/solicitudes")
public ResponseEntity<Void> nuevaSolicitud(@RequestParam int idActividad, @RequestBody DSolicitud dSolicitud) {
...

#### solución: 

Crear solicitud

POST /sociosclub/actividades/{idActividad}/solicitudes

PathVariable: idActividad identifica la actividad padre.

Body: JSON con campos de la solicitud, p. ej. { "dniUsuario": "...", "numAcomp": 2 }.

No se usan RequestParam para datos de creación, sino que se sigue la ruta anidada RESTful y el DTO en RequestBody.

### issue 3:

Meter frases, verbos etc. en la ruta para indicar una acción es otro error (nuevamente aparece claramente indicado en las transparencias). Marcar la cuota pagada no es más que una modificación que se puede resolver con un PUT al usuario. Solo hay controlar que ese cambio lo haga la dirección y no el propio usuario.
@PutMapping("/usuarios/{dni}/pagoCuota")
public ResponseEntity<Void> marcarCuotaPagada(@PathVariable String dni) {
...

#### solución:

Actualizar usuario (marcar cuota pagada, cambiar datos, etc.)

PUT /sociosclub/usuarios/{dni} con body JSON que contenga los campos a actualizar, p. ej. { "dni":"12345678X", "nombre":"...", ..., "cuotaPagada": true, "clave": "" }.

Solo ADMIN puede invocar este PUT para marcar cuotaPagada o cambiar rol/clave.

Se evita introducir verbos en la ruta; en REST, la acción “marcar pago” se ve como una modificación del recurso usuario.

### issue 4:

Las reglas definidas en la clase ServicioSeguridad son un desastre y están llenas de agujeros de seguridad. Cualquier incluso sin hacer login puede crear temporadas, borrar o modificar solicitudes, actividades, etc.

#### solución: Seguridad y roles nuevos:

- Se usa HTTP Basic con BCryptPasswordEncoder.
- Registro de usuario: `POST /sociosclub/usuarios` es público.
- Login: Basic Auth en cada petición.
- Roles:
    - `ROLE_USUARIO`: cualquier usuario normal después de registrarse.
    - `ROLE_ADMIN`: usuarios con `administrador=true` en BD.
- Reglas de acceso (Spring Security):
    - `GET /sociosclub/usuarios/{dni}`: solo ADMIN o el propio usuario (`principal.username == dni`).
    - `PUT /sociosclub/usuarios/{dni}`: solo ADMIN.
    - `PUT /sociosclub/usuarios/{dni}/pagoCuota`: se sustituye por `PUT /sociosclub/usuarios/{dni}` con body `{..., "cuotaPagada": true}`.
    - `POST /sociosclub/temporadas`: solo ADMIN.
    - `GET /sociosclub/temporadas/**`: público.
    - `POST /sociosclub/actividades`: solo ADMIN.
    - `GET /sociosclub/actividades/**`: público.
    - `GET /sociosclub/temporadas/{anio}/actividades`: público (o autenticado según preferencia).
    - `POST /sociosclub/actividades/{idActividad}/solicitudes`: solo USUARIO.
    - `GET/PUT/DELETE /sociosclub/solicitudes/{idSolicitud}`: solo ADMIN o propietario de la solicitud.
    - `GET /sociosclub/actividades/{idActividad}/solicitudes`: solo ADMIN.
