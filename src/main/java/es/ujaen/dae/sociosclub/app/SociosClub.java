package es.ujaen.dae.sociosclub.app;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(scanBasePackages={
        "es.ujaen.dae.sociosclub.servicios",
        "es.ujaen.dae.sociosclub.repositorios"
})
@EnableCaching
public class SociosClub {
    public static void main(String[] args) {
        SpringApplication.run(SociosClub.class);
    }
}
