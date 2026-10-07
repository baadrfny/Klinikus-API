package ma.klinikus.service;

import jakarta.ws.rs.NotAuthorizedException;
import ma.klinikus.filter.PasswordVerification;
import ma.klinikus.model.Utilisateur;
import ma.klinikus.repository.UtilisateurRepository;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AuthService {

    public static final String CHALLENGE = "Basic realm=\"Klinikus API\"";
    private static final String BASIC_PREFIX = "Basic ";

    private final UtilisateurRepository utilisateurRepository = new UtilisateurRepository();

    public Utilisateur authenticate(String authHeader) {
        String[] credentials = extractCredentials(authHeader);
        String email = credentials[0];
        String password = credentials[1];

        return utilisateurRepository.findByEmail(email)
                .filter(u -> PasswordVerification.verify(password, u.getMotDePasse()))
                .orElseThrow(() -> new NotAuthorizedException("Identifiants incorrects", CHALLENGE));
    }

    private String[] extractCredentials(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BASIC_PREFIX)) {
            throw new NotAuthorizedException("Authentification requise", CHALLENGE);
        }
        try {
            String decoded = new String(
                    Base64.getDecoder().decode(authHeader.substring(BASIC_PREFIX.length()).trim()),
                    StandardCharsets.UTF_8);
            int sep = decoded.indexOf(':'); // the password may contain ':'
            if (sep < 0) {
                throw new IllegalArgumentException();
            }
            return new String[] { decoded.substring(0, sep), decoded.substring(sep + 1) };
        } catch (IllegalArgumentException e) {
            throw new NotAuthorizedException("En-tête Authorization invalide", CHALLENGE);
        }
    }
}