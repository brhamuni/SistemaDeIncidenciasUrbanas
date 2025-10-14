package es.ujaen.dae.sistemadeincidenciasurbanas.repositorios;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.TipoIncidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RepositorioTiposIncidencia extends JpaRepository<TipoIncidencia, Long> {

    default void guardar(TipoIncidencia tipo) { save(tipo);}
    Optional<TipoIncidencia> buscarPorId(Long id);
    List<TipoIncidencia> buscarTodos();
    default void eliminar(TipoIncidencia tipo) { delete(tipo); }

}