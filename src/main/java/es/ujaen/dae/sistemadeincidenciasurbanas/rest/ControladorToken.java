package es.ujaen.dae.sistemadeincidenciasurbanas.rest;

import es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto.DAutenticacionUsuario;
import es.ujaen.dae.sistemadeincidenciasurbanas.util.UtilJwt;
import java.util.Collections;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/incidencias")
public class ControladorToken {

    final AuthenticationManager authenticationManager;

    @Value("${tiempoExpiracionTokenJwtMin}")
    int tiempoExpiracionToken;

    public ControladorToken(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/autenticacion")
    public ResponseEntity<String> getToken(@RequestBody DAutenticacionUsuario loginClaveUsuario) {

        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginClaveUsuario.login(), loginClaveUsuario.clave())
            );
        }catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return ResponseEntity.ok(UtilJwt.crearToken(
                        loginClaveUsuario.login(),
                        Collections.singletonMap("roles", roles),
                        tiempoExpiracionToken)
        );
    }
}
