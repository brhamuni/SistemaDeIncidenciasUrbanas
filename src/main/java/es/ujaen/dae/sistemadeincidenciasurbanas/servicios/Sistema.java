package es.ujaen.dae.sistemadeincidenciasurbanas.servicios;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.*;
import es.ujaen.dae.sistemadeincidenciasurbanas.excepciones.*;
import es.ujaen.dae.sistemadeincidenciasurbanas.util.LocalizacionGPS;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Validated
@Transactional(rollbackFor = Exception.class)
public class Sistema {

    @PersistenceContext
    private EntityManager em;

    private static final Administrador administrador = new Administrador(
            "del Sistema", "Administrador", LocalDate.of(2000, 1, 1),
            "N/A", "600000000", "admin@sistema.com", "admin", "admin1234"
    );

    @PostConstruct
    public void inicializar() {
        // No usamos iniciarSesion() aquí porque @Transactional no aplica aún
        List<Usuario> adminList = em.createQuery(
                        "SELECT u FROM Usuario u WHERE u.login = :login", Usuario.class)
                .setParameter("login", administrador.login())
                .getResultList();

        if (adminList.isEmpty()) {
            em.persist(administrador);
        }
    }

    @Transactional
    public void registrarUsuario(@Valid Usuario nuevoUsuario) {
        boolean existe = !em.createQuery(
                        "SELECT u FROM Usuario u WHERE u.login = :login", Usuario.class)
                .setParameter("login", nuevoUsuario.login())
                .getResultList().isEmpty();

        if (existe) throw new UsuarioYaExiste();

        em.persist(nuevoUsuario);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> iniciarSesion(@NotNull String login, @NotNull String clave) {
        return em.createQuery(
                        "SELECT u FROM Usuario u WHERE u.login = :login AND u.claveAcceso = :clave", Usuario.class)
                .setParameter("login", login)
                .setParameter("clave", clave)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Transactional
    public void actualizarDatosUsuario(@Valid Usuario usuarioNuevo) {
        Usuario usuarioOriginal = em.createQuery(
                        "SELECT u FROM Usuario u WHERE u.login = :login", Usuario.class)
                .setParameter("login", usuarioNuevo.login())
                .getResultList()
                .stream()
                .findFirst()
                .orElseThrow(UsuarioNoEncontrado::new);

        usuarioOriginal.nombre(usuarioNuevo.nombre());
        usuarioOriginal.apellidos(usuarioNuevo.apellidos());
        usuarioOriginal.email(usuarioNuevo.email());
        usuarioOriginal.telefono(usuarioNuevo.telefono());
        usuarioOriginal.direccion(usuarioNuevo.direccion());
    }

    @Transactional
    public void crearIncidencia(@Valid Incidencia nuevaIncidencia, @NotNull Usuario usuario) {
        if (usuario == null) throw new UsuarioNoLogeado();

        Usuario usuarioAttached = em.merge(usuario);
        TipoIncidencia tipoAttached = em.merge(nuevaIncidencia.tipoIncidencia());

        nuevaIncidencia.fecha(LocalDateTime.now());
        nuevaIncidencia.usuario(usuarioAttached);
        nuevaIncidencia.tipoIncidencia(tipoAttached);
        nuevaIncidencia.estadoIncidencia(EstadoIncidencia.PENDIENTE);

        em.persist(nuevaIncidencia);
    }

    @Transactional(readOnly = true)
    public List<Incidencia> listarIncidenciasDeUsuario(@Valid Usuario usuario) {
        return em.createQuery(
                        "SELECT i FROM Incidencia i WHERE i.usuario.login = :login", Incidencia.class)
                .setParameter("login", usuario.login())
                .getResultList();
    }

    @Transactional(readOnly = true)
    public List<Incidencia> buscarIncidencias(TipoIncidencia tipo, EstadoIncidencia estado) {
        StringBuilder jpql = new StringBuilder("SELECT i FROM Incidencia i WHERE 1=1");
        if (tipo != null) jpql.append(" AND i.tipoIncidencia = :tipo");
        if (estado != null) jpql.append(" AND i.estadoIncidencia = :estado");

        var query = em.createQuery(jpql.toString(), Incidencia.class);
        if (tipo != null) query.setParameter("tipo", tipo);
        if (estado != null) query.setParameter("estado", estado);

        return query.getResultList();
    }

    @Transactional
    public void borrarIncidencia(@Valid Usuario usuario, @Valid Incidencia incidencia) {
        Incidencia inc = em.find(Incidencia.class, incidencia.id());
        if (inc == null) throw new IncidenciaNoExiste();

        if (esAdmin(usuario)) {
            em.remove(inc);
        } else {
            if (!inc.usuario().equals(usuario))
                throw new AccionNoAutorizada("No puedes borrar una incidencia que no es tuya");
            if (inc.estadoIncidencia() != EstadoIncidencia.PENDIENTE)
                throw new AccionNoAutorizada("Solo puedes borrar incidencias pendientes");

            em.remove(inc);
        }
    }

    @Transactional
    public void modificarEstadoIncidencia(Incidencia incidenciaNuevoEstado, @Valid Usuario usuarioLogeado) {
        if (usuarioLogeado == null) throw new UsuarioNoLogeado();
        if (!esAdmin(usuarioLogeado)) throw new UsuarioNoAdmin();

        Incidencia inc = em.find(Incidencia.class, incidenciaNuevoEstado.id());
        if (inc == null) throw new IncidenciaNoExiste();

        inc.estadoIncidencia(incidenciaNuevoEstado.estadoIncidencia());
    }

    public boolean esAdmin(@Valid Usuario usuario) {
        return usuario != null && usuario.login().equals(administrador.login());
    }

    @Transactional
    public void addTipoIncidencia(@Valid TipoIncidencia nuevoTipo, @Valid Usuario usuarioLogeado) {
        if (usuarioLogeado == null) throw new UsuarioNoLogeado();
        if (!esAdmin(usuarioLogeado)) throw new UsuarioNoAdmin();

        boolean existe = !em.createQuery(
                        "SELECT t FROM TipoIncidencia t WHERE t.nombre = :nombre", TipoIncidencia.class)
                .setParameter("nombre", nuevoTipo.nombre())
                .getResultList()
                .isEmpty();

        if (existe) throw new TipoIncidenciaYaExiste();

        em.persist(nuevoTipo);
    }

}