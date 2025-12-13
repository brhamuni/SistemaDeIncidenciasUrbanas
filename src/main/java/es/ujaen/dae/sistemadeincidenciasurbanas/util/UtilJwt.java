package es.ujaen.dae.sistemadeincidenciasurbanas.util;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

public class UtilJwt {

    private static final SecretKey claveFirmadoTokens = Jwts.SIG.HS256.key().build();

    public static String crearToken(String usuario, Map<String, ?> claims, int tiempoExpiracion) {

        var ahora = LocalDateTime.now().atZone(ZoneId.systemDefault());
        return Jwts.builder().claims(claims).subject(usuario).expiration(Date.from(ahora.plusMinutes(tiempoExpiracion).toInstant())).signWith(claveFirmadoTokens).compact();
    }

    public static Claims extraerContenido(String token) {
        return Jwts.parser().verifyWith(claveFirmadoTokens).build().parseSignedClaims(token).getPayload();
    }

}