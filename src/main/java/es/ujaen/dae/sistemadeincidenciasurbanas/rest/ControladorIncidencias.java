package es.ujaen.dae.sistemadeincidenciasurbanas.rest;

import es.ujaen.dae.sistemadeincidenciasurbanas.rest.dto.Mapeador;
import es.ujaen.dae.sistemadeincidenciasurbanas.servicios.Sistema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/incidencias")
public class ControladorIncidencias {
    @Autowired
    Sistema sistema;

    @Autowired
    Mapeador mapeador;


}
