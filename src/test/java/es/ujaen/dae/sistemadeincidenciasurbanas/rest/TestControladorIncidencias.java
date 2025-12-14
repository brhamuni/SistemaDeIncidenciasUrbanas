package es.ujaen.dae.sistemadeincidenciasurbanas.rest;

import es.ujaen.dae.sistemadeincidenciasurbanas.SistemaDeIncidenciasUrbanasApplication;
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

import java.time.LocalDate;

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
        var builder = new RestTemplateBuilder().rootUri("http://localhost:" + localPort + "/incidencias");
        restTemplate = new TestRestTemplate(builder);
    }


    /**
     * Intento de creación de un cliente inválido
     */
    @Test
    public void testNuevoClienteInvalido() {

        var usuario = new DUsuario("PedroBenito123", "miClAvE","pedrobeni1gmail.com", "Pedro","Benito", LocalDate.of(2000, 1, 1),"Jaén Jaén","6115577" );
        var respuesta = restTemplate.postForEntity(
                "/usuarios",
                usuario,
                Void.class
        );
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * Intento de creación de un cliente inválido
     */
    @Test
    public void testNuevoClienteValido() {

        var usuario = new DUsuario("PedroBenito123", "miClAvE","pedrobeni1@gmail.com", "Pedro","Benito", LocalDate.of(2000, 1, 1),"Jaén Jaén","611225577" );
        var respuesta = restTemplate.postForEntity(
                "/usuarios",
                usuario,
                Void.class
        );
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    /**
     * Intento de creación de un cliente duplicado
     */
    @Test
    public void testClienteDuplicado() {

        var usuario = new DUsuario("PedroBenito123", "miClAvE","pedrobeni1@gmail.com", "Pedro","Benito", LocalDate.of(2000, 1, 1),"Jaén Jaén","611225577" );
        var respuesta = restTemplate.postForEntity(
                "/usuarios",
                usuario,
                Void.class
        );
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        respuesta = restTemplate.postForEntity(
                "/usuarios",
                usuario,
                Void.class
        );
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

    }




}