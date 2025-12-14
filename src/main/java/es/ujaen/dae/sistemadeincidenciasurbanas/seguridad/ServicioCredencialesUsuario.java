package es.ujaen.dae.sistemadeincidenciasurbanas.seguridad;


import es.ujaen.dae.sistemadeincidenciasurbanas.entidades.Usuario;
import es.ujaen.dae.sistemadeincidenciasurbanas.servicios.Sistema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ServicioCredencialesUsuario implements UserDetailsService {

    @Autowired
    Sistema sistema;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Usuario usuario = sistema.buscarUsuario(login).orElseThrow(() -> new UsernameNotFoundException(""));

        return User.withUsername(usuario.login())
                .password(usuario.claveAcceso())
                .roles(usuario.login().equals("admin") ? "ADMIN": "USER")
                .build();
    }
}
