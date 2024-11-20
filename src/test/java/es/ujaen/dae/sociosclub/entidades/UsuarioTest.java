package es.ujaen.dae.sociosclub.entidades;

import es.ujaen.dae.sociosclub.util.CodificadorMd5;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    private Usuario usuario;

    @BeforeEach
    void configInicial() {
        usuario = new Usuario("12345678A", "Juan", "Pérez", "Calle Falsa 123", "600123456",
                              "juan@ejemplo.com", "password123", false);
    }

    @Test
    void testCrearUsuario() {
        assertEquals("12345678A", usuario.getDni());
        assertEquals("Juan", usuario.getNombre());
        assertEquals("Pérez", usuario.getApellidos());
        assertEquals("Calle Falsa 123", usuario.getDireccion());
        assertEquals("600123456", usuario.getTlf());
        assertEquals("juan@ejemplo.com", usuario.getEmail());
        assertEquals("password123", usuario.getClave());
        assertFalse(usuario.getCuota(), "La cuota debería estar sin pagar inicialmente.");
    }

    @Test
    void testModificarCuota() {
        usuario.setCuota(true);
        assertTrue(usuario.getCuota(), "La cuota debería estar pagada después de modificarla.");
    }

    @Test
    void testClaveValida() {
        
        String claveCodificada = CodificadorMd5.codificar("password123");
        usuario.clave = claveCodificada; 
        assertTrue(usuario.claveValida("password123"), "La clave debería ser válida.");
        assertFalse(usuario.claveValida("otraClave"), "La clave debería ser inválida.");
    }

    @Test
    void testModificarClave() {
        usuario.clave = "nuevaClave123";
        assertEquals("nuevaClave123", usuario.getClave(), "La clave debería actualizarse correctamente.");
    }

    @Test
    void testModificarDireccion() {
        usuario.direccion = "Nueva Direccion 456";
        assertEquals("Nueva Direccion 456", usuario.getDireccion(), "La dirección debería actualizarse correctamente.");
    }
}
