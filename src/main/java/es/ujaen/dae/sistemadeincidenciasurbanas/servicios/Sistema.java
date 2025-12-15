package es.ujaen.dae.sistemadeincidenciasurbanas.servicios;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.*;
import es.ujaen.dae.sistemadeincidenciasurbanas.excepciones.*;
import es.ujaen.dae.sistemadeincidenciasurbanas.repositorios.*;
import es.ujaen.dae.sistemadeincidenciasurbanas.util.DistanciaGeografica;
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
public class Sistema {

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    private RepositorioIncidencia repositorioIncidencia;

    @Autowired
    private RepositorioTipoIncidencia repositorioTipo;

    private static final Administrador administrador = new Administrador(
            "Administrador", "Administrador", LocalDate.of(2000, 1, 1),
            "N/A", "600000000", "admin@ujaen.es", "admin", "$2a$10$8CjaO.y0yCvi.HynDNAgZe2chWO3S2oOkQzwuQLCSVi1Jg4J7dlme"
    );

    @PostConstruct
    public void inicializar() {
        repositorioUsuario.buscarPorLogin(administrador.login())
                .orElseGet(() -> {
                    repositorioUsuario.actualizar(administrador);
                    return administrador;
                });
    }


    public void registrarUsuario(@Valid Usuario nuevoUsuario) {
        if (repositorioUsuario.buscarPorLogin(nuevoUsuario.login()).isPresent()) {
            throw new UsuarioYaExiste();
        }
        repositorioUsuario.guardar(nuevoUsuario);
    }

    public Optional<Usuario> buscarUsuario(String login) {
        return repositorioUsuario.buscarPorLogin(login);
    }


    public Optional<Usuario> iniciarSesion(@NotNull String login, @NotNull String clave) {
        return repositorioUsuario.buscarPorLoginYClaveAcceso(login, clave);
    }

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

    public void crearIncidencia(@Valid Incidencia nuevaIncidencia, @NotNull Usuario usuario) {
        System.out.println("LLEGO AQUI---");
        if (usuario == null) throw new UsuarioNoLogeado();
        System.out.println("LLEGO AQUI------------------------------------------------------");
        Usuario usuarioAttached = repositorioUsuario.actualizar(usuario);
        TipoIncidencia tipoAttached = repositorioTipo.buscar(nuevaIncidencia.tipoIncidencia().nombre())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de incidencia no existe"));

        nuevaIncidencia.fecha(LocalDateTime.now());
        nuevaIncidencia.usuario(usuarioAttached);
        nuevaIncidencia.tipoIncidencia(tipoAttached);
        nuevaIncidencia.estadoIncidencia(EstadoIncidencia.PENDIENTE);

        repositorioIncidencia.guardar(nuevaIncidencia);
    }

    public List<Incidencia> obtenerIncidenciasCercanas(double latitud, double longitud) {
        // Recupera solo las incidencias pendientes o en evaluación
        List<Incidencia> candidatas = repositorioIncidencia.buscarPorEstados(
                List.of(EstadoIncidencia.PENDIENTE, EstadoIncidencia.EN_EVALUACION)
        );

        // Filtra por distancia (<10 metros)
        return candidatas.stream()
                .filter(inc -> {
                    double distancia = DistanciaGeografica.distancia_dos_puntos(
                            latitud, longitud,
                            inc.localizacionGPS().y(),
                            inc.localizacionGPS().x()
                    );
                    return distancia <= 10.0;
                })
                .toList();
    }

    public List<Incidencia> listarIncidenciasDeUsuario(@Valid Usuario usuario) {
        return repositorioIncidencia.buscarPorUsuario(usuario);
    }

    public List<Incidencia> buscarIncidencias(TipoIncidencia tipo, EstadoIncidencia estado) {
        return repositorioIncidencia.buscarPorTipoYEstado(tipo, estado);
    }

    public void borrarIncidencia(@Valid Usuario usuario, @Valid Incidencia incidencia) {
        if (incidencia.id() == 0) {
            throw new IncidenciaNoExiste();
        }

        if (esAdmin(usuario)) {
            repositorioIncidencia.borrar(incidencia);
        } else {
            if (!incidencia.usuario().equals(usuario))
                throw new AccionNoAutorizada("No puedes borrar una incidencia que no es tuya");
            if (incidencia.estadoIncidencia() != EstadoIncidencia.PENDIENTE)
                throw new AccionNoAutorizada("Solo puedes borrar incidencias pendientes");

            repositorioIncidencia.borrar(incidencia);
        }
    }

    public void modificarEstadoIncidencia(Incidencia incidencia, EstadoIncidencia nuevoEstado, @Valid Usuario usuarioLogeado) {
        if (usuarioLogeado == null) throw new UsuarioNoLogeado();
        if (!esAdmin(usuarioLogeado)) throw new UsuarioNoAdmin();
        if (incidencia == null) throw new IncidenciaNoExiste();

        incidencia.estadoIncidencia(nuevoEstado);
        repositorioIncidencia.actualizar(incidencia);
    }

    public boolean esAdmin(@Valid Usuario usuario) {
        return usuario != null && usuario.login().equals(administrador.login());
    }

    public void addTipoIncidencia(@Valid TipoIncidencia nuevoTipo, @Valid Usuario usuarioLogeado) {
        if (usuarioLogeado == null) throw new UsuarioNoLogeado();
        if (!esAdmin(usuarioLogeado)) throw new UsuarioNoAdmin();

        if (repositorioTipo.existe(nuevoTipo.nombre())) throw new TipoIncidenciaYaExiste();

        repositorioTipo.guardar(nuevoTipo);
    }

    public void borrarTipoIncidencia(@Valid String nombre, @NotNull Usuario usuario) {
        if (!esAdmin(usuario)) throw new UsuarioNoAdmin();

        if (repositorioIncidencia.existeConTipoIncidencia(nombre)) {
            throw new TipoIncidenciaEnUso("No se puede borrar un tipo de incidencia en uso.");
        }

        repositorioTipo.borrar(nombre);
    }

    public List<TipoIncidencia> listarTiposDeIncidencia() {
        return repositorioTipo.listarTodos();
    }
}