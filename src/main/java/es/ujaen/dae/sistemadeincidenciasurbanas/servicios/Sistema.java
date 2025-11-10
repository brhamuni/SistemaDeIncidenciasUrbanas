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
}