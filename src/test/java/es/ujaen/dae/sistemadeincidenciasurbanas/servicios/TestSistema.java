package es.ujaen.dae.sistemadeincidenciasurbanas.servicios;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.*;
import es.ujaen.dae.sistemadeincidenciasurbanas.excepciones.*;
import es.ujaen.dae.sistemadeincidenciasurbanas.util.LocalizacionGPS;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test") // Activamos el 'application-test.properties' (H2 y create-drop)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Limpiamos la bbdd despues de cada test
class TestSistema {

    @Autowired
    private Sistema sistema;


    //Test para Usuario

    @Test
    void registrarUsuarioExitosamente() {
        Usuario nuevoUsuario = new Usuario("Test", "Usuario", LocalDate.of(2004, 1, 1), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(nuevoUsuario);

        Optional<Usuario> optUsuario = sistema.iniciarSesion("usuariotest", "usuariotest1234");
        assertTrue(optUsuario.isPresent());

        Usuario usuario = optUsuario.get();
        assertEquals("usuariotest", usuario.login());
        assertEquals("usuariotest1234", usuario.claveAcceso());
    }

    @Test
    void registrarUsuarioLanzaExcepcionSiLoginDuplicado() {
        Usuario usuarioExistente = new Usuario("Test", "Usuario", LocalDate.of(2004, 1, 1), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuarioExistente);

        assertThrows(UsuarioYaExiste.class, () -> {
            Usuario usuarioDuplicado = new Usuario("Otro", "Usuario", LocalDate.of(2004, 2, 2), "Casa Otro Test", "611111111", "otro@test.com", "usuariotest", "otraclave1234");
            sistema.registrarUsuario(usuarioDuplicado);
        });
    }

    @Test
    void iniciarSesionCorrecto() {
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.of(2004, 1, 1), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuario);

        Optional<Usuario> optUsuario = sistema.iniciarSesion("usuariotest", "usuariotest1234");
        assertTrue(optUsuario.isPresent());

        Usuario usuarioIniciado = optUsuario.get();
        assertEquals("usuariotest", usuarioIniciado.login());
        assertEquals("usuariotest1234", usuarioIniciado.claveAcceso());
    }

    @Test
    void iniciarSesionIncorrecto() {
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.of(2004, 1, 1), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuario);

        Optional<Usuario> optUsuario = sistema.iniciarSesion("usuariotest", "claveErronea");
        assertTrue(optUsuario.isEmpty());
    }

    @Test
    void actualizarDatosUsuarioCorrectamente() {
        // Arrange
        Usuario nuevoUsuario = new Usuario("Test", "Usuario", LocalDate.of(2004, 1, 1), "Casa Vieja", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(nuevoUsuario);

        Usuario usuarioLogueado = sistema.iniciarSesion("usuariotest", "usuariotest1234").orElseThrow();

        usuarioLogueado.nombre("Nombre Nuevo");
        usuarioLogueado.direccion("Casa Nueva");
        sistema.actualizarDatosUsuario(usuarioLogueado);

        Usuario usuarioActualizado = sistema.iniciarSesion("usuariotest", "usuariotest1234").orElseThrow();
        assertEquals("Nombre Nuevo", usuarioActualizado.nombre());
        assertEquals("Casa Nueva", usuarioActualizado.direccion());
    }

    //Test para Incidencia

    @Test
    void crearIncidenciaCorrectamente() {
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.of(2004, 1, 1), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuario);

        Usuario admin = sistema.iniciarSesion("admin", "$2a$10$8CjaO.y0yCvi.HynDNAgZe2chWO3S2oOkQzwuQLCSVi1Jg4J7dlme").orElseThrow();
        sistema.addTipoIncidencia(new TipoIncidencia("Limpieza", "Suciedad en la vía pública"), admin);

        List<TipoIncidencia> tipos = sistema.listarTiposDeIncidencia();
        TipoIncidencia tipo = tipos.getFirst();

        Usuario usuarioLogueado = sistema.iniciarSesion("usuariotest", "usuariotest1234").orElseThrow();

        Incidencia nuevaIncidencia = new Incidencia();
        nuevaIncidencia.descripcion("Contenedor roto");
        nuevaIncidencia.localizacion("Calle Mayor");
        nuevaIncidencia.localizacionGPS(new LocalizacionGPS(10,10));
        nuevaIncidencia.tipoIncidencia(tipo);

        sistema.crearIncidencia(nuevaIncidencia, usuarioLogueado);

        List<Incidencia> incidenciasUsuario = sistema.listarIncidenciasDeUsuario(usuarioLogueado);
        assertEquals(1, incidenciasUsuario.size());
        Incidencia creada = incidenciasUsuario.getFirst();
        assertEquals("Contenedor roto", creada.descripcion());
        assertEquals(usuarioLogueado, creada.usuario()); // Comprobamos la relación
        assertEquals(tipo, creada.tipoIncidencia()); // Comprobamos la relación
    }

    @Test
    void usuarioNormalPuedeBorrarSuIncidenciaPendiente() {
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.of(2004, 1, 1), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuario);
        Usuario admin = sistema.iniciarSesion("admin", "$2a$10$8CjaO.y0yCvi.HynDNAgZe2chWO3S2oOkQzwuQLCSVi1Jg4J7dlme").orElseThrow();
        sistema.addTipoIncidencia(new TipoIncidencia("Varios", "Incidencias varias"), admin);
        List<TipoIncidencia> tipos = sistema.listarTiposDeIncidencia();
        TipoIncidencia tipo = tipos.getFirst();
        Usuario usuarioLogueado = sistema.iniciarSesion("usuariotest", "usuariotest1234").orElseThrow();
        Incidencia nuevaIncidencia = new Incidencia();
        nuevaIncidencia.descripcion("Test de borrado");
        nuevaIncidencia.localizacion("Calle Test");
        nuevaIncidencia.localizacionGPS(new LocalizacionGPS(10,10));
        nuevaIncidencia.tipoIncidencia(tipo);
        sistema.crearIncidencia(nuevaIncidencia, usuarioLogueado);
        Incidencia incidencia = sistema.listarIncidenciasDeUsuario(usuarioLogueado).getFirst();

        assertDoesNotThrow(() -> sistema.borrarIncidencia(usuarioLogueado, incidencia));
        assertTrue(sistema.listarIncidenciasDeUsuario(usuarioLogueado).isEmpty());
    }

    @Test
    void usuarioNormalNoPuedeBorrarIncidenciaAjena() {
        Usuario usuarioNormal1 = new Usuario("Test", "Usuario1", LocalDate.of(2004, 1, 1), "Casa 1", "611111111", "user1@test.com", "user1", "user1pass");
        Usuario usuarioNormal2 = new Usuario("Test", "Usuario2", LocalDate.of(2003, 2, 2), "Casa 2", "622222222", "user2@test.com", "user2", "user2pass");
        sistema.registrarUsuario(usuarioNormal1);
        sistema.registrarUsuario(usuarioNormal2);
        Usuario admin = sistema.iniciarSesion("admin", "$2a$10$8CjaO.y0yCvi.HynDNAgZe2chWO3S2oOkQzwuQLCSVi1Jg4J7dlme").orElseThrow();
        sistema.addTipoIncidencia(new TipoIncidencia("Mobiliario", "Mobiliario urbano dañado"), admin);
        List<TipoIncidencia> tipos = sistema.listarTiposDeIncidencia();
        TipoIncidencia tipo = tipos.getFirst();
        Usuario usuarioLogueado2 = sistema.iniciarSesion("user2", "user2pass").orElseThrow();
        Incidencia nuevaIncidencia = new Incidencia();
        nuevaIncidencia.descripcion("Incidencia de User2");
        nuevaIncidencia.localizacion("Plaza Central");
        nuevaIncidencia.localizacionGPS(new LocalizacionGPS(10,10));
        nuevaIncidencia.tipoIncidencia(tipo);
        sistema.crearIncidencia(nuevaIncidencia, usuarioLogueado2);
        Incidencia incidenciaDeOtro = sistema.listarIncidenciasDeUsuario(usuarioLogueado2).getFirst();
        Usuario usuarioLogueado1 = sistema.iniciarSesion("user1", "user1pass").orElseThrow();

        assertThrows(AccionNoAutorizada.class, () -> sistema.borrarIncidencia(usuarioLogueado1, incidenciaDeOtro));
    }

    @Test
    void administradorPuedeBorrarCualquierIncidencia() {
        Usuario admin = sistema.iniciarSesion("admin", "$2a$10$8CjaO.y0yCvi.HynDNAgZe2chWO3S2oOkQzwuQLCSVi1Jg4J7dlme").orElseThrow();
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.of(2004, 1, 1), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuario);
        sistema.addTipoIncidencia(new TipoIncidencia("Limpieza", "Suciedad"), admin);
        List<TipoIncidencia> tipos = sistema.listarTiposDeIncidencia();
        TipoIncidencia tipo = tipos.getFirst();
        Usuario usuarioLogueado = sistema.iniciarSesion("usuariotest", "usuariotest1234").orElseThrow();
        Incidencia nuevaIncidencia = new Incidencia();
        nuevaIncidencia.descripcion("Incidencia de Juan");
        nuevaIncidencia.localizacion("Test");
        nuevaIncidencia.localizacionGPS(new LocalizacionGPS(10,10));
        nuevaIncidencia.tipoIncidencia(tipo);
        sistema.crearIncidencia(nuevaIncidencia, usuarioLogueado);
        Incidencia incidencia = sistema.listarIncidenciasDeUsuario(usuarioLogueado).getFirst();
        incidencia.estadoIncidencia(EstadoIncidencia.RESUELTA); // La marcamos como resuelta

        assertDoesNotThrow(() -> sistema.borrarIncidencia(admin, incidencia));
        assertTrue(sistema.listarIncidenciasDeUsuario(usuarioLogueado).isEmpty());
    }

    @Test
    void administradorPuedeCambiarEstadoIncidencia() {
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.of(2004, 1, 1), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuario);
        Usuario admin = sistema.iniciarSesion("admin", "$2a$10$8CjaO.y0yCvi.HynDNAgZe2chWO3S2oOkQzwuQLCSVi1Jg4J7dlme").orElseThrow();
        sistema.addTipoIncidencia(new TipoIncidencia("Limpieza", "Suciedad"), admin);
        List<TipoIncidencia> tipos = sistema.listarTiposDeIncidencia();
        TipoIncidencia tipo = tipos.getFirst();
        Usuario usuarioLogueado = sistema.iniciarSesion("usuariotest", "usuariotest1234").orElseThrow();
        Incidencia nuevaIncidencia = new Incidencia();
        nuevaIncidencia.descripcion("Test");
        nuevaIncidencia.localizacion("Test");
        nuevaIncidencia.localizacionGPS(new LocalizacionGPS(10,10));
        nuevaIncidencia.tipoIncidencia(tipo);
        sistema.crearIncidencia(nuevaIncidencia, usuarioLogueado);
        Incidencia incidencia = sistema.listarIncidenciasDeUsuario(usuarioLogueado).getFirst();
    
        sistema.modificarEstadoIncidencia(incidencia, EstadoIncidencia.RESUELTA, admin);
    
        List<Incidencia> incidenciasResueltas = sistema.buscarIncidencias(tipo, EstadoIncidencia.RESUELTA);
        assertEquals(1, incidenciasResueltas.size());
        assertEquals(incidencia.id(), incidenciasResueltas.getFirst().id());
    }

    //Test para TipoIncidencia

    @Test
    void borrarTipoIncidenciaCorrectamente() {
        Usuario admin = sistema.iniciarSesion("admin", "$2a$10$8CjaO.y0yCvi.HynDNAgZe2chWO3S2oOkQzwuQLCSVi1Jg4J7dlme").orElseThrow();
        TipoIncidencia tipoBorrable = new TipoIncidencia("Borrable", "Se puede borrar");
        sistema.addTipoIncidencia(tipoBorrable, admin);

        assertEquals(1, sistema.listarTiposDeIncidencia().size());
        assertDoesNotThrow(() -> sistema.borrarTipoIncidencia(tipoBorrable.nombre(), admin));
        assertEquals(0, sistema.listarTiposDeIncidencia().size());
    }

    @Test
    void borrarTipoIncidenciaLanzaExcepcionSiEstaEnUso() {
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.of(2004, 1, 1), "Casa", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuario);
        Usuario admin = sistema.iniciarSesion("admin", "$2a$10$8CjaO.y0yCvi.HynDNAgZe2chWO3S2oOkQzwuQLCSVi1Jg4J7dlme").orElseThrow();
        TipoIncidencia tipoEnUso = new TipoIncidencia("EnUso", "No se puede borrar");
        sistema.addTipoIncidencia(tipoEnUso, admin);
        Usuario usuarioLogueado = sistema.iniciarSesion("usuariotest", "usuariotest1234").orElseThrow();
        Incidencia nuevaIncidencia = new Incidencia();
        nuevaIncidencia.descripcion("Test");
        nuevaIncidencia.localizacion("jaen");
        nuevaIncidencia.localizacionGPS(new LocalizacionGPS(10,10));
        nuevaIncidencia.tipoIncidencia(tipoEnUso);
        sistema.crearIncidencia(nuevaIncidencia, usuarioLogueado);
        assertThrows(TipoIncidenciaEnUso.class, () -> {
            sistema.borrarTipoIncidencia(tipoEnUso.nombre(), admin);
        });
        assertEquals(1, sistema.listarTiposDeIncidencia().size());
    }

    @Test
    public void guardarIncidenciaConFoto() throws Exception {

        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.of(2004, 1, 1), "Casa", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuario);

        Usuario admin = sistema.iniciarSesion("admin", "$2a$10$8CjaO.y0yCvi.HynDNAgZe2chWO3S2oOkQzwuQLCSVi1Jg4J7dlme").orElseThrow();
        sistema.addTipoIncidencia(new TipoIncidencia("Limpieza", "Suciedad en la vía pública"), admin);

        List<TipoIncidencia> tipos = sistema.listarTiposDeIncidencia();
        TipoIncidencia tipo = tipos.getFirst();

        Usuario usuarioLogueado = sistema.iniciarSesion("usuariotest", "usuariotest1234").orElseThrow();

        Incidencia nuevaIncidencia = new Incidencia();
        nuevaIncidencia.descripcion("Contenedor roto");
        nuevaIncidencia.localizacion("Calle Mayor");
        nuevaIncidencia.localizacionGPS(new LocalizacionGPS(10, 10));
        nuevaIncidencia.tipoIncidencia(tipo);

        Path imagen = Path.of("ClubDeSocios-Diagrama_de_Clases_Sistema_de_Incidencia.png");
        byte[] contenidoImagen = Files.readAllBytes(imagen);
        nuevaIncidencia.foto(contenidoImagen);

        sistema.crearIncidencia(nuevaIncidencia, usuarioLogueado);

        List<Incidencia> incidenciasUsuario = sistema.listarIncidenciasDeUsuario(usuarioLogueado);
        assertEquals(1, incidenciasUsuario.size());

        Incidencia creada = incidenciasUsuario.getFirst();

        assertNotNull(creada.foto(), "La foto debe estar guardada en la incidencia");
        assertArrayEquals(contenidoImagen, creada.foto(), "La foto guardada debe coincidir con la foto original");
    }

    @Test
    public void incidenciaSimilaresUsuarioContinua(){
        Usuario usuario = new Usuario("Continua", "Usuario", LocalDate.of(2004, 1, 1), "Casa", "600000001", "usuario2@test.com", "usuariocontinua", "usuariotest1234");
        sistema.registrarUsuario(usuario);
        Usuario usuarioLogueado = sistema.iniciarSesion("usuariocontinua", "usuariotest1234").orElseThrow();

        Usuario admin = sistema.iniciarSesion("admin", "$2a$10$8CjaO.y0yCvi.HynDNAgZe2chWO3S2oOkQzwuQLCSVi1Jg4J7dlme").orElseThrow();
        sistema.addTipoIncidencia(new TipoIncidencia("Vandalismo", "Graffiti"), admin);

        TipoIncidencia tipo = sistema.listarTiposDeIncidencia().getFirst();

        Incidencia primera_incidencia = new Incidencia();
        primera_incidencia.descripcion("Pintada en pared");
        primera_incidencia.localizacion("Calle 1");
        primera_incidencia.localizacionGPS(new LocalizacionGPS(10,10));
        primera_incidencia.tipoIncidencia(tipo);
        sistema.crearIncidencia(primera_incidencia, usuarioLogueado);

        Incidencia segunda_incidencia = new Incidencia();
        segunda_incidencia.descripcion("Otra pintada");
        segunda_incidencia.localizacion("Calle 1");
        segunda_incidencia.localizacionGPS(new LocalizacionGPS(10,10));
        segunda_incidencia.tipoIncidencia(tipo);

        String input = "S\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        sistema.crearIncidencia(segunda_incidencia, usuarioLogueado);

        List<Incidencia> incidenciasUsuario = sistema.listarIncidenciasDeUsuario(usuarioLogueado);
        assertEquals(2, incidenciasUsuario.size());
    }

    //Test para incidencia cercanas

//    @Test
//    public void incidenciaSimilaresUsuarioNoContinua(){
//        Usuario usuario = new Usuario("NoContinua", "Usuario", LocalDate.of(2004, 1, 1), "Casa", "600000001", "usuario2@test.com", "usuarionocontinua", "usuariotest1234");
//        sistema.registrarUsuario(usuario);
//        Usuario usuarioLogueado = sistema.iniciarSesion("usuarionocontinua", "usuariotest1234").orElseThrow();
//
//        Usuario admin = sistema.iniciarSesion("admin", "$2a$10$8CjaO.y0yCvi.HynDNAgZe2chWO3S2oOkQzwuQLCSVi1Jg4J7dlme").orElseThrow();
//        sistema.addTipoIncidencia(new TipoIncidencia("Vandalismo", "Graffiti"), admin);
//
//        TipoIncidencia tipo = sistema.listarTiposDeIncidencia().getFirst();
//
//        Incidencia primera_incidencia = new Incidencia();
//        primera_incidencia.descripcion("Pintada en pared");
//        primera_incidencia.localizacion("Calle 1");
//        primera_incidencia.localizacionGPS(new LocalizacionGPS(10,10));
//        primera_incidencia.tipoIncidencia(tipo);
//        sistema.crearIncidencia(primera_incidencia, usuarioLogueado);
//
//        Incidencia segunda_incidencia = new Incidencia();
//        segunda_incidencia.descripcion("Otra pintada");
//        segunda_incidencia.localizacion("Calle 1");
//        segunda_incidencia.localizacionGPS(new LocalizacionGPS(10,10));
//        segunda_incidencia.tipoIncidencia(tipo);
//
//        String input = "N\n";
//        System.setIn(new ByteArrayInputStream(input.getBytes()));
//
//        sistema.crearIncidencia(segunda_incidencia, usuarioLogueado);
//
//        List<Incidencia> incidenciasUsuario = sistema.listarIncidenciasDeUsuario(usuarioLogueado);
//        assertEquals(1, incidenciasUsuario.size());
//    }
}