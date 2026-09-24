package com.qm.admin;

import com.qm.admin.system.config.SystemAuthProperties;
import com.qm.admin.system.security.RsaPasswordDecryptor;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RsaPasswordDecryptorTests {

    @Test
    void decryptsJseEncryptCompatiblePkcs1Ciphertext() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();

        SystemAuthProperties properties = new SystemAuthProperties();
        properties.setRsaPrivateKey("-----BEGIN PRIVATE KEY-----\\n"
                + Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded())
                + "\\n-----END PRIVATE KEY-----");
        RsaPasswordDecryptor decryptor = new RsaPasswordDecryptor(properties);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
        String encrypted = Base64.getEncoder().encodeToString(
                cipher.doFinal("admin123".getBytes(StandardCharsets.UTF_8)));

        assertEquals("admin123", decryptor.decrypt(encrypted));
    }
}
