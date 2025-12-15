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
                        .requestMatchers(HttpMethod.GET, "/incidencias/usuarios/{login}")
                            .access(new WebExpressionAuthorizationManager("hasRole('ADMIN') or (hasRole('USER') and #login == principal)"))

                        .requestMatchers("/incidencias/autenticacion").permitAll()

                        //Endpoints Crear, Borrar y Listar tipo incidencias.
                        .requestMatchers("/incidencias/tipos/**").hasRole("ADMIN")

                        //Endpoints Crear, Borrar y Listar incidencias.
                        .requestMatchers("incidencias/creadas/**").permitAll()

                        //Cualquier otra cosa.
                        .requestMatchers("/incidencias/**").permitAll()

                        //Endpoints de fotos
                        .requestMatchers(HttpMethod.POST, "/incidencias/*/foto").authenticated()
                        .requestMatchers(HttpMethod.GET, "/incidencias/*/foto").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/incidencias/*/foto").authenticated()
                )
                .build();
    }
}
