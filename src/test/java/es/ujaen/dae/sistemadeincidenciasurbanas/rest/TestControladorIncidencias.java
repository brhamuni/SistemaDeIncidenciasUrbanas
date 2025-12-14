package es.ujaen.dae.sistemadeincidenciasurbanas.rest;

import es.ujaen.dae.sistemadeincidenciasurbanas.app.SistemaDeIncidenciasUrbanasApplication;
import es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto.DUsuario;
import es.ujaen.dae.sistemadeincidenciasurbanas.servicios.Sistema;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;

import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest(classes = SistemaDeIncidenciasUrbanasApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
public class TestControladorIncidencias {

    @Autowired
    private Sistema sistema;

    @LocalServerPort
    int localPort;

    TestRestTemplate restTemplate;

    static HttpHeaders headerAutorizacion(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        return headers;
    }

    @PostConstruct
    void crearRestTemplate() {
        var builder = new RestTemplateBuilder().rootUri("http://localhost:" + localPort);
        restTemplate = new TestRestTemplate(builder);
    }


    /**
     * Test para verificar que el acceso sin token es rechazado.
     */
    @Test
    void testAccesoSinToken() {
        var respuesta = restTemplate.getForEntity("/incidencias", String.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testLoginCorrecto() {
        var nuevoUsuario = new DUsuario("usuariotest", "Usuario", "Test","usuario@test.com", "600000000","Casa Usuario Test","usuariotest1234");

        var respuesta = restTemplate.postForEntity(
                "/auth/login",
                nuevoUsuario,
                Void.class
        );

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

}