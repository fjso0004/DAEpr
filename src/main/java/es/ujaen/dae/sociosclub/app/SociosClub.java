package es.ujaen.dae.sociosclub.app;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages={
        "es.ujaen.dae.sociosclub.servicios",
        "es.ujaen.dae.sociosclub.repositorios",
        "es.ujaen.dae.sociosclub.rest",
        "es.ujaen.dae.sociosclub.seguridad"
})
@EntityScan(basePackages="es.ujaen.dae.sociosclub.entidades")
@EnableCaching
public class SociosClub {
    public static void main(String[] args) {
        SpringApplication.run(SociosClub.class);
    }
}
