package es.ujaen.dae.sistemadeincidenciasurbanas.repositorios;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.Incidencia;
import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.EstadoIncidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RepositorioIncidencias extends JpaRepository<Incidencia, Long> {

    List<Incidencia> buscarPorEstado(EstadoIncidencia estado);
    default void guardar(Incidencia incidencia) {
        save(incidencia);
    }
    Optional<Incidencia> buscarPorId(Long id);
    default void eliminar(Incidencia incidencia) {
        delete(incidencia);
    }

}