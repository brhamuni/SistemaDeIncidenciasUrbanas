package es.ujaen.dae.sistemadeincidenciasurbanas.seguridad;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class ServicioSeguridad {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.disable())
                .addFilterAfter(new FiltroAutenticacionJwt(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/incidencias/admin/**").hasRole("ADMIN")
                        .requestMatchers("/incidencias/**").hasRole("USER")
                )
                .build();
    }
}
