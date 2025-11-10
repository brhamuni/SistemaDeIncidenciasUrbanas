package es.ujaen.dae.sistemadeincidenciasurbanas.repositorios;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class RepositorioUsuario {

    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void guardar(Usuario usuario) {
        em.persist(usuario);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @CacheEvict(value = {"usuariosPorLogin", "usuariosPorId"}, key = "#usuario.login()")
    public Usuario actualizar(Usuario usuario) {
        return em.merge(usuario);
    }

    @Cacheable(value = "usuariosPorId", key = "#id")
    public Optional<Usuario> buscarPorId(Long id) {
        return Optional.ofNullable(em.find(Usuario.class, id));
    }

    @Cacheable(value = "usuariosPorLogin", key = "#login")
    public Optional<Usuario> buscarPorLogin(String login) {
        try {
            Usuario usuario = em.createQuery("SELECT u FROM Usuario u WHERE u.login = :login", Usuario.class)
                    .setParameter("login", login)
                    .getSingleResult();
            return Optional.of(usuario);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<Usuario> buscarPorLoginYClaveAcceso(String login, String clave) {
        try {
            Usuario usuario = em.createQuery("SELECT u FROM Usuario u WHERE u.login = :login AND u.claveAcceso = :clave", Usuario.class)
                    .setParameter("login", login)
                    .setParameter("clave", clave)
                    .getSingleResult();
            return Optional.of(usuario);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}