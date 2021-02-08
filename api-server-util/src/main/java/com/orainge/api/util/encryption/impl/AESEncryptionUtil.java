package com.orainge.api.util.encryption.impl;

import com.orainge.api.util.encryption.EncryptionUtil;
import org.apache.commons.codec.binary.Base64;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * AES加解密工具<br/>
 * 如果有自定义的实现，可以继承 EncryptionUtil 类后创建 Bean
 *
 * @author Eason Huang
 * @date 2021/1/22
 */
@Component
@ConditionalOnMissingBean(EncryptionUtil.class)
public class AESEncryptionUtil implements EncryptionUtil {
    @Override
    public String encrypt(String str, String key) {
        try {
            return aesEncrypt(str, key);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String decrypt(String str, String key) {
        try {
            return aesDecrypt(str, key);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * AES加密为base 64 code
     *
     * @param content    待加密的内容
     * @param encryptKey 加密密钥
     * @return 加密后的 base 64 code
     */
    public static String aesEncrypt(String content, String encryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        /*防止linux下 随机生成key*/
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(encryptKey.getBytes());
        kgen.init(128, random);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));

        byte[] encryptBytes = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeBase64String(encryptBytes);
    }

    /**
     * 将base 64 code AES解密
     *
     * @param encryptStr 待解密的base 64 code
     * @param decryptKey 解密密钥
     * @return 解密后的string
     */
    public static String aesDecrypt(String encryptStr, String decryptKey) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            /*防止linux下 随机生成key*/
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(decryptKey.getBytes());
            kgen.init(128, random);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(kgen.generateKey().getEncoded(), "AES"));
            byte[] decryptBytes = cipher.doFinal(Base64.decodeBase64(encryptStr));
            return new String(decryptBytes, StandardCharsets.UTF_8);
        } catch (Exception ignore) {
        }
        return "";
    }
}