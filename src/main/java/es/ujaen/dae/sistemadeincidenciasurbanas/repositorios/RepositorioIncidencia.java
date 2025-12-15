package es.ujaen.dae.sistemadeincidenciasurbanas.repositorios;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.EstadoIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.Incidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.TipoIncidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.Usuario;
import es.ujaen.dae.sistemadeincidenciasurbanas.util.DistanciaGeografica;
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
        Optional<Incidencia> optIncidencia = Optional.ofNullable(em.find(Incidencia.class, id));

        optIncidencia.ifPresent(inc -> {
            inc.usuario().login();
            inc.tipoIncidencia().nombre();
        });

        return optIncidencia;
    }

    @Transactional(readOnly = true)
    public byte[] obtenerFoto(int id) {
        return em.createQuery("SELECT i.foto FROM Incidencia i WHERE i.id = :id", byte[].class).setParameter("id", id).getSingleResult();
    }

    public List<Incidencia> buscarPorUsuario(Usuario usuario) {
        List<Incidencia> incidencias = em.createQuery("SELECT i FROM Incidencia i WHERE i.usuario = :usuario", Incidencia.class)
                .setParameter("usuario", usuario)
                .getResultList();

        incidencias.forEach(inc -> {
            inc.usuario().login();
            inc.tipoIncidencia().nombre();
        });

        return incidencias;
    }

    public List<Incidencia> buscarPorTipoYEstado(TipoIncidencia tipo, EstadoIncidencia estado) {
        StringBuilder select = new StringBuilder("SELECT i FROM Incidencia i WHERE 1=1");
        if (tipo != null) {
            select.append(" AND i.tipoIncidencia = :tipo");
        }
        if (estado != null) {
            select.append(" AND i.estadoIncidencia = :estado");
        }
        var query = em.createQuery(select.toString(), Incidencia.class);
        if (tipo != null) {
            query.setParameter("tipo", tipo);
        }
        if (estado != null) {
            query.setParameter("estado", estado);
        }
        List<Incidencia> incidencias = query.getResultList();

        incidencias.forEach(inc -> {
            inc.usuario().login();
            inc.tipoIncidencia().nombre();
        });

        return incidencias;
    }

    public boolean existeConTipoIncidencia(String nombreTipoIncidencia) {
        Long count = em.createQuery("SELECT COUNT(i) FROM Incidencia i WHERE i.tipoIncidencia.nombre = :nombre", Long.class)
                .setParameter("nombre", nombreTipoIncidencia)
                .getSingleResult();

        return count > 0;
    }

    public List<Incidencia> buscarPorEstados(List<EstadoIncidencia> estados) {
        List<Incidencia> incidencias = em.createQuery(
                        "SELECT i FROM Incidencia i WHERE i.estadoIncidencia IN :estados", Incidencia.class)
                .setParameter("estados", estados)
                .getResultList();

        incidencias.forEach(inc -> {
            inc.usuario().login();
            inc.tipoIncidencia().nombre();
        });

        return incidencias;
    }
}