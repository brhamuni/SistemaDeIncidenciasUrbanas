package es.ujaen.dae.sistemadeincidenciasurbanas.util;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;

@Embeddable
public class LocalizacionGPS {
    @Column(name = "gps_x")
    private int x;

    @Column(name = "gps_y")
    private int y;

    public LocalizacionGPS() {}

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
