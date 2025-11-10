package es.ujaen.dae.sistemadeincidenciasurbanas.repositorios;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.EstadoIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.Incidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.TipoIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class RepositorioIncidencia {

    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void guardar(Incidencia incidencia) {
        em.persist(incidencia);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @CacheEvict(value = "incidenciasPorId", key = "#incidencia.id()")
    public Incidencia actualizar(Incidencia incidencia) {
        return em.merge(incidencia);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    @CacheEvict(value = "incidenciasPorId", key = "#incidencia.id()")
    public void borrar(Incidencia incidencia) {
        em.remove(em.merge(incidencia));
    }

    @Cacheable(value = "incidenciasPorId", key = "#id")
    public Optional<Incidencia> buscarPorId(int id) {
        return Optional.ofNullable(em.find(Incidencia.class, id));
    }

    public List<Incidencia> buscarPorUsuario(Usuario usuario) {
        return em.createQuery("SELECT i FROM Incidencia i WHERE i.usuario = :usuario", Incidencia.class)
                .setParameter("usuario", usuario)
                .getResultList();
    }

    public List<Incidencia> buscarPorTipoYEstado(TipoIncidencia tipo, EstadoIncidencia estado) {
        StringBuilder jpql = new StringBuilder("SELECT i FROM Incidencia i WHERE 1=1");
        if (tipo != null) {
            jpql.append(" AND i.tipoIncidencia = :tipo");
        }
        if (estado != null) {
            jpql.append(" AND i.estadoIncidencia = :estado");
        }

        var query = em.createQuery(jpql.toString(), Incidencia.class);
        if (tipo != null) {
            query.setParameter("tipo", tipo);
        }
        if (estado != null) {
            query.setParameter("estado", estado);
        }

        return query.getResultList();
    }

    public boolean existeConTipoIncidencia(TipoIncidencia tipoIncidencia) {
        return !em.createQuery("SELECT 1 FROM Incidencia i WHERE i.tipoIncidencia = :tipo", Long.class)
                .setParameter("tipo", tipoIncidencia)
                .getResultList()
                .isEmpty();
    }
}