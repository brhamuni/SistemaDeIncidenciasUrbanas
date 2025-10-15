package es.ujaen.dae.sistemadeincidenciasurbanas.excepciones;

public class AccionNoAutorizada extends  RuntimeException{

    public AccionNoAutorizada(){}

    public AccionNoAutorizada(String descripcion){
        super(descripcion);
    }

}
