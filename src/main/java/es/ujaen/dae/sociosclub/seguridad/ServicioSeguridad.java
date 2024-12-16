package es.ujaen.dae.sociosclub.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;

@Configuration
public class ServicioSeguridad {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.disable())
                .httpBasic(httpBasic -> httpBasic.realmName("sociosclub"))
                .authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.GET, "/sociosclub/usuarios/{dni}")
                        .access(new WebExpressionAuthorizationManager("hasRole('ADMIN') or (hasRole('USUARIO') and #dni == principal.username)"))
                        .requestMatchers(HttpMethod.POST, "/sociosclub/actividades").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/sociosclub/solicitudes").hasRole("USUARIO")
                        .requestMatchers("/sociosclub/**").permitAll()
                )
                .build();
    }
}

