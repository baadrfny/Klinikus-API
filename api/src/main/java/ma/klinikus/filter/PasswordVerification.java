package ma.klinikus.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;

public final class PasswordVerification {

    private PasswordVerification() {
    }

    public static boolean verify(String plainPassword, String bcryptHash) {
        if (plainPassword == null || bcryptHash == null) {
            return false;
        }
        try {
            return BCrypt.verifyer().verify(plainPassword.toCharArray(), bcryptHash).verified;
        } catch (IllegalArgumentException e) {

            return false;
        }
    }
}