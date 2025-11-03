package es.ujaen.dae.sistemadeincidenciasurbanas.servicios;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.*;
import es.ujaen.dae.sistemadeincidenciasurbanas.excepciones.*;
import es.ujaen.dae.sistemadeincidenciasurbanas.util.LocalizacionGPS;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TestSistema {

    @Test
    void registrarUsuarioExitosamente() {
        Sistema sistema = new Sistema();
        Usuario nuevoUsuario = new Usuario("Test", "Usuario", LocalDate.now(), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(nuevoUsuario);
        assertTrue(sistema.iniciarSesion("usuariotest", "usuariotest1234"));
    }

    @Test
    void registrarUsuarioLanzaExcepcionSiLoginDuplicado() {
        Sistema sistema = new Sistema();
        Usuario usuarioExistente = new Usuario("Test", "Usuario", LocalDate.now(), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuarioExistente);

        assertThrows(UsuarioYaExiste.class, () -> {
            Usuario usuarioDuplicado = new Usuario("Otro", "Usuario", LocalDate.now(), "Casa Otro Test", "611111111", "otro@test.com", "usuariotest", "otraclave1234");
            sistema.registrarUsuario(usuarioDuplicado);
        });
    }

    @Test
    void iniciarSesionCorrecto() {
        Sistema sistema = new Sistema();
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.now(), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuario);
        boolean exito = sistema.iniciarSesion("usuariotest", "usuariotest1234");
        assertTrue(exito);
    }

    @Test
    void iniciarSesionIncorrecto() {
        Sistema sistema = new Sistema();
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.now(), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuario);
        boolean exito = sistema.iniciarSesion("usuariotest", "clave_erronea");
        assertFalse(exito);
    }

    @Test
    void cerrarSesionLimpiaUsuarioActual() {
        Sistema sistema = new Sistema();
        sistema.iniciarSesion("admin", "admin1234");

        sistema.cerrarSesion();

        // Verificación: Si la sesión se ha cerrado, una acción de admin debe fallar.
        assertThrows(UsuarioNoLogeado.class, () -> {
            sistema.addTipoIncidencia(new TipoIncidencia("Test", "Test"), null);
        });
    }


    @Test
    void crearIncidenciaCorrectamente() {
        Sistema sistema = new Sistema();
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.now(), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        sistema.registrarUsuario(usuario);

        Usuario admin = new Administrador("del Sistema", "Admin", LocalDate.now(), "N/A", "600000000", "admin@sistema.com", "admin", "admin1234");

        sistema.iniciarSesion("admin", "admin1234");
        sistema.addTipoIncidencia(new TipoIncidencia("Limpieza", "Suciedad en la vía pública"), admin);

        List<TipoIncidencia> tipos = sistema.listarTiposDeIncidencia();
        TipoIncidencia tipo = tipos.getFirst();

        sistema.iniciarSesion("usuariotest", "usuariotest1234")

        Incidencia nuevaIncidencia = new Incidencia();
        nuevaIncidencia.descripcion("Contenedor roto");
        nuevaIncidencia.localizacion("Calle Mayor");
        nuevaIncidencia.localizacionGPS(new LocalizacionGPS(10,10));
        nuevaIncidencia.tipoIncidencia(tipo);

        sistema.crearIncidencia(nuevaIncidencia);

        List<Incidencia> incidenciasUsuario = sistema.listarIncidenciasDeUsuario(usuario);
        assertEquals(1, incidenciasUsuario.size());
        Incidencia creada = incidenciasUsuario.getFirst();
        assertEquals("Contenedor roto", creada.descripcion());
    }

    @Test
    void usuarioNormalPuedeBorrarSuIncidenciaPendiente() {
        Sistema sistema = new Sistema();
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
        sistema.crearIncidencia(nuevaIncidencia);

        Incidencia incidencia = sistema.listarIncidenciasDeUsuario(usuario).getFirst();

        assertDoesNotThrow(() -> sistema.borrarIncidencia(usuario, incidencia));
        assertTrue(sistema.listarIncidenciasDeUsuario(usuario).isEmpty());
    }

    @Test
    void usuarioNormalNoPuedeBorrarIncidenciaAjena() {
        Sistema sistema = new Sistema();
        Usuario usuarioNormal1 = new Usuario("Test", "Usuario1", LocalDate.now(), "Casa 1", "611111111", "user1@test.com", "user1", "user1pass");
        Usuario usuarioNormal2 = new Usuario("Test", "Usuario2", LocalDate.now(), "Casa 2", "622222222", "user2@test.com", "user2", "user2pass");
        TipoIncidencia tipo = new TipoIncidencia("Mobiliario", "Mobiliario urbano dañado");
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
        sistema.crearIncidencia(nuevaIncidencia);

        Incidencia incidenciaDeOtro = sistema.listarIncidenciasDeUsuario(usuarioNormal2).getFirst();

        assertThrows(AccionNoAutorizada.class, () -> sistema.borrarIncidencia(usuarioNormal1, incidenciaDeOtro));
    }

    @Test
    void administradorPuedeBorrarCualquierIncidencia() {
        Sistema sistema = new Sistema();

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
        sistema.crearIncidencia(nuevaIncidencia);

        Incidencia incidencia = sistema.listarIncidenciasDeUsuario(usuario).getFirst();
        incidencia.estadoIncidencia(EstadoIncidencia.RESUELTA);

        assertDoesNotThrow(() -> sistema.borrarIncidencia(admin, incidencia));
        assertTrue(sistema.listarIncidenciasDeUsuario(usuario).isEmpty());
    }

    @Test
    void administradorPuedeCambiarEstadoIncidencia() {
        Sistema sistema = new Sistema();
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
        sistema.crearIncidencia(nuevaIncidencia);

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