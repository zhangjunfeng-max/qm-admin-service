package com.qm.admin.system.security;

import com.qm.admin.common.exception.BusinessException;
import com.qm.admin.common.response.CommonResultCode;
import com.qm.admin.system.config.SystemAuthProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * 使用 JDK 标准 RSA/JCE 解密前端通过 jsencrypt（RSA/ECB/PKCS1Padding）加密的登录密码。
 * 私钥只从后端受保护配置读取，绝不返回前端或写入日志。
 */
@Component
public class RsaPasswordDecryptor {

    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final String PRIVATE_KEY_BEGIN = "-----BEGIN PRIVATE KEY-----";
    private static final String PRIVATE_KEY_END = "-----END PRIVATE KEY-----";

    private final SystemAuthProperties authProperties;

    public RsaPasswordDecryptor(SystemAuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public String decrypt(String encryptedPassword) {
        if (encryptedPassword == null || encryptedPassword.isBlank()) {
            throw unauthorized();
        }
        try {
            PrivateKey privateKey = loadPrivateKey(authProperties.getRsaPrivateKey());
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedPassword);
            return new String(cipher.doFinal(encryptedBytes), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            // 对外统一返回登录失败，避免暴露密钥格式、解密过程或密码明文信息。
            throw unauthorized();
        }
    }

    private PrivateKey loadPrivateKey(String pem) throws Exception {
        if (pem == null || pem.isBlank()) {
            throw new IllegalStateException("RSA private key is not configured");
        }
        String normalized = pem.replace("\\n", "\n")
                .replace(PRIVATE_KEY_BEGIN, "")
                .replace(PRIVATE_KEY_END, "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(normalized);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    private BusinessException unauthorized() {
        return new BusinessException(CommonResultCode.UNAUTHORIZED, "用户名或密码错误");
    }
}
