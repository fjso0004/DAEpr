package es.ujaen.dae.sociosclub.excepciones;

public class ClaveoUsuarioIncorrecto extends RuntimeException {
    public ClaveoUsuarioIncorrecto(String message) {
        super(message);
    }
}
