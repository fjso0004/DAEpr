package es.ujaen.dae.sociosclub.entidades;
import es.ujaen.dae.sociosclub.util.CodificadorMd5;
import es.ujaen.dae.sociosclub.util.ExprReg;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.*;

@Entity
public class Usuario {

    @Id
    @Pattern(regexp = ExprReg.DNI)
    String dni;

    @NotBlank
    String nombre;

    @NotBlank
    String apellidos;

    @NotBlank
    String direccion;

    String tlf;

    @Email
    String email;

    @Size(min = 8)
    String clave;

    boolean cuotaPagada;

    boolean administrador;  // nuevo campo

    public Usuario() {
    }

    // Constructor completo: clave en texto plano, luego se codifica en servicio
    public Usuario(String dni, String nombre, String apellidos, String direccion, String tlf,
                   String email, String clave, boolean cuotaPagada, boolean administrador) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.direccion = direccion;
        this.tlf = tlf;
        this.email = email;
        this.clave = clave;
        this.cuotaPagada = cuotaPagada;
        this.administrador = administrador;
    }

    // Constructor sin administrador: por defecto administrador = false
    public Usuario(String dni, String nombre, String apellidos, String direccion, String tlf,
                   String email, String clave, boolean cuotaPagada) {
        this(dni, nombre, apellidos, direccion, tlf, email, clave, cuotaPagada, false);
    }

    // Métodos de validación de clave, etc.
    public boolean claveValida(String clave) {
        return this.clave.equals(CodificadorMd5.codificar(clave));
    }

    // Getters y setters
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTlf() { return tlf; }
    public void setTlf(String tlf) { this.tlf = tlf; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public boolean isCuotaPagada() { return cuotaPagada; }
    public void setCuotaPagada(boolean cuotaPagada) { this.cuotaPagada = cuotaPagada; }

    public boolean isAdministrador() { return administrador; }
    public void setAdministrador(boolean administrador) { this.administrador = administrador; }
}
