package es.ujaen.dae.sistemadeincidenciasurbanas.servicios;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.*;
import es.ujaen.dae.sistemadeincidenciasurbanas.excepciones.*;
import es.ujaen.dae.sistemadeincidenciasurbanas.util.LocalizacionGPS;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TestSistema {

    @Autowired
    private Sistema sistema;

    @Test
    void registrarUsuarioExitosamente() {
        Usuario nuevoUsuario = new Usuario("Test", "Usuario", LocalDate.now(), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(nuevoUsuario);
        Usuario usuario = sistema.iniciarSesion("usuariotest", "usuariotest1234");
        assertNotNull(usuario);
        assertEquals("usuariotest", usuario.login());
        assertEquals("usuariotest1234", usuario.claveAcceso());
    }

    @Test
    void registrarUsuarioLanzaExcepcionSiLoginDuplicado() {
        Usuario usuarioExistente = new Usuario("Test", "Usuario", LocalDate.now(), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuarioExistente);

        assertThrows(UsuarioYaExiste.class, () -> {
            Usuario usuarioDuplicado = new Usuario("Otro", "Usuario", LocalDate.now(), "Casa Otro Test", "611111111", "otro@test.com", "usuariotest", "otraclave1234");
            sistema.registrarUsuario(usuarioDuplicado);
        });
    }

    @Test
    void iniciarSesionCorrecto() {
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.now(), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuario);
        Usuario usuarioIniciado = sistema.iniciarSesion("usuariotest", "usuariotest1234");

        assertNotNull(usuarioIniciado);
        assertEquals("usuariotest", usuarioIniciado.login());
        assertEquals("usuariotest1234", usuarioIniciado.claveAcceso());
    }

    @Test
    void iniciarSesionIncorrecto() {
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.now(), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuario);
        assertThrows(UsuarioNoEncontrado.class, () -> {
            sistema.iniciarSesion("usuariotest", "claveErronea");
        });
    }


    @Test
    void crearIncidenciaCorrectamente() {
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.now(), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuario);

        Usuario admin = new Administrador("del Sistema", "Admin", LocalDate.now(), "N/A", "600000000", "admin@sistema.com", "admin", "admin1234");

        sistema.iniciarSesion("admin", "admin1234");
        sistema.addTipoIncidencia(new TipoIncidencia("Limpieza", "Suciedad en la vía pública"), admin);

        List<TipoIncidencia> tipos = sistema.listarTiposDeIncidencia();
        TipoIncidencia tipo = tipos.getFirst();

        sistema.iniciarSesion("usuariotest", "usuariotest1234");

        Incidencia nuevaIncidencia = new Incidencia();
        nuevaIncidencia.descripcion("Contenedor roto");
        nuevaIncidencia.localizacion("Calle Mayor");
        nuevaIncidencia.localizacionGPS(new LocalizacionGPS(10,10));
        nuevaIncidencia.tipoIncidencia(tipo);

        sistema.crearIncidencia(nuevaIncidencia, usuario);

        List<Incidencia> incidenciasUsuario = sistema.listarIncidenciasDeUsuario(usuario);
        assertEquals(1, incidenciasUsuario.size());
        Incidencia creada = incidenciasUsuario.getFirst();
        assertEquals("Contenedor roto", creada.descripcion());
    }

    @Test
    void usuarioNormalPuedeBorrarSuIncidenciaPendiente() {
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.now(), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuario);

        Usuario admin = new Administrador("del Sistema", "Admin", LocalDate.now(), "N/A", "600000000", "admin@sistema.com", "admin", "admin1234");

        sistema.iniciarSesion("admin", "admin1234");
        sistema.addTipoIncidencia(new TipoIncidencia("Varios", "Incidencias varias"), admin);

        List<TipoIncidencia> tipos = sistema.listarTiposDeIncidencia();
        TipoIncidencia tipo = tipos.getFirst();

        sistema.iniciarSesion("usuariotest", "usuariotest1234");

        Incidencia nuevaIncidencia = new Incidencia();
        nuevaIncidencia.descripcion("Test de borrado");
        nuevaIncidencia.localizacion("Calle Test");
        nuevaIncidencia.localizacionGPS(new LocalizacionGPS(10,10));
        nuevaIncidencia.tipoIncidencia(tipo);
        sistema.crearIncidencia(nuevaIncidencia, usuario);

        Incidencia incidencia = sistema.listarIncidenciasDeUsuario(usuario).getFirst();

        assertDoesNotThrow(() -> sistema.borrarIncidencia(usuario, incidencia));
        assertTrue(sistema.listarIncidenciasDeUsuario(usuario).isEmpty());
    }

    @Test
    void usuarioNormalNoPuedeBorrarIncidenciaAjena() {
        Usuario usuarioNormal1 = new Usuario("Test", "Usuario1", LocalDate.now(), "Casa 1", "611111111", "user1@test.com", "user1", "user1pass");
        Usuario usuarioNormal2 = new Usuario("Test", "Usuario2", LocalDate.now(), "Casa 2", "622222222", "user2@test.com", "user2", "user2pass");
        sistema.registrarUsuario(usuarioNormal1);
        sistema.registrarUsuario(usuarioNormal2);

        Usuario admin = new Administrador("del Sistema", "Admin", LocalDate.now(), "N/A", "600000000", "admin@sistema.com", "admin", "admin1234");

        sistema.iniciarSesion("admin", "admin1234");
        sistema.addTipoIncidencia(new TipoIncidencia("Mobiliario", "Mobiliario urbano dañado"), admin);

        List<TipoIncidencia> tipos = sistema.listarTiposDeIncidencia();
        TipoIncidencia tipo = tipos.getFirst();

        sistema.iniciarSesion("user2", "user2pass");
        Incidencia nuevaIncidencia = new Incidencia();
        nuevaIncidencia.descripcion("Incidencia de User2");
        nuevaIncidencia.localizacion("Plaza Central");
        nuevaIncidencia.localizacionGPS(new LocalizacionGPS(10,10));
        nuevaIncidencia.tipoIncidencia(tipo);
        sistema.crearIncidencia(nuevaIncidencia, usuarioNormal2);

        Incidencia incidenciaDeOtro = sistema.listarIncidenciasDeUsuario(usuarioNormal2).getFirst();

        assertThrows(AccionNoAutorizada.class, () -> sistema.borrarIncidencia(usuarioNormal1, incidenciaDeOtro));
    }

    @Test
    void administradorPuedeBorrarCualquierIncidencia() {
        Usuario admin = new Administrador("del Sistema", "Administrador", LocalDate.of(2000, 1, 1), "N/A", "600000000", "admin@sistema.com", "admin", "admin1234");

        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.now(), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuario);

        sistema.iniciarSesion("admin", "admin1234");
        sistema.addTipoIncidencia(new TipoIncidencia("Limpieza", "Suciedad"), admin);

        List<TipoIncidencia> tipos = sistema.listarTiposDeIncidencia();
        TipoIncidencia tipo = tipos.getFirst();

        sistema.iniciarSesion("usuariotest", "usuariotest1234");
        Incidencia nuevaIncidencia = new Incidencia();
        nuevaIncidencia.descripcion("Incidencia de Juan");
        nuevaIncidencia.localizacion("Test");
        nuevaIncidencia.localizacionGPS(new LocalizacionGPS(10,10));
        nuevaIncidencia.tipoIncidencia(tipo);
        sistema.crearIncidencia(nuevaIncidencia, usuario);

        Incidencia incidencia = sistema.listarIncidenciasDeUsuario(usuario).getFirst();
        incidencia.estadoIncidencia(EstadoIncidencia.RESUELTA);

        assertDoesNotThrow(() -> sistema.borrarIncidencia(admin, incidencia));
        assertTrue(sistema.listarIncidenciasDeUsuario(usuario).isEmpty());
    }

    @Test
    void administradorPuedeCambiarEstadoIncidencia() {
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.now(), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuario);

        Usuario admin = new Administrador("del Sistema", "Administrador", LocalDate.of(2000, 1, 1), "N/A", "600000000", "admin@sistema.com", "admin", "admin1234");

        sistema.iniciarSesion("admin", "admin1234");
        sistema.addTipoIncidencia(new TipoIncidencia("Limpieza", "Suciedad"), admin);

        List<TipoIncidencia> tipos = sistema.listarTiposDeIncidencia();
        TipoIncidencia tipo = tipos.getFirst();

        sistema.iniciarSesion("usuariotest", "usuariotest1234");
        Incidencia nuevaIncidencia = new Incidencia();
        nuevaIncidencia.descripcion("Test");
        nuevaIncidencia.localizacion("Test");
        nuevaIncidencia.localizacionGPS(new LocalizacionGPS(10,10));
        nuevaIncidencia.tipoIncidencia(tipo);
        sistema.crearIncidencia(nuevaIncidencia, usuario);

        Incidencia incidencia = sistema.listarIncidenciasDeUsuario(usuario).getFirst();
        int idIncidencia = incidencia.id();

        sistema.iniciarSesion("admin", "admin1234");

        Incidencia paraModificar = new Incidencia();
        paraModificar.id(idIncidencia);
        paraModificar.estadoIncidencia(EstadoIncidencia.RESUELTA);

        sistema.modificarEstadoIncidencia(paraModificar, admin);

        List<Incidencia> incidenciasResueltas = sistema.buscarIncidencias(tipo, EstadoIncidencia.RESUELTA);
        assertEquals(1, incidenciasResueltas.size());
        assertEquals(idIncidencia, incidenciasResueltas.getFirst().id());
    }
}