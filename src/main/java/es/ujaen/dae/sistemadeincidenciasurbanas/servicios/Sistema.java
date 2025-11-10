package es.ujaen.dae.sistemadeincidenciasurbanas.servicios;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.*;
import es.ujaen.dae.sistemadeincidenciasurbanas.excepciones.*;
import es.ujaen.dae.sistemadeincidenciasurbanas.repositorios.*;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Validated
@Transactional(rollbackFor = Exception.class)
public class Sistema {

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    private RepositorioIncidencia repositorioIncidencia;

    @Autowired
    private RepositorioTipoIncidencia repositorioTipo;

    private static final Administrador administrador = new Administrador(
            "del Sistema", "Administrador", LocalDate.of(2000, 1, 1),
            "N/A", "600000000", "admin@sistema.com", "admin", "admin1234"
    );

    @PostConstruct
    public void inicializar() {
        repositorioUsuario.buscarPorLogin(administrador.login())
                .orElseGet(() -> {
                    repositorioUsuario.guardar(administrador);
                    return administrador;
                });
    }

    @Transactional
    public void registrarUsuario(@Valid Usuario nuevoUsuario) {
        if (repositorioUsuario.buscarPorLogin(nuevoUsuario.login()).isPresent()) {
            throw new UsuarioYaExiste();
        }
        repositorioUsuario.guardar(nuevoUsuario);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> iniciarSesion(@NotNull String login, @NotNull String clave) {
        return repositorioUsuario.buscarPorLoginYClaveAcceso(login, clave);
    }

    @Transactional
    public void actualizarDatosUsuario(@Valid Usuario usuarioNuevo) {
        Usuario usuarioOriginal = repositorioUsuario.buscarPorLogin(usuarioNuevo.login())
                .orElseThrow(UsuarioNoEncontrado::new);

        usuarioOriginal.nombre(usuarioNuevo.nombre());
        usuarioOriginal.apellidos(usuarioNuevo.apellidos());
        usuarioOriginal.email(usuarioNuevo.email());
        usuarioOriginal.telefono(usuarioNuevo.telefono());
        usuarioOriginal.direccion(usuarioNuevo.direccion());

        repositorioUsuario.actualizar(usuarioOriginal);
    }

    @Transactional
    public void crearIncidencia(@Valid Incidencia nuevaIncidencia, @NotNull Usuario usuario) {
        if (usuario == null) throw new UsuarioNoLogeado();

        Usuario usuarioAttached = repositorioUsuario.actualizar(usuario); // attach
        TipoIncidencia tipoAttached = repositorioTipo.buscar(nuevaIncidencia.tipoIncidencia().nombre())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de incidencia no existe"));

        nuevaIncidencia.fecha(LocalDateTime.now());
        nuevaIncidencia.usuario(usuarioAttached);
        nuevaIncidencia.tipoIncidencia(tipoAttached);
        nuevaIncidencia.estadoIncidencia(EstadoIncidencia.PENDIENTE);

        repositorioIncidencia.guardar(nuevaIncidencia);
    }

    @Transactional(readOnly = true)
    public List<Incidencia> listarIncidenciasDeUsuario(@Valid Usuario usuario) {
        return repositorioIncidencia.buscarPorUsuario(usuario);
    }

    @Transactional(readOnly = true)
    public List<Incidencia> buscarIncidencias(TipoIncidencia tipo, EstadoIncidencia estado) {
        return repositorioIncidencia.buscarPorTipoYEstado(tipo, estado);
    }

    @Transactional
    public void borrarIncidencia(@Valid Usuario usuario, @Valid Incidencia incidencia) {
        Incidencia inc = repositorioIncidencia.buscarPorId(incidencia.id())
                .orElseThrow(IncidenciaNoExiste::new);

        if (esAdmin(usuario)) {
            repositorioIncidencia.borrar(inc);
        } else {
            if (!inc.usuario().equals(usuario))
                throw new AccionNoAutorizada("No puedes borrar una incidencia que no es tuya");
            if (inc.estadoIncidencia() != EstadoIncidencia.PENDIENTE)
                throw new AccionNoAutorizada("Solo puedes borrar incidencias pendientes");

            repositorioIncidencia.borrar(inc);
        }
    }

    @Transactional
    public void modificarEstadoIncidencia(Incidencia incidenciaNuevoEstado, @Valid Usuario usuarioLogeado) {
        if (usuarioLogeado == null) throw new UsuarioNoLogeado();
        if (!esAdmin(usuarioLogeado)) throw new UsuarioNoAdmin();

        Incidencia inc = repositorioIncidencia.buscarPorId(incidenciaNuevoEstado.id())
                .orElseThrow(IncidenciaNoExiste::new);

        inc.estadoIncidencia(incidenciaNuevoEstado.estadoIncidencia());
        repositorioIncidencia.actualizar(inc);
    }

    public boolean esAdmin(@Valid Usuario usuario) {
        return usuario != null && usuario.login().equals(administrador.login());
    }

    @Transactional
    public void addTipoIncidencia(@Valid TipoIncidencia nuevoTipo, @Valid Usuario usuarioLogeado) {
        if (usuarioLogeado == null) throw new UsuarioNoLogeado();
        if (!esAdmin(usuarioLogeado)) throw new UsuarioNoAdmin();

        if (repositorioTipo.existe(nuevoTipo.nombre())) throw new TipoIncidenciaYaExiste();

        repositorioTipo.guardar(nuevoTipo);
    }

    @Transactional
    public void borrarTipoIncidencia(@Valid TipoIncidencia tipoIncidencia, @NotNull Usuario usuario) {
        if (!esAdmin(usuario)) throw new UsuarioNoAdmin();

        if (repositorioIncidencia.existeConTipoIncidencia(tipoIncidencia)) {
            throw new TipoIncidenciaEnUso("No se puede borrar un tipo de incidencia en uso.");
        }

        repositorioTipo.borrar(tipoIncidencia);
    }

    @Transactional(readOnly = true)
    public List<TipoIncidencia> listarTiposDeIncidencia() {
        return repositorioTipo.listarTodos();
    }
}