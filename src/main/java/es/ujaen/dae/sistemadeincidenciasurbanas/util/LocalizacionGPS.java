package es.ujaen.dae.sistemadeincidenciasurbanas.util;

public class LocalizacionGPS {
    private int x;
    private int y;

    public LocalizacionGPS(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public void x(int x) {
        this.x = x;
    }

    public int y() {
        return y;
    }

    public void y(int y) {
        this.y = y;
    }
}
