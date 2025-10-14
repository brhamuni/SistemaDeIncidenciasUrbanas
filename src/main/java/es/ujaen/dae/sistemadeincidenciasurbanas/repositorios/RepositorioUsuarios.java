package es.ujaen.dae.sistemadeincidenciasurbanas.repositorios;

import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RepositorioUsuarios extends JpaRepository<Usuario, Long> {

    Optional<Usuario> buscarPorLogin(String login);
    default void guardar(Usuario usuario) { save(usuario); }

}