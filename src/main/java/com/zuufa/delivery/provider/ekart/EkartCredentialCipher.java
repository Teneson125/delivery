package com.zuufa.delivery.provider.ekart;

import com.zuufa.exception.ServiceException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EkartCredentialCipher {
    private static final String PREFIX = "v1:";
    private final byte[] key;
    private final SecureRandom random = new SecureRandom();

    public EkartCredentialCipher(@Value("${delivery.providers.ekart.credential-key:}") String encodedKey) {
        key = encodedKey.isBlank() ? null : Base64.getDecoder().decode(encodedKey);
        if (key != null && key.length != 32) throw new IllegalArgumentException("Ekart credential key must decode to 32 bytes");
    }

    public boolean configured() { return key != null; }

    public String encrypt(String value) {
        requireKey();
        try {
            byte[] iv = new byte[12];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return PREFIX + Base64.getEncoder().encodeToString(ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array());
        } catch (Exception e) { throw new ServiceException(500, "Unable to protect Ekart credentials"); }
    }

    public String decrypt(String value) {
        if (value == null) return null;
        // Existing installations stored JSON in this column. Re-encrypt on the next settings save.
        if (value.stripLeading().startsWith("{")) return value;
        requireKey();
        try {
            if (!value.startsWith(PREFIX)) throw new IllegalArgumentException();
            byte[] data = Base64.getDecoder().decode(value.substring(PREFIX.length()));
            if (data.length < 29) throw new IllegalArgumentException();
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, data, 0, 12));
            return new String(cipher.doFinal(data, 12, data.length - 12), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new ServiceException(500, "Unable to read Ekart credentials"); }
    }

    private void requireKey() {
        if (key == null) throw new ServiceException(503, "Ekart credential encryption key is not configured on the server");
    }
}
