package es.ujaen.dae.sistemadeincidenciasurbanas.rest;

import es.ujaen.dae.sistemadeincidenciasurbanas.SistemaDeIncidenciasUrbanasApplication;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.EstadoIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.TipoIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.Usuario;
import es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto.*;
import es.ujaen.dae.sistemadeincidenciasurbanas.servicios.Sistema;
import es.ujaen.dae.sistemadeincidenciasurbanas.util.LocalizacionGPS;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest(classes = SistemaDeIncidenciasUrbanasApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TestControladorIncidencias {

    @Autowired
    private Sistema sistema;

    @LocalServerPort
    int localPort;

    TestRestTemplate restTemplate;
    @Autowired
    private Mapeador mapeador;
    @Autowired
    private PasswordEncoder passwordEncoder;

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
     * Intento de creación de un cliente válido
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

    @Test
    void testLoginCliente() {
        var usuario = new DUsuario("PedroBenito123", "miClAvE","pedrobeni1@gmail.com", "Pedro","Benito", LocalDate.of(2000, 1, 1),"Jaén Jaén","611225577" );
        var respuesta = restTemplate.postForEntity(
                "/usuarios",
                usuario,
                Void.class
        );
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Email inexistente
        var respuestaAutenticacion = restTemplate.postForEntity(
                "/autenticacion",
                new DAutenticacionUsuario("xyz", usuario.claveAcceso()),
                String.class
        );
        assertThat(respuestaAutenticacion.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Clave incorrecta
        respuestaAutenticacion = restTemplate.postForEntity(
                "/autenticacion",
                new DAutenticacionUsuario(usuario.login(), "xyz"),
                String.class
        );
        assertThat(respuestaAutenticacion.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Login correcto
        respuestaAutenticacion = restTemplate.postForEntity(
                "/autenticacion",
                new DAutenticacionUsuario(usuario.login(), usuario.claveAcceso()),
                String.class
        );
        assertThat(respuestaAutenticacion.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Obtener información de usuario
        String token = respuestaAutenticacion.getBody();
        var peticionUsuario = RequestEntity
                .get("/usuarios/{login}", usuario.login())
                .headers(headerAutorizacion(token))
                .build();

        var respuestaUsuario = restTemplate.exchange(peticionUsuario, DUsuario.class);

        assertThat(respuestaUsuario.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuestaUsuario.getBody().login()).isEqualTo(usuario.login());
    }

    @Test
    void testCrearTipoIncidencia_UsuarioAdmin() {

        //Login admin
        ResponseEntity<String> loginAdmin = restTemplate.postForEntity(
                "/autenticacion",
                new DAutenticacionUsuario("admin", "admin1234"),
                String.class
        );
        assertThat(loginAdmin.getStatusCode()).isEqualTo(HttpStatus.OK);

        //Crear y enviar Tipo de incidencia
        String tokenAdmin = loginAdmin.getBody();
        DTipoIncidencia nuevoTipo = new DTipoIncidencia("Via Publica", "Arreglos de farolas y carreteras en la via publica" );

        var peticionCrearTipo = RequestEntity
                .post("/tipos")
                .headers(headerAutorizacion(tokenAdmin))
                .body(nuevoTipo);

        ResponseEntity<Void> respuestaTipo = restTemplate.exchange(peticionCrearTipo, Void.class);
        assertThat(respuestaTipo.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void testCrearTipoIncidencia_UsuarioNoAdmin() {
        //Creo un usuario no admin
        var usuario = new DUsuario("PedroBenito123", "miClAvE","pedrobeni1@gmail.com", "Pedro","Benito", LocalDate.of(2000, 1, 1),"Jaén Jaén","611225577" );
        var respuesta = restTemplate.postForEntity(
                "/usuarios",
                usuario,
                Void.class
        );
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        //Login no admin
        ResponseEntity<String> loginAdmin = restTemplate.postForEntity(
                "/autenticacion",
                new DAutenticacionUsuario("PedroBenito123", "miClAvE"),
                String.class
        );
        assertThat(loginAdmin.getStatusCode()).isEqualTo(HttpStatus.OK);

        //Crear y enviar Tipo de incidencia
        String tokenAdmin = loginAdmin.getBody();
        DTipoIncidencia nuevoTipo = new DTipoIncidencia("Via Publica", "Arreglos de farolas y carreteras en la via publica" );

        var peticionCrearTipo = RequestEntity
                .post("/tipos")
                .headers(headerAutorizacion(tokenAdmin))
                .body(nuevoTipo);

        ResponseEntity<Void> respuestaTipo = restTemplate.exchange(peticionCrearTipo, Void.class);
        assertThat(respuestaTipo.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

}