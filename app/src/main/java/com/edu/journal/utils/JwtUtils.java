package com.edu.journal.utils;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;

public class JwtUtils {
    private String privateKeyString;  // 私钥字符串
    private String keyId;            // Key ID
    private String projectId;        // 项目ID

    public JwtUtils(String privateKeyString, String keyId, String projectId){
        this.privateKeyString = privateKeyString;
        this.keyId = keyId;
        this.projectId = projectId;
    }

    // 生成JWT
    public String generateJwt() throws Exception {
        // 私钥解析
        privateKeyString = privateKeyString.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").trim();
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyString);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EdDSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        // 头部部分
        String headerJson = "{\"alg\": \"EdDSA\", \"kid\": \"" + keyId + "\"}";
        String headerEncoded = Base64.getUrlEncoder().encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));

        // 载荷部分
        long iat = ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond() - 30;
        long exp = iat + 900; // 过期时间设为签发时间之后15分钟
        String payloadJson = "{\"sub\": \"" + projectId + "\", \"iat\": " + iat + ", \"exp\": " + exp + "}";
        String payloadEncoded = Base64.getUrlEncoder().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

        // 拼接header和payload
        String data = headerEncoded + "." + payloadEncoded;

        // 签名部分
        Signature signer = Signature.getInstance("EdDSA");
        signer.initSign(privateKey);
        signer.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signature = signer.sign();

        // 将签名进行Base64url编码
        String signatureEncoded = Base64.getUrlEncoder().encodeToString(signature);

        // 最终的JWT
        return data + "." + signatureEncoded;
    }

//    public static void main(String[] args) {
//        try {
//            // 示例：私钥、Key ID 和项目ID可以替换成你自己的数据
//            String privateKeyString = "YOUR PRIVATE KEY"; // 替换为实际私钥
//            String keyId = "YOUR_KEY_ID";                 // 替换为实际Key ID
//            String projectId = "YOUR_PROJECT_ID";         // 替换为实际项目ID
//
//            JwtGenerator jwtGenerator = new JwtGenerator(privateKeyString, keyId, projectId);
//            String jwt = jwtGenerator.generateJwt();
//            System.out.println("Generated JWT: \n" + jwt);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
