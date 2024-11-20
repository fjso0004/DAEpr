package es.ujaen.dae.sociosclub.entidades;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class UsuarioTest {
    @Test
    void testUsuarioYClave() {
        var usuario = new Usuario("12345678Z", "Pedro", "Sánchez", "Calle Falsa", "611203025", "pedro@gmail.com", "miClaveSegura", true);

        assertThat(usuario.getDni()).isEqualTo("12345678Z");
        assertThat(usuario.getNombre()).isEqualTo("Pedro");
        assertThat(usuario.getEmail()).isEqualTo("pedro@gmail.com");

        assertThat(usuario.claveValida("miClaveSegura")).isTrue();
        assertThat(usuario.claveValida("claveIncorrecta")).isFalse();

        assertThat(usuario.getCuota()).isTrue();

        usuario.setCuota(false);
        assertThat(usuario.getCuota()).isFalse();
    }
}
