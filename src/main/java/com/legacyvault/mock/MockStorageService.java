package com.legacyvault.mock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

/**
 * Mock文件存储服务
 * 模拟Arweave/S3永久存储，实际将加密文件存储在本地磁盘
 *
 * 【Mock模式】使用本地文件系统存储密文
 * 【切换正式】替换为Arweave SDK / AWS S3 SDK
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class MockStorageService {

    @Value("${legacy-vault.storage.local-path:./uploads/encrypted}")
    private String localPath;

    /**
     * 上传加密文件
     *
     * @param userId       用户ID
     * @param encryptedData Base64编码的加密数据
     * @param fileName     原始文件名（可选）
     * @return 存储路径/CID
     */
    public String uploadEncryptedFile(Long userId, String encryptedData, String fileName) {
        // 生成本地存储路径
        String fileId = UUID.randomUUID().toString().replace("-", "");
        String subDir = localPath + "/" + userId;
        String filePath = subDir + "/" + fileId;

        try {
            // 创建目录
            File dir = new File(subDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 写入文件（Base64解码后存储）
            byte[] data = Base64.getDecoder().decode(encryptedData);
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(data);
            }

            log.info("【Mock存储】上传成功 | 用户ID={} | 路径={} | 大小={}bytes", userId, filePath, data.length);

            /*
             * ========== 正式接口预留（Arweave） ==========
             * 切换步骤：
             * 1. 引入依赖：arweave-client
             * 2. 创建Arweave钱包（JWK）
             * 3. 创建Transaction → 设置Data → 签名 → 发送
             *
             * Arweave arweave = Arweave.getInstance("arweave.net", 443, "https");
             * Transaction tx = arweave.transactions().createTransaction(jwk, data);
             * arweave.transactions().sign(tx, jwk);
             * arweave.transactions().post(tx);
             * return tx.getId(); // 返回Arweave CID
             */

            /*
             * ========== 备选正式接口（AWS S3） ==========
             * S3Client s3 = S3Client.builder().build();
             * s3.putObject(PutObjectRequest.builder()
             *     .bucket("legacy-vault-encrypted")
             *     .key(userId + "/" + fileId)
             *     .build(), RequestBody.fromBytes(data));
             */

            return filePath;

        } catch (IOException e) {
            log.error("【Mock存储】上传失败 | 用户ID={} | 错误={}", userId, e.getMessage());
            throw new RuntimeException("文件存储失败", e);
        }
    }

    /**
     * 下载加密文件
     *
     * @param storagePath 存储路径
     * @return Base64编码的加密数据
     */
    public String downloadEncryptedFile(String storagePath) {
        try {
            File file = new File(storagePath);
            if (!file.exists()) {
                log.warn("【Mock存储】文件不存在 | 路径={}", storagePath);
                return null;
            }

            byte[] data = new byte[(int) file.length()];
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                fis.read(data);
            }

            String base64 = Base64.getEncoder().encodeToString(data);
            log.info("【Mock存储】下载成功 | 路径={} | 大小={}bytes", storagePath, data.length);
            return base64;

        } catch (IOException e) {
            log.error("【Mock存储】下载失败 | 路径={} | 错误={}", storagePath, e.getMessage());
            return null;
        }
    }

    /**
     * 删除加密文件
     */
    public boolean deleteFile(String storagePath) {
        File file = new File(storagePath);
        if (file.exists()) {
            boolean deleted = file.delete();
            log.info("【Mock存储】删除{} | 路径={}", deleted ? "成功" : "失败", storagePath);
            return deleted;
        }
        return true;
    }
}
