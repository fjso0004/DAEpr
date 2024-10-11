package entidades;

public class Usuario {

String nombre;
String apellidos;
String direccion;
String tlf;
String email;
String clave;
boolean cuotaPagada;


public Usuario(String nombre, String apellidos, String dirección, String tlf, String email, String clave, boolean cuotaPagada){
    this.nombre = nombre;
    this.apellidos = apellidos;
    this.direccion = direccion; 
    this.tlf = tlf;
    this.email = email;
    this.clave = clave;
    this.cuotaPagada= cuotaPagada;
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
