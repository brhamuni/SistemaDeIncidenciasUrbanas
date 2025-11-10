package es.ujaen.dae.sistemadeincidenciasurbanas.servicios;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.*;
import es.ujaen.dae.sistemadeincidenciasurbanas.excepciones.*;
import es.ujaen.dae.sistemadeincidenciasurbanas.util.LocalizacionGPS;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TestSistema {

    @Autowired
    private Sistema sistema;

    @Test
    @DirtiesContext
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
    @DirtiesContext
    void registrarUsuarioLanzaExcepcionSiLoginDuplicado() {
        Usuario usuarioExistente = new Usuario("Test", "Usuario", LocalDate.of(2004, 1, 1), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuarioExistente);

        assertThrows(UsuarioYaExiste.class, () -> {
            Usuario usuarioDuplicado = new Usuario("Otro", "Usuario", LocalDate.of(2004, 2, 2), "Casa Otro Test", "611111111", "otro@test.com", "usuariotest", "otraclave1234");
            sistema.registrarUsuario(usuarioDuplicado);
        });
    }

    @Test
    @DirtiesContext
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
    @DirtiesContext
    void iniciarSesionIncorrecto() {
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.of(2004, 1, 1), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuario);

        Optional<Usuario> optUsuario = sistema.iniciarSesion("usuariotest", "claveErronea");
        assertTrue(optUsuario.isEmpty());
    }


    @Test
    @DirtiesContext
    void crearIncidenciaCorrectamente() {
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.of(2004, 1, 1), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuario);

        Usuario admin = sistema.iniciarSesion("admin", "admin1234").orElseThrow();
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
    }

    @Test
    @DirtiesContext
    void usuarioNormalPuedeBorrarSuIncidenciaPendiente() {
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.of(2004, 1, 1), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuario);

        Usuario admin = sistema.iniciarSesion("admin", "admin1234").orElseThrow();
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
    @DirtiesContext
    void usuarioNormalNoPuedeBorrarIncidenciaAjena() {
        Usuario usuarioNormal1 = new Usuario("Test", "Usuario1", LocalDate.of(2004, 1, 1), "Casa 1", "611111111", "user1@test.com", "user1", "user1pass");
        Usuario usuarioNormal2 = new Usuario("Test", "Usuario2", LocalDate.of(2003, 2, 2), "Casa 2", "622222222", "user2@test.com", "user2", "user2pass");
        sistema.registrarUsuario(usuarioNormal1);
        sistema.registrarUsuario(usuarioNormal2);

        Usuario admin = sistema.iniciarSesion("admin", "admin1234").orElseThrow();
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
    @DirtiesContext
    void administradorPuedeBorrarCualquierIncidencia() {
        Usuario admin = sistema.iniciarSesion("admin", "admin1234").orElseThrow();

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
        incidencia.estadoIncidencia(EstadoIncidencia.RESUELTA);

        assertDoesNotThrow(() -> sistema.borrarIncidencia(admin, incidencia));
        assertTrue(sistema.listarIncidenciasDeUsuario(usuarioLogueado).isEmpty());
    }

    @Test
    @DirtiesContext
    void administradorPuedeCambiarEstadoIncidencia() {
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.of(2004, 1, 1), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuario);

        Usuario admin = sistema.iniciarSesion("admin", "admin1234").orElseThrow();
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
        int idIncidencia = incidencia.id();

        Incidencia paraModificar = new Incidencia();
        paraModificar.id(idIncidencia);
        paraModificar.estadoIncidencia(EstadoIncidencia.RESUELTA);

        sistema.modificarEstadoIncidencia(paraModificar, admin);

        List<Incidencia> incidenciasResueltas = sistema.buscarIncidencias(tipo, EstadoIncidencia.RESUELTA);
        assertEquals(1, incidenciasResueltas.size());
        assertEquals(idIncidencia, incidenciasResueltas.getFirst().id());
    }
}