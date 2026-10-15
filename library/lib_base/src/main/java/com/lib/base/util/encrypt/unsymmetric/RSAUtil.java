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
 * Last modified 2017-09-07 17:30:37
 *
 * GitHub: https://github.com/GcsSloop
 * WeiBo: http://weibo.com/GcsSloop
 * WebSite: http://www.gcssloop.com
 */

package com.lib.base.util.encrypt.unsymmetric;


import com.lib.base.util.encrypt.base.Base64;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

import androidx.annotation.IntDef;

/**
 * RSA 工具类
 * <p>
 * 加密方式固定为 RSA/ECB/PKCS1Padding，与 Java 服务端默认的 "RSA" 一致。
 * 不能写 Cipher.getInstance("RSA")：Android 上默认是 NoPadding；也不能指定 "BC" 提供者：Android 9 起会抛 NoSuchAlgorithmException
 */
public class RSAUtil {
    private static final String ALGORITHM = "RSA";
    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    /** PKCS1Padding 每段明文最多 模长 - 11 字节 */
    private static final int PKCS1_PADDING_SIZE = 11;

    /**
     * 随机获取密钥(公钥和私钥), 客户端公钥加密，服务器私钥解密
     *
     * @return 结果密钥对
     * @throws Exception 异常
     */
    public static Map<String, Object> getKeyPair() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(ALGORITHM);
        keyPairGen.initialize(2048);

        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        Map<String, Object> keyMap = new HashMap<>(2);
        keyMap.put("RSAPublicKey", publicKey);
        keyMap.put("RSAPrivateKey", privateKey);
        return keyMap;
    }

    /**
     * 获取公钥/私钥
     *
     * @param keyMap      密钥对
     * @param isPublicKey true：获取公钥，false：获取私钥
     * @return 获取密钥字符串
     */
    public static String getKey(Map<String, Object> keyMap, boolean isPublicKey) {
        Key key = (Key) keyMap.get(isPublicKey ? "RSAPublicKey" : "RSAPrivateKey");
        return new String(Base64.encode(key.getEncoded(), Base64.DEFAULT));
    }

    /**
     * 获取数字签名
     *
     * @param data       二进制位
     * @param privateKey 私钥(BASE64编码)
     * @return 数字签名结果字符串
     * @throws Exception 异常
     */
    public static String sign(byte[] data, String privateKey) throws Exception {
        byte[] keyBytes = Base64.decode(privateKey.getBytes(), Base64.DEFAULT);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);

        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateK);
        signature.update(data);
        return new String(Base64.encode(signature.sign(), Base64.DEFAULT));
    }

    /**
     * 数字签名校验
     *
     * @param data      二进位组
     * @param publicKey 公钥(BASE64编码)
     * @param sign      数字签名字符串
     * @return true：校验成功，false：校验失败
     * @throws Exception 异常
     */
    public static boolean verify(byte[] data, String publicKey, String sign) throws Exception {
        byte[] keyBytes = Base64.decode(publicKey.getBytes(), Base64.DEFAULT);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        PublicKey publicK = keyFactory.generatePublic(keySpec);

        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicK);
        signature.update(data);
        return signature.verify(Base64.decode(sign.getBytes(), Base64.DEFAULT));
    }


    /**
     * Rsa公钥加密类型
     */
    public static final int RSA_PUBLIC_ENCRYPT = 0;

    /**
     * Rsa公钥解密类型
     */
    public static final int RSA_PUBLIC_DECRYPT = 1;

    /**
     * Rsa私钥加密类型
     */
    public static final int RSA_PRIVATE_ENCRYPT = 2;

    /**
     * Rsa私钥解密类型
     */
    public static final int RSA_PRIVATE_DECRYPT = 3;

    @IntDef({RSA_PUBLIC_ENCRYPT, RSA_PUBLIC_DECRYPT, RSA_PRIVATE_ENCRYPT, RSA_PRIVATE_DECRYPT})
    @interface RSAType {}

    /**
     * Rsa加密/解密（一般情况下，公钥加密私钥解密）
     *
     * @param data   源数据
     * @param string 密钥(BASE64编码)
     * @param type   操作类型：{@link #RSA_PUBLIC_ENCRYPT}，{@link #RSA_PUBLIC_DECRYPT
     *               }，{@link #RSA_PRIVATE_ENCRYPT}，{@link #RSA_PRIVATE_DECRYPT}
     * @return 加密/解密结果字符串
     * @throws Exception 异常
     */
    public static byte[] rsa(byte[] data, String string, @RSAType int type) throws Exception {
        byte[] keyBytes = Base64.decode(string, Base64.DEFAULT);

        Key key;
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        if (type == RSA_PUBLIC_ENCRYPT || type == RSA_PUBLIC_DECRYPT) {
            key = keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
        } else {
            key = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        }

        boolean encrypt = type == RSA_PUBLIC_ENCRYPT || type == RSA_PRIVATE_ENCRYPT;
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, key);
        //分段长度由密钥长度决定：加密每段 模长-11，解密每段 模长
        int modulusBytes = (((RSAKey) key).getModulus().bitLength() + 7) / 8;
        int blockSize = encrypt ? modulusBytes - PKCS1_PADDING_SIZE : modulusBytes;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int offset = 0; offset < data.length; offset += blockSize) {
            int len = Math.min(blockSize, data.length - offset);
            out.write(cipher.doFinal(data, offset, len));
        }
        return out.toByteArray();
    }
}
