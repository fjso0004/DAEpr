package es.ujaen.dae.sociosclub.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;

@Configuration
@EnableMethodSecurity
public class ServicioSeguridad {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.disable())
                .httpBasic(basic -> {})
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/sociosclub/usuarios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/sociosclub/usuarios/*").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/sociosclub/usuarios/*").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/sociosclub/temporadas").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/sociosclub/temporadas/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/sociosclub/actividades/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/sociosclub/actividades").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/sociosclub/temporadas/*/actividades").authenticated()

                        .requestMatchers(HttpMethod.POST, "/sociosclub/actividades/*/solicitudes").hasRole("USUARIO")
                        .requestMatchers(HttpMethod.GET, "/sociosclub/solicitudes/*").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/sociosclub/solicitudes/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/sociosclub/solicitudes/*").authenticated()

                        .requestMatchers(HttpMethod.GET, "/sociosclub/actividades/*/solicitudes").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/sociosclub/usuarios/*/pagoCuota").hasRole("ADMIN")

                        .anyRequest().authenticated()
                );
        return http.build();
    }
}