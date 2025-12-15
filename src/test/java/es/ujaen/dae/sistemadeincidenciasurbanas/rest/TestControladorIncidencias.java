package es.ujaen.dae.sistemadeincidenciasurbanas.rest;

import es.ujaen.dae.sistemadeincidenciasurbanas.SistemaDeIncidenciasUrbanasApplication;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.EstadoIncidencia;

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

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;


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
    void testCrearYBorrarTipoIncidencia_UsuarioAdmin() {

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

        //Borro el tipo de incidencia
        RequestEntity<Void> peticionBorrar = RequestEntity
                .delete("/tipos/{nombreTipoIncidencia}", "Via Publica")
                .headers(headerAutorizacion(tokenAdmin))
                .build();

        ResponseEntity<Void> respuestaBorrar = restTemplate.exchange(peticionBorrar, Void.class);
        assertThat(respuestaBorrar.getStatusCode()).isEqualTo(HttpStatus.OK);
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
        String token = loginAdmin.getBody();
        DTipoIncidencia nuevoTipo = new DTipoIncidencia("Via Publica", "Arreglos de farolas y carreteras en la via publica" );

        var peticionCrearTipo = RequestEntity
                .post("/tipos")
                .headers(headerAutorizacion(token))
                .body(nuevoTipo);

        ResponseEntity<Void> respuestaTipo = restTemplate.exchange(peticionCrearTipo, Void.class);
        assertThat(respuestaTipo.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testCrearYBorrarIncidencia(){

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

        //Creo un usuario nuevo
        var dUsuario = new DUsuario("PedroBenito123", "miClAvE","pedrobeni1@gmail.com", "Pedro","Benito", LocalDate.of(2000, 1, 1),"Jaén Jaén","611225577" );
        var respuesta = restTemplate.postForEntity(
                "/usuarios",
                dUsuario,
                Void.class
        );
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        //Login con usuario nuevo
        var respuestaAutenticacion = restTemplate.postForEntity(
                "/autenticacion",
                new DAutenticacionUsuario(dUsuario.login(), dUsuario.claveAcceso()),
                String.class
        );
        assertThat(respuestaAutenticacion.getStatusCode()).isEqualTo(HttpStatus.OK);
        String tokenUsuario = respuestaAutenticacion.getBody();

        //Creo y envío la nueva incidencia
        DIncidencia nuevaIncidencia = new DIncidencia(
                0,
                null,
                "Farola rota en la avenida principal",
                "Avenida Principal, Jaén",
                new LocalizacionGPS(10,10),
                EstadoIncidencia.PENDIENTE,
                dUsuario.login(),
                nuevoTipo.nombre(),
                null
        );

        var peticionCrearIncidencia = RequestEntity
                .post("/creadas")
                .headers(headerAutorizacion(tokenUsuario))
                .body(nuevaIncidencia);

        ResponseEntity<DIncidencia> respuestaIncidencia = restTemplate.exchange(peticionCrearIncidencia, DIncidencia.class);
        assertThat(respuestaIncidencia.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        //Consulto las incidencias del usuario: PedroBenito123
        var reqListado = RequestEntity
                .get("/creadas")
                .headers(headerAutorizacion(tokenUsuario))
                .build();

        ResponseEntity<DIncidencia[]> respListado = restTemplate.exchange(reqListado, DIncidencia[].class);

        assertThat(respListado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respListado.getBody().length).isEqualTo(1);

        RequestEntity<Void> peticionBorrarIncidencia = RequestEntity
                .delete("/creadas/{id}", 1)
                .headers(headerAutorizacion(tokenUsuario))
                .build();

        ResponseEntity<Void> respBorrarIncidencia = restTemplate.exchange(peticionBorrarIncidencia, Void.class);
        assertThat(respBorrarIncidencia.getStatusCode()).isEqualTo(HttpStatus.OK);

        //Listo para comprobar que esta borrado, lenght tiene que ser igua a 0
        var reqListadoBorrado = RequestEntity
                .get("/creadas")
                .headers(headerAutorizacion(tokenUsuario))
                .build();

        ResponseEntity<DIncidencia[]> respListadoBorrado = restTemplate.exchange(reqListadoBorrado, DIncidencia[].class);

        assertThat(respListadoBorrado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respListadoBorrado.getBody().length).isEqualTo(0);

    }

    // Agregar estos tests a la clase TestControladorIncidencias existente

    @Test
    void testBorrarTipoSinAutenticacion() {
        ResponseEntity<String> loginAdmin = restTemplate.postForEntity(
            "/autenticacion",
            new DAutenticacionUsuario("admin", "admin1234"),
            String.class
        );
        String tokenAdmin = loginAdmin.getBody();
    
        DTipoIncidencia nuevoTipo = new DTipoIncidencia("TipoBorrar", "Descripción");
        var peticionCrear = RequestEntity
            .post("/tipos")
            .headers(headerAutorizacion(tokenAdmin))
            .body(nuevoTipo);
        restTemplate.exchange(peticionCrear, Void.class);
    
        RequestEntity<Void> peticionBorrar = RequestEntity
            .delete("/tipos/{nombreTipoIncidencia}", "TipoBorrar")
            .build();
    
        ResponseEntity<Void> respuesta = restTemplate.exchange(peticionBorrar, Void.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testCrearIncidenciaSinAutenticacion() {
        // Login admin y crear tipo
        ResponseEntity<String> loginAdmin = restTemplate.postForEntity(
            "/autenticacion",
            new DAutenticacionUsuario("admin", "admin1234"),
            String.class
        );
        String tokenAdmin = loginAdmin.getBody();
    
        DTipoIncidencia nuevoTipo = new DTipoIncidencia("Limpieza", "Basura");
        var peticionTipo = RequestEntity
            .post("/tipos")
            .headers(headerAutorizacion(tokenAdmin))
            .body(nuevoTipo);
        restTemplate.exchange(peticionTipo, Void.class);
    
        // Intentar crear incidencia sin autenticación
        DIncidencia nuevaIncidencia = new DIncidencia(
            0,
            null,
            "Basura acumulada",
            "Calle Principal",
            new LocalizacionGPS(10, 10),
            EstadoIncidencia.PENDIENTE,
            "usuariotest",
            "Limpieza",
            null
        );
    
        ResponseEntity<DIncidencia> respuesta = restTemplate.postForEntity(
            "/creadas",
            nuevaIncidencia,
            DIncidencia.class
        );
    
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testListarIncidenciasSinAutenticacion() {
        RequestEntity<Void> peticion = RequestEntity
            .get("/creadas")
            .build();
    
        ResponseEntity<DIncidencia[]> respuesta = restTemplate.exchange(peticion, DIncidencia[].class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testBorrarTipoQueNoExiste() {
        ResponseEntity<String> loginAdmin = restTemplate.postForEntity(
            "/autenticacion",
            new DAutenticacionUsuario("admin", "admin1234"),
            String.class
        );
        assertThat(loginAdmin.getStatusCode()).isEqualTo(HttpStatus.OK);
    
        String tokenAdmin = loginAdmin.getBody();
    
        RequestEntity<Void> peticionBorrar = RequestEntity
            .delete("/tipos/{nombreTipoIncidencia}", "TipoInexistente")
            .headers(headerAutorizacion(tokenAdmin))
            .build();
    
        ResponseEntity<Void> respuesta = restTemplate.exchange(peticionBorrar, Void.class);
    
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
        
    @Test
    void testUsuarioSoloPuedeVerSusPropiosDatos() {
        // Crear dos usuarios
        var usuario1 = new DUsuario("user1", "pass1", "user1@test.com", "User", "One",
                                LocalDate.of(2000, 1, 1), "Dir1", "611111111");
        var usuario2 = new DUsuario("user2", "pass2", "user2@test.com", "User", "Two",
                                LocalDate.of(2000, 2, 2), "Dir2", "622222222");
    
        restTemplate.postForEntity("/usuarios", usuario1, Void.class);
        restTemplate.postForEntity("/usuarios", usuario2, Void.class);
    
        // Usuario1 se autentica
        ResponseEntity<String> login1 = restTemplate.postForEntity(
            "/autenticacion",
            new DAutenticacionUsuario("user1", "pass1"),
            String.class
        );
        String token1 = login1.getBody();
    
        // Usuario1 intenta ver datos de Usuario2
        var peticion = RequestEntity
            .get("/usuarios/{login}", "user2")
            .headers(headerAutorizacion(token1))
            .build();
    
        ResponseEntity<DUsuario> respuesta = restTemplate.exchange(peticion, DUsuario.class);
    
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testAdminPuedeVerDatosDeOtrosUsuarios() {
        // Crear usuario normal
        var usuario = new DUsuario("normaluser", "pass", "normal@test.com", "Normal", "User",
                               LocalDate.of(2000, 1, 1), "Dir", "611111111");
        restTemplate.postForEntity("/usuarios", usuario, Void.class);
    
        // Admin se autentica
        ResponseEntity<String> loginAdmin = restTemplate.postForEntity(
            "/autenticacion",
            new DAutenticacionUsuario("admin", "admin1234"),
            String.class
        );
        String tokenAdmin = loginAdmin.getBody();
    
        // Admin obtiene datos del usuario normal
         var peticion = RequestEntity
            .get("/usuarios/{login}", "normaluser")
            .headers(headerAutorizacion(tokenAdmin))
            .build();
    
        ResponseEntity<DUsuario> respuesta = restTemplate.exchange(peticion, DUsuario.class);
    
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().login()).isEqualTo("normaluser");
    }
}