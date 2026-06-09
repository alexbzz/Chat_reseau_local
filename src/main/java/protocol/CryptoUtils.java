package protocol;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class CryptoUtils {

    // Clé partagée 16 caractères = 128 bits AES
    private static final String SECRET_KEY = "ChatSecretKey123";
    private static final String ALGORITHM  = "AES";

    /** Chiffre une chaîne en AES et retourne le résultat en Base64 */
    public static String encrypt(String plainText) {
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            System.err.println("[Crypto] Erreur chiffrement : " + e.getMessage());
            return plainText; // fallback sans chiffrement
        }
    }

    /** Déchiffre une chaîne Base64 chiffrée en AES */
    public static String decrypt(String encryptedText) {
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            return new String(cipher.doFinal(decoded));
        } catch (Exception e) {
            System.err.println("[Crypto] Erreur déchiffrement : " + e.getMessage());
            return encryptedText;
        }
    }
}