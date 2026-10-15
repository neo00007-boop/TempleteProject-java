/*
 * Copyright 2017 GcsSloop
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last modified 2017-09-07 17:30:16
 *
 * GitHub: https://github.com/GcsSloop
 * WeiBo: http://weibo.com/GcsSloop
 * WebSite: http://www.gcssloop.com
 */

package com.lib.base.util.encrypt.symmetric;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import static com.lib.base.util.encrypt.base.BaseUtils.parseByte2HexStr;
import static com.lib.base.util.encrypt.base.BaseUtils.parseHexStr2Byte;


/**
 * AES 工具类
 * <p>
 * 密钥用 PBKDF2 从密码派生，加密用 AES-GCM（自带完整性校验，密码错或密文被改都会解密失败）。
 * 每次加密随机生成盐和 IV，同样的明文每次密文都不同；密文格式：十六进制(盐16字节 + IV12字节 + 密文)
 */
public class AESUtil {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    //minSdk 24 不支持 PBKDF2WithHmacSHA256（API 26 起才有）
    private static final String KEY_DERIVATION = "PBKDF2WithHmacSHA1";
    private static final int ITERATIONS = 10000;
    private static final int KEY_BITS = 256;
    private static final int SALT_BYTES = 16;
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    @IntDef({Cipher.ENCRYPT_MODE, Cipher.DECRYPT_MODE})
    @interface AESType {}

    /**
     * Aes加密/解密
     *
     * @param content  加密时为明文，解密时为 aes 加密得到的十六进制字符串
     * @param password 密码
     * @param type     加密：{@link Cipher#ENCRYPT_MODE}，解密：{@link Cipher#DECRYPT_MODE}
     * @return 加密/解密结果字符串，失败返回 null
     */
    @Nullable
    public static String aes(String content, String password, @AESType int type) {
        if (content == null || password == null) {
            return null;
        }
        try {
            return type == Cipher.ENCRYPT_MODE ? encrypt(content, password) : decrypt(content, password);
        } catch (GeneralSecurityException | RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String encrypt(String content, String password) throws GeneralSecurityException {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_BYTES];
        byte[] iv = new byte[IV_BYTES];
        random.nextBytes(salt);
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, deriveKey(password, salt), new GCMParameterSpec(TAG_BITS, iv));
        byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));

        ByteBuffer buffer = ByteBuffer.allocate(SALT_BYTES + IV_BYTES + encrypted.length);
        buffer.put(salt).put(iv).put(encrypted);
        return parseByte2HexStr(buffer.array());
    }

    private static String decrypt(String content, String password) throws GeneralSecurityException {
        byte[] all = parseHexStr2Byte(content);
        if (all == null || all.length <= SALT_BYTES + IV_BYTES) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(all);
        byte[] salt = new byte[SALT_BYTES];
        byte[] iv = new byte[IV_BYTES];
        byte[] encrypted = new byte[all.length - SALT_BYTES - IV_BYTES];
        buffer.get(salt).get(iv).get(encrypted);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, deriveKey(password, salt), new GCMParameterSpec(TAG_BITS, iv));
        return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
    }

    private static SecretKeySpec deriveKey(String password, byte[] salt) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_BITS);
        try {
            byte[] key = SecretKeyFactory.getInstance(KEY_DERIVATION).generateSecret(spec).getEncoded();
            return new SecretKeySpec(key, "AES");
        } finally {
            spec.clearPassword();
        }
    }
}
