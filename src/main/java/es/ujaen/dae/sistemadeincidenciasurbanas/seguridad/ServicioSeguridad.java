package es.ujaen.dae.sistemadeincidenciasurbanas.seguridad;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class ServicioSeguridad {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration authConf) throws Exception {
        return authConf.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.disable())
                .addFilterAfter(new FiltroAutenticacionJwt(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.POST, "/incidencias/autenticacion").permitAll()
                        
                        .requestMatchers(HttpMethod.POST, "/incidencias/usuarios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/incidencias/usuarios/{login}")
                            .access(new WebExpressionAuthorizationManager(
                                "hasRole('ADMIN') or (hasRole('USER') and #login == principal)"))
                        .requestMatchers(HttpMethod.PUT, "/incidencias/usuarios/{login}")
                            .access(new WebExpressionAuthorizationManager(
                                "hasRole('USER') and #login == principal"))
                        
                        .requestMatchers(HttpMethod.GET, "/incidencias/tipos/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/incidencias/tipos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/incidencias/tipos/**").hasRole("ADMIN")
                        
                        .requestMatchers(HttpMethod.GET, "/incidencias/buscar").authenticated()
                        .requestMatchers(HttpMethod.POST, "/incidencias").authenticated()
                        .requestMatchers(HttpMethod.GET, "/incidencias").authenticated()
                        
                        .requestMatchers("/incidencias/**").authenticated()
                )
                .build();
    }
}
