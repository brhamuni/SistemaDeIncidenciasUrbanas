package es.ujaen.dae.sistemadeincidenciasurbanas.repositorios;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.TipoIncidencia;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.constraints.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class RepositorioTipoIncidencia {

    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @CacheEvict(value = "tiposIncidencia", allEntries = true)
    public void guardar(TipoIncidencia tipo) {
        em.persist(tipo);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @CacheEvict(value = {"tiposIncidenciaPorNombre", "tiposIncidencia"}, allEntries = true)
    public void borrar(@NotNull TipoIncidencia tipo) {
        TipoIncidencia managedTipo = em.find(TipoIncidencia.class, tipo.nombre());
        if (managedTipo != null) {
            em.remove(managedTipo);
        }
    }

    @Cacheable(value = "tiposIncidenciaPorNombre", key = "#nombre")
    public Optional<TipoIncidencia> buscar(String nombre) {
        return Optional.ofNullable(em.find(TipoIncidencia.class, nombre));
    }

    public boolean existe(String nombre) {
        Long count = em.createQuery("SELECT COUNT(t) FROM TipoIncidencia t WHERE t.nombre = :nombre", Long.class)
            .setParameter("nombre", nombre)
            .getSingleResult();
        return count > 0;
    }

    @Cacheable("tiposIncidencia")
    public List<TipoIncidencia> listarTodos() {
        return em.createQuery("SELECT t FROM TipoIncidencia t", TipoIncidencia.class)
                .getResultList();
    }
}