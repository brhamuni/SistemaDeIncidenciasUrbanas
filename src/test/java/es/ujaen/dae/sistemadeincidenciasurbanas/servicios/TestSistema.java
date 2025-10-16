package es.ujaen.dae.sistemadeincidenciasurbanas.servicios;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.*;
import es.ujaen.dae.sistemadeincidenciasurbanas.excepciones.*;
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
            sistema.addTipoIncidencia("Test", "Test");
        });
    }

    @Test
    void crearIncidenciaCorrectamente() {
        Sistema sistema = new Sistema();
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.now(), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        TipoIncidencia tipo = new TipoIncidencia("Limpieza", "Suciedad en la vía pública");
        sistema.registrarUsuario(usuario);

        // Para añadir un tipo, hay que ser admin
        sistema.iniciarSesion("admin", "admin1234");
        sistema.addTipoIncidencia(tipo.nombre(), tipo.descripcion());
        sistema.cerrarSesion();

        sistema.crearIncidencia(LocalDateTime.now(), "Limpieza", "Contenedor roto", "Calle Mayor", "", usuario, tipo);

        // Verificación: Comprobamos que el usuario tiene una incidencia registrada.
        List<Incidencia> incidenciasUsuario = sistema.listarIncidenciasDeUsuario(usuario);
        assertEquals(1, incidenciasUsuario.size());
        Incidencia creada = incidenciasUsuario.get(0);
        assertEquals("Contenedor roto", creada.descripcion());
    }

    @Test
    void usuarioNormalPuedeBorrarSuIncidenciaPendiente() {
        Sistema sistema = new Sistema();
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.now(), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        TipoIncidencia tipo = new TipoIncidencia("Varios", "Incidencias varias");
        sistema.registrarUsuario(usuario);
        sistema.iniciarSesion("admin", "admin1234");
        sistema.addTipoIncidencia(tipo.nombre(), tipo.descripcion());
        sistema.cerrarSesion();

        sistema.crearIncidencia(LocalDateTime.now(), "Varios", "Test de borrado", "Calle Test", "", usuario, tipo);
        Incidencia incidencia = sistema.listarIncidenciasDeUsuario(usuario).get(0);

        assertDoesNotThrow(() -> sistema.borrarIncidencia(usuario, incidencia));
        // Verificación: La lista de incidencias del usuario debe estar vacía.
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

        sistema.iniciarSesion("admin", "admin1234");
        sistema.addTipoIncidencia(tipo.nombre(), tipo.descripcion());
        sistema.cerrarSesion();

        sistema.crearIncidencia(LocalDateTime.now(), "Mobiliario", "Incidencia de User2", "Plaza Central", "", usuarioNormal2, tipo);
        Incidencia incidenciaDeOtro = sistema.listarIncidenciasDeUsuario(usuarioNormal2).get(0);

        assertThrows(AccionNoAutorizada.class, () -> sistema.borrarIncidencia(usuarioNormal1, incidenciaDeOtro));
    }

    @Test
    void administradorPuedeBorrarCualquierIncidencia() {
        Sistema sistema = new Sistema();
        Administrador admin = new Administrador(
                "del Sistema",
                "Administrador",
                LocalDate.of(2000, 1, 1),
                "N/A",
                "600000000",
                "admin@sistema.com",
                "admin",
                "admin1234"
        );
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.now(), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        TipoIncidencia tipo = new TipoIncidencia("Limpieza", "Suciedad");
        sistema.registrarUsuario(usuario);

        sistema.iniciarSesion("admin", "admin1234");
        sistema.addTipoIncidencia(tipo.nombre(), tipo.descripcion());
        sistema.cerrarSesion();

        sistema.crearIncidencia(LocalDateTime.now(), "Limpieza", "Incidencia de Juan", "Test", "", usuario, tipo);
        Incidencia incidencia = sistema.listarIncidenciasDeUsuario(usuario).get(0);
        incidencia.estadoIncidencia(EstadoIncidencia.RESUELTA);

        assertDoesNotThrow(() -> sistema.borrarIncidencia(admin, incidencia));
        assertTrue(sistema.listarIncidenciasDeUsuario(usuario).isEmpty());
    }

    @Test
    void administradorPuedeCambiarEstadoIncidencia() {
        Sistema sistema = new Sistema();
        Usuario usuario = new Usuario("Test", "Usuario", LocalDate.now(), "Casa Usuario Test", "600000000", "usuario@test.com", "usuariotest", "usuariotest1234");
        TipoIncidencia tipo = new TipoIncidencia("Limpieza", "Suciedad");
        sistema.registrarUsuario(usuario);

        sistema.iniciarSesion("admin", "admin1234");
        sistema.addTipoIncidencia(tipo.nombre(), tipo.descripcion());

        sistema.crearIncidencia(LocalDateTime.now(), "Limpieza", "Test", "Test", "", usuario, tipo);
        Incidencia incidencia = sistema.listarIncidenciasDeUsuario(usuario).get(0);
        int idIncidencia = incidencia.id();

        sistema.modificarEstadoIncidencia(idIncidencia, EstadoIncidencia.RESUELTA);

        // Verificación: Buscamos incidencias resueltas y comprobamos que está ahí.
        List<Incidencia> incidenciasResueltas = sistema.buscarIncidencias(tipo, EstadoIncidencia.RESUELTA);
        assertEquals(1, incidenciasResueltas.size());
        assertEquals(idIncidencia, incidenciasResueltas.get(0).id());
    }
}