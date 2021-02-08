package com.orainge.api.util.encryption;

/**
 * 加解密工具接口
 *
 * @author Eason Huang
 * @date 2021/1/22
 */
public interface EncryptionUtil {
    /**
     * 加密
     *
     * @param str 待加密字符串
     * @param key 密钥
     * @return 加密后的字符串，如果加密失败则返回 null
     */
    public String encrypt(String str, String key);

    /**
     * 解密
     *
     * @param str 待解密字符串
     * @param key 密钥
     * @return 解密后的字符串，如果解密失败则返回 null
     */
    public String decrypt(String str, String key);
}
