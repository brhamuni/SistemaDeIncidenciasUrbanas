package es.ujaen.dae.sistemadeincidenciasurbanas.seguridad;
import es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto.DAutenticacionUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.stream.Collectors;

@RestController
public class ControladorToken {

    @Autowired
    AuthenticationManager authenticationManager;

    @Value("${tiempoExpiracionTokenJwtMin}")
    int tiempoExpiracionToken;

    @PostMapping("/login")
    public ResponseEntity<String> getToken(@RequestBody DAutenticacionUsuario usuario) {

        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usuario.email(), usuario.clave()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return ResponseEntity.ok(
                UtilJwt.crearToken(usuario.email(), Collections.singletonMap("roles", roles), tiempoExpiracionToken)
        );
    }

}