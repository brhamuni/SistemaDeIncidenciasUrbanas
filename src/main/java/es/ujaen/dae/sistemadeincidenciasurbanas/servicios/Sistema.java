package es.ujaen.dae.sistemadeincidenciasurbanas.servicios;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.*;
import es.ujaen.dae.sistemadeincidenciasurbanas.excepciones.*;
import es.ujaen.dae.sistemadeincidenciasurbanas.util.LocalizacionGPS;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Validated
public class Sistema {

    private List<Usuario> usuarios;
    private List<Incidencia> incidencias;
    private List<TipoIncidencia> tiposDeIncidencia;
    private static int nIncidencia = 1;

    private static final Administrador administrador = new Administrador(
            "del Sistema",
            "Administrador",
            LocalDate.of(2000, 1, 1),
            "N/A",
            "600000000",
            "admin@sistema.com",
            "admin",
            "admin1234"
    );


    public Sistema() {
        this.usuarios = new ArrayList<>();
        this.incidencias = new ArrayList<>();
        this.tiposDeIncidencia = new ArrayList<>();
        usuarios.add(administrador);
    }

    public void registrarUsuario(@Valid Usuario nuevoUsuario) {
        Optional<Usuario> existente = usuarios.stream().filter(u -> u.equals(nuevoUsuario)).findFirst();
        if (existente.isPresent()) {
            throw new UsuarioYaExiste();
        }
        usuarios.add(nuevoUsuario);
    }

    public Usuario iniciarSesion(@NotNull String login, @NotNull String clave) {
        for (Usuario u : usuarios) {
            if (u.login().equals(login) && u.claveAcceso().equals(clave)) {
                return u;
            }
        }
        throw new UsuarioNoEncontrado();
    }

    public void actualizarDatosUsuario(@Valid Usuario usuarioNuevo) {
        Usuario usuarioOriginal = usuarios.stream()
                .filter(u -> u.equals(usuarioNuevo))
                .findFirst()
                .orElseThrow(() -> new UsuarioNoEncontrado());

        usuarioOriginal.nombre(usuarioNuevo.nombre());
        usuarioOriginal.apellidos(usuarioNuevo.apellidos());
        usuarioOriginal.email(usuarioNuevo.email());
        usuarioOriginal.telefono(usuarioNuevo.telefono());
        usuarioOriginal.direccion(usuarioNuevo.direccion());
    }

    public void crearIncidencia(@Valid Incidencia nuevaIncidencia, @NotNull Usuario usuario) {
        if (usuario == null) {
            throw new UsuarioNoLogeado();
        }

        nuevaIncidencia.id(nIncidencia++);
        nuevaIncidencia.fecha(LocalDateTime.now());
        nuevaIncidencia.usuario(usuarioActual);
        nuevaIncidencia.estadoIncidencia(EstadoIncidencia.PENDIENTE);

        if (incidencias.contains(nuevaIncidencia)) {
            throw new IncidenciaYaExiste();
        }

        incidencias.add(nuevaIncidencia);
    }

    public List<Incidencia> listarIncidenciasDeUsuario(@Valid Usuario usuario) {
        List<Incidencia> lista_incidencias = new ArrayList<>();

        for (Incidencia incidencia : incidencias) {
            if(incidencia.usuario().equals(usuario)) {
                lista_incidencias.add(incidencia);
            }
        }

        return lista_incidencias;
    }

    public List<Incidencia> buscarIncidencias(@Valid TipoIncidencia tipo, @Valid EstadoIncidencia estado){
        List<Incidencia> lista_incidencias = new ArrayList<>();

        for (Incidencia incidencia : incidencias) {
            if(incidencia.estadoIncidencia().equals(estado) && incidencia.tipoIncidencia().equals(tipo)) {
                lista_incidencias.add(incidencia);
            }
        }

        return lista_incidencias;
    }

    public void borrarIncidencia(@Valid Usuario usuario, @Valid Incidencia incidencia) {
        if (!incidencias.contains(incidencia)) {
            throw new IncidenciaNoExiste();
        }
        if (esAdmin(usuario)) {
            incidencias.remove(incidencia);
        }else{
            if (!incidencia.usuario().equals(usuario)) {
                throw new AccionNoAutorizada("No puedes borrar una incidencia que no es tuya");
            }
            if (incidencia.estadoIncidencia() != EstadoIncidencia.PENDIENTE) {
                throw new AccionNoAutorizada("Solo puedes borrar incidencias pendientes");
            }
            incidencias.remove(incidencia);
        }
    }

    public void modificarEstadoIncidencia(@Valid Incidencia incidenciaNuevoEstado, @Valid Usuario usuarioLogeado) {
        if (usuarioLogeado == null) {
            throw new UsuarioNoLogeado();
        }
        if (!esAdmin(usuarioLogeado)) {
            throw new UsuarioNoAdmin();
        }

        Incidencia incidenciaOriginal = incidencias.stream()
                .filter(i -> i.id() == incidenciaNuevoEstado.id())
                .findFirst()
                .orElseThrow(() -> new IncidenciaNoExiste());

        incidenciaOriginal.estadoIncidencia(incidenciaNuevoEstado.estadoIncidencia());
    }

    public void addTipoIncidencia(@Valid TipoIncidencia nuevoTipo, @Valid Usuario usuarioLogeado) {
        if (usuarioLogeado == null){
            throw new UsuarioNoLogeado();
        }
        if (!esAdmin(usuarioLogeado)){
            throw new UsuarioNoAdmin();
        }

        boolean yaExiste = tiposDeIncidencia.stream()
                .anyMatch(t -> t.nombre().equalsIgnoreCase(nuevoTipo.nombre()));
        if (yaExiste) {
            throw new TipoIncidenciaYaExiste(); // Deberías crear esta excepción
        }

        tiposDeIncidencia.add(nuevoTipo);
    }

    public void borrarTipoIncidencia(@Valid TipoIncidencia tipoIncidencia, @NotNull Usuario usuario) {
        if (!esAdmin(usuario)){
            throw new UsuarioNoAdmin();
        }
        
        boolean enUso = incidencias.stream().anyMatch(incidencia -> incidencia.tipoIncidencia().equals(tipoIncidencia));
        if (enUso) {
            throw new TipoIncidenciaEnUso("No se puede borrar un tipo de incidencia que está siendo utilizado por una o más incidencias.");
        }

        tiposDeIncidencia.remove(tipoIncidencia);
    }

    public boolean esAdmin(@Valid Usuario usuario) {
        if(usuario.equals(administrador)) {
            return true;
        }
        return false;
    }

    public List<TipoIncidencia> listarTiposDeIncidencia() {
        return new ArrayList<>(tiposDeIncidencia);
    }

}