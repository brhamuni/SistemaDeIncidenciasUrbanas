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

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.core.io.ByteArrayResource;

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

        //Borro la incidencia con id = 1
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

    @Test
    void testBorrarTipoSinAutenticacion() {

        //Login admin
        ResponseEntity<String> loginAdmin = restTemplate.postForEntity(
            "/autenticacion",
            new DAutenticacionUsuario("admin", "admin1234"),
            String.class
        );
        String tokenAdmin = loginAdmin.getBody();

        //Crear y enviar Tipo de incidencia
        DTipoIncidencia nuevoTipo = new DTipoIncidencia("TipoBorrar", "Descripción");
        var peticionCrear = RequestEntity
            .post("/tipos")
            .headers(headerAutorizacion(tokenAdmin))
            .body(nuevoTipo);

        //Borro la incidencia sin estar logueado
        RequestEntity<Void> peticionBorrar = RequestEntity
            .delete("/tipos/{nombreTipoIncidencia}", "TipoBorrar")
            .build();
    
        ResponseEntity<Void> respuesta = restTemplate.exchange(peticionBorrar, Void.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testCrearIncidenciaSinAutenticacion() {

        // Login admin
        ResponseEntity<String> loginAdmin = restTemplate.postForEntity(
            "/autenticacion",
            new DAutenticacionUsuario("admin", "admin1234"),
            String.class
        );
        String tokenAdmin = loginAdmin.getBody();

        //Crear y enviar Tipo de incidencia
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

        //Listo las incidencias sin autenticación.
        RequestEntity<Void> peticion = RequestEntity
            .get("/creadas")
            .build();
    
        ResponseEntity<DIncidencia[]> respuesta = restTemplate.exchange(peticion, DIncidencia[].class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testBorrarTipoQueNoExiste() {

        // Login admin
        ResponseEntity<String> loginAdmin = restTemplate.postForEntity(
            "/autenticacion",
            new DAutenticacionUsuario("admin", "admin1234"),
            String.class
        );
        String tokenAdmin = loginAdmin.getBody();

        //Borro el tipo de incidencia que no existe.
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
        var usuario1 = new DUsuario("user1", "pass1", "user1@test.com", "User", "One", LocalDate.of(2000, 1, 1), "Dir1", "611111111");
        var usuario2 = new DUsuario("user2", "pass2", "user2@test.com", "User", "Two", LocalDate.of(2000, 2, 2), "Dir2", "622222222");
    
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
        var usuario = new DUsuario("normaluser", "pass", "normal@test.com", "Normal", "User", LocalDate.of(2000, 1, 1), "Dir", "611111111");
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

    }

    @Test
    public void testSubirFoto() {
        // Crear usuario
        var usuario = new DUsuario("PedroBenito123", "miClAvE", 
                "pedrobeni1@gmail.com", "Pedro", "Benito", 
                LocalDate.of(2000, 1, 1), "Jaén Jaén", "611225577");
        restTemplate.postForEntity("/usuarios", usuario, Void.class);
        
        // Autenticar usuario
        var respuestaAuth = restTemplate.postForEntity("/autenticacion",
                new DAutenticacionUsuario(usuario.login(), usuario.claveAcceso()),
                String.class);
        String token = respuestaAuth.getBody();
        
        // Crear tipo de incidencia como admin
        var respuestaAuthAdmin = restTemplate.postForEntity("/autenticacion",
                new DAutenticacionUsuario("admin", "admin1234"),
                String.class);
        String tokenAdmin = respuestaAuthAdmin.getBody();
        
        var tipoDto = new DTipoIncidencia("Limpieza", "Problemas de limpieza");
        var headers = headerAutorizacion(tokenAdmin);
        var peticionTipo = new HttpEntity<>(tipoDto, headers);
        restTemplate.exchange("/tipos", HttpMethod.POST, peticionTipo, Void.class);
        
        // Obtener el tipo creado
        TipoIncidencia tipo = sistema.listarTiposDeIncidencia().get(0);
        Usuario usuarioEntity = sistema.buscarUsuario(usuario.login()).get();
        
        // Crear incidencia
        var incidenciaDto = new DIncidencia(
                0, null, "Basura acumulada", "Calle Mayor", 
                new LocalizacionGPS(10, 10), null, 
                usuarioEntity, tipo, null);
        
        var headersUser = headerAutorizacion(token);
        var peticionIncidencia = new HttpEntity<>(incidenciaDto, headersUser);
        var respuestaIncidencia = restTemplate.exchange(
                "/", HttpMethod.POST, peticionIncidencia, DIncidencia.class);
        
        assertThat(respuestaIncidencia.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        int idIncidencia = respuestaIncidencia.getBody().id();
        
        // Preparar foto (imagen de prueba de 100 bytes)
        byte[] fotoBytes = new byte[100];
        for (int i = 0; i < fotoBytes.length; i++) {
            fotoBytes[i] = (byte) i;
        }
        
        // Subir foto
        HttpHeaders headersMultipart = headerAutorizacion(token);
        headersMultipart.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("foto", new ByteArrayResource(fotoBytes) {
            @Override
            public String getFilename() {
                return "test.jpg";
            }
        });
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = 
                new HttpEntity<>(body, headersMultipart);
        
        var respuestaFoto = restTemplate.exchange(
                "/{id}/foto", HttpMethod.POST, requestEntity, 
                Void.class, idIncidencia);
        
        assertThat(respuestaFoto.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testSubirFotoSinAutenticacion() {
        byte[] imagenPrueba = crearImagenPrueba();
        
        var body = new org.springframework.util.LinkedMultiValueMap<String, Object>();
        body.add("foto", new org.springframework.core.io.ByteArrayResource(imagenPrueba) {
            @Override
            public String getFilename() {
                return "test.png";
            }
        });

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        var request = new HttpEntity<>(body, headers);

        ResponseEntity<Void> respuesta = restTemplate.postForEntity(
            "/incidencias/1/foto",
            request,
            Void.class
        );

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testSubirFotoAIncidenciaAjena() {
        // Crear dos usuarios
        var usuario1 = new DUsuario("user1foto", "pass1", "user1@foto.com", "User", "One",
                                    LocalDate.of(2000, 1, 1), "Casa 1", "611111111");
        var usuario2 = new DUsuario("user2foto", "pass2", "user2@foto.com", "User", "Two",
                                    LocalDate.of(2000, 2, 2), "Casa 2", "622222222");
        
        restTemplate.postForEntity("/usuarios", usuario1, Void.class);
        restTemplate.postForEntity("/usuarios", usuario2, Void.class);

        // Login usuario1
        ResponseEntity<String> login1 = restTemplate.postForEntity(
            "/autenticacion",
            new DAutenticacionUsuario(usuario1.login(), usuario1.claveAcceso()),
            String.class
        );
        String token1 = login1.getBody();

        // Login usuario2
        ResponseEntity<String> login2 = restTemplate.postForEntity(
            "/autenticacion",
            new DAutenticacionUsuario(usuario2.login(), usuario2.claveAcceso()),
            String.class
        );
        String token2 = login2.getBody();

        // Admin crea tipo
        ResponseEntity<String> loginAdmin = restTemplate.postForEntity(
            "/autenticacion",
            new DAutenticacionUsuario("admin", "admin1234"),
            String.class
        );
        String tokenAdmin = loginAdmin.getBody();
        
        DTipoIncidencia tipo = new DTipoIncidencia("Alumbrado", "Problemas de luz");
        var reqTipo = RequestEntity.post("/tipos")
            .headers(headerAutorizacion(tokenAdmin))
            .body(tipo);
        restTemplate.exchange(reqTipo, Void.class);

        // Usuario1 crea incidencia
        DIncidencia incidencia = new DIncidencia(0, null, "Farola fundida", "Calle Mayor",
            new LocalizacionGPS(10, 10), EstadoIncidencia.PENDIENTE, usuario1.login(),
            tipo.nombre(), null);
        
        var reqIncidencia = RequestEntity.post("/creadas")
            .headers(headerAutorizacion(token1))
            .body(incidencia);
        ResponseEntity<DIncidencia> respIncidencia = restTemplate.exchange(reqIncidencia, DIncidencia.class);
        int idIncidencia = respIncidencia.getBody().id();

        // Usuario2 intenta subir foto a incidencia de usuario1
        byte[] imagenPrueba = crearImagenPrueba();
        
        var body = new org.springframework.util.LinkedMultiValueMap<String, Object>();
        body.add("foto", new org.springframework.core.io.ByteArrayResource(imagenPrueba) {
            @Override
            public String getFilename() {
                return "test.png";
            }
        });

        var headers = headerAutorizacion(token2);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        var request = new HttpEntity<>(body, headers);

        ResponseEntity<Void> respuesta = restTemplate.postForEntity(
            "/incidencias/" + idIncidencia + "/foto",
            request,
            Void.class
        );

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void testDescargarFotoExitosamente() {
        // Crear usuario
        var usuario = new DUsuario("userDesc", "pass", "desc@test.com", "Desc", "User",
                                   LocalDate.of(2000, 1, 1), "Casa", "611111111");
        restTemplate.postForEntity("/usuarios", usuario, Void.class);
        
        ResponseEntity<String> login = restTemplate.postForEntity(
            "/autenticacion",
            new DAutenticacionUsuario(usuario.login(), usuario.claveAcceso()),
            String.class
        );
        String token = login.getBody();

        // Admin crea tipo
        ResponseEntity<String> loginAdmin = restTemplate.postForEntity(
            "/autenticacion",
            new DAutenticacionUsuario("admin", "admin1234"),
            String.class
        );
        String tokenAdmin = loginAdmin.getBody();
        
        DTipoIncidencia tipo = new DTipoIncidencia("Limpieza", "Basura");
        var reqTipo = RequestEntity.post("/tipos")
            .headers(headerAutorizacion(tokenAdmin))
            .body(tipo);
        restTemplate.exchange(reqTipo, Void.class);

        // Crear incidencia
        DIncidencia incidencia = new DIncidencia(0, null, "Basura acumulada", "Plaza",
            new LocalizacionGPS(10, 10), EstadoIncidencia.PENDIENTE, usuario.login(),
            tipo.nombre(), null);
        
        var reqIncidencia = RequestEntity.post("/creadas")
            .headers(headerAutorizacion(token))
            .body(incidencia);
        ResponseEntity<DIncidencia> respIncidencia = restTemplate.exchange(reqIncidencia, DIncidencia.class);
        int idIncidencia = respIncidencia.getBody().id();

        // Subir foto
        byte[] imagenOriginal = crearImagenPrueba();
        var body = new org.springframework.util.LinkedMultiValueMap<String, Object>();
        body.add("foto", new org.springframework.core.io.ByteArrayResource(imagenOriginal) {
            @Override
            public String getFilename() {
                return "test.png";
            }
        });

        var headersUpload = headerAutorizacion(token);
        headersUpload.setContentType(MediaType.MULTIPART_FORM_DATA);
        var requestUpload = new HttpEntity<>(body, headersUpload);
        restTemplate.postForEntity("/incidencias/" + idIncidencia + "/foto", requestUpload, Void.class);

        // Descargar foto
        var reqDescargar = RequestEntity
            .get("/incidencias/{id}/foto", idIncidencia)
            .headers(headerAutorizacion(token))
            .build();

        ResponseEntity<byte[]> respuesta = restTemplate.exchange(reqDescargar, byte[].class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_JPEG);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().length).isGreaterThan(0);
    }

    @Test
    void testEliminarFotoExitosamente() {
        var usuario = new DUsuario("userDel", "pass", "del@test.com", "Del", "User",
                                   LocalDate.of(2000, 1, 1), "Casa", "611111111");
        restTemplate.postForEntity("/usuarios", usuario, Void.class);
        
        ResponseEntity<String> login = restTemplate.postForEntity(
            "/autenticacion",
            new DAutenticacionUsuario(usuario.login(), usuario.claveAcceso()),
            String.class
        );
        String token = login.getBody();

        // Admin crea tipo
        ResponseEntity<String> loginAdmin = restTemplate.postForEntity(
            "/autenticacion",
            new DAutenticacionUsuario("admin", "admin1234"),
            String.class
        );
        String tokenAdmin = loginAdmin.getBody();
        
        DTipoIncidencia tipo = new DTipoIncidencia("Parques", "Parques y jardines");
        var reqTipo = RequestEntity.post("/tipos")
            .headers(headerAutorizacion(tokenAdmin))
            .body(tipo);
        restTemplate.exchange(reqTipo, Void.class);

        // Crear incidencia
        DIncidencia incidencia = new DIncidencia(0, null, "Árbol caído", "Parque",
            new LocalizacionGPS(10, 10), EstadoIncidencia.PENDIENTE, usuario.login(),
            tipo.nombre(), null);
        
        var reqIncidencia = RequestEntity.post("/creadas")
            .headers(headerAutorizacion(token))
            .body(incidencia);
        ResponseEntity<DIncidencia> respIncidencia = restTemplate.exchange(reqIncidencia, DIncidencia.class);
        int idIncidencia = respIncidencia.getBody().id();

        // Subir foto
        byte[] imagen = crearImagenPrueba();
        var body = new org.springframework.util.LinkedMultiValueMap<String, Object>();
        body.add("foto", new org.springframework.core.io.ByteArrayResource(imagen) {
            @Override
            public String getFilename() {
                return "test.png";
            }
        });

        var headersUpload = headerAutorizacion(token);
        headersUpload.setContentType(MediaType.MULTIPART_FORM_DATA);
        var requestUpload = new HttpEntity<>(body, headersUpload);
        restTemplate.postForEntity("/incidencias/" + idIncidencia + "/foto", requestUpload, Void.class);

        // Eliminar foto
        var reqEliminar = RequestEntity
            .delete("/incidencias/{id}/foto", idIncidencia)
            .headers(headerAutorizacion(token))
            .build();

        ResponseEntity<Void> respuesta = restTemplate.exchange(reqEliminar, Void.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verificar que ya no existe
        var reqDescargar = RequestEntity
            .get("/incidencias/{id}/foto", idIncidencia)
            .headers(headerAutorizacion(token))
            .build();

        ResponseEntity<byte[]> respDescarga = restTemplate.exchange(reqDescargar, byte[].class);
        assertThat(respDescarga.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testAdminPuedeEliminarCualquierFoto() {
        var usuario = new DUsuario("userAdmin", "pass", "useradmin@test.com", "User", "Admin",
                                   LocalDate.of(2000, 1, 1), "Casa", "611111111");
        restTemplate.postForEntity("/usuarios", usuario, Void.class);
        
        ResponseEntity<String> login = restTemplate.postForEntity(
            "/autenticacion",
            new DAutenticacionUsuario(usuario.login(), usuario.claveAcceso()),
            String.class
        );
        String token = login.getBody();

        // Admin
        ResponseEntity<String> loginAdmin = restTemplate.postForEntity(
            "/autenticacion",
            new DAutenticacionUsuario("admin", "admin1234"),
            String.class
        );
        String tokenAdmin = loginAdmin.getBody();
        
        DTipoIncidencia tipo = new DTipoIncidencia("Residuos", "Gestión de residuos");
        var reqTipo = RequestEntity.post("/tipos")
            .headers(headerAutorizacion(tokenAdmin))
            .body(tipo);
        restTemplate.exchange(reqTipo, Void.class);

        // Usuario crea incidencia
        DIncidencia incidencia = new DIncidencia(0, null, "Contenedor lleno", "Calle",
            new LocalizacionGPS(10, 10), EstadoIncidencia.PENDIENTE, usuario.login(),
            tipo.nombre(), null);
        
        var reqIncidencia = RequestEntity.post("/creadas")
            .headers(headerAutorizacion(token))
            .body(incidencia);
        ResponseEntity<DIncidencia> respIncidencia = restTemplate.exchange(reqIncidencia, DIncidencia.class);
        int idIncidencia = respIncidencia.getBody().id();

        // Usuario sube foto
        byte[] imagen = crearImagenPrueba();
        var body = new org.springframework.util.LinkedMultiValueMap<String, Object>();
        body.add("foto", new org.springframework.core.io.ByteArrayResource(imagen) {
            @Override
            public String getFilename() {
                return "test.png";
            }
        });

        var headersUpload = headerAutorizacion(token);
        headersUpload.setContentType(MediaType.MULTIPART_FORM_DATA);
        var requestUpload = new HttpEntity<>(body, headersUpload);
        restTemplate.postForEntity("/incidencias/" + idIncidencia + "/foto", requestUpload, Void.class);

        // Admin elimina foto
        var reqEliminar = RequestEntity
            .delete("/incidencias/{id}/foto", idIncidencia)
            .headers(headerAutorizacion(tokenAdmin))
            .build();

        ResponseEntity<Void> respuesta = restTemplate.exchange(reqEliminar, Void.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    //Funcion auxiliar
    private byte[] crearImagenPrueba() {
        return new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
            (byte) 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54,
            0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05,
            0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00, 0x00,
            0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42,
            0x60, (byte) 0x82
        };
    }
}