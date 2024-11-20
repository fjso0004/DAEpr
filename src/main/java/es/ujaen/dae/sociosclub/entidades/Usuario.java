package es.ujaen.dae.sociosclub.entidades;

import jakarta.persistence.*;
import es.ujaen.dae.sociosclub.util.CodificadorMd5;
import es.ujaen.dae.sociosclub.util.ExprReg;
import jakarta.persistence.CascadeType;
import jakarta.validation.constraints.*;

import java.beans.Expression;
import java.util.ArrayList;
import java.util.List;

public class Usuario {

    @Id
    @Pattern(regexp = ExprReg.DNI)
    private String dni;

    @NotBlank
    private String nombre;

    @NotBlank
    private String apellidos;

    private String direccion;

    private String tlf;
    private String email;

    @Size(min = 8)
    private String clave;

    boolean cuotaPagada;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Solicitudes> solicitudes = new ArrayList<>();


public Usuario(String dni, String nombre, String apellidos, String dirección, String tlf, String email, String clave, boolean cuotaPagada){
    this.dni = dni;
    this.nombre = nombre;
    this.apellidos = apellidos;
    this.tlf = tlf;
    this.email = email;
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
