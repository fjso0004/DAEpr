package es.ujaen.dae.sociosclub.entidades;


import es.ujaen.dae.sociosclub.util.CodificadorMd5;
import es.ujaen.dae.sociosclub.util.ExprReg;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;

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

    public Usuario(){

    }

    public Usuario(String dni, String nombre, String apellidos, String direccion, String tlf, String email, String clave, boolean cuotaPagada){
        this.dni = dni;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.tlf = tlf;
        this.email = email;
        this.direccion = direccion;
        this.clave = clave;
        this.cuotaPagada= cuotaPagada;
    }

public boolean claveValida(String clave){
    return this.clave.equals(CodificadorMd5.codificar(clave));
}

public String getDni() {
    return dni;
}

public String getNombre() {
    return nombre;
}

public String getApellidos() {
    return apellidos;
}

public String getTlf() {
    return tlf;
}

public String getDireccion(){
    return direccion;
}

public String getEmail() {
    return email;
}

public String getClave() {
    return clave;
}

public Boolean getCuota() {
    return cuotaPagada;
}

public void setCuota(Boolean cuotaPagada){
    this.cuotaPagada = cuotaPagada;
}

}
