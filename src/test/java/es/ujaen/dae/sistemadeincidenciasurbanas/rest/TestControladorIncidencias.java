package es.ujaen.dae.sistemadeincidenciasurbanas.rest;

import es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto.DAutenticacionUsuario;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = es.ujaen.dae.sistemadeincidenciasurbanas.SistemaDeIncidenciasUrbanasApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
public class TestControladorIncidencias {

    @Autowired
    private TestRestTemplate restTemplate;

    static HttpHeaders headerAutorizacion(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        return headers;
    }

    /**
     * Test para verificar que el acceso sin token es rechazado.
     */
    @Test
    public void testAccesoSinToken_DebeFallar() {
        ResponseEntity<String> respuesta = restTemplate.getForEntity("/incidencias", String.class);
        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
    }

}