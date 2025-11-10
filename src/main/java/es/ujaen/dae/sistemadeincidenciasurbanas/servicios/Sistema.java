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

}