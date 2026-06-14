package com.legacyvault.mock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Mock区块链存证服务
 * 模拟将操作哈希写入以太坊/Polygon区块链
 *
 * 【Mock模式】生成模拟交易哈希，记录存证日志
 * 【切换正式】替换为Web3j调用Polygon智能合约
 *
 * @author LegacyVault
 */
@Slf4j
@Service
public class MockBlockchainService {

    /**
     * 将操作记录上链存证
     *
     * @param userId      用户ID
     * @param operationType 操作类型
     * @param contentHash 内容哈希
     * @return 存证结果（包含交易哈希）
     */
    public Map<String, Object> storeOnChain(Long userId, String operationType, String contentHash) {
        log.info("【Mock区块链】存证 | 用户ID={} | 操作={} | 哈希={}", userId, operationType, contentHash);

        // Mock：生成模拟交易哈希
        String txHash = "0x" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "").substring(0, 32);

        Map<String, Object> result = new HashMap<>();
        result.put("txHash", txHash);
        result.put("blockNumber", 10000000 + (int)(System.currentTimeMillis() % 1000000));
        result.put("network", "polygon-mock");
        result.put("timestamp", System.currentTimeMillis());
        result.put("status", "CONFIRMED");
        result.put("message", "Mock模式：模拟存证成功");

        log.info("【Mock区块链】存证成功 | txHash={}", txHash);

        /*
         * ========== 正式接口预留（Web3j + Polygon） ==========
         * 切换步骤：
         * 1. 引入依赖：web3j-core
         * 2. 连接Polygon节点（Infura/Alchemy/自建）
         * 3. 加载智能合约ABI和字节码
         * 4. 调用合约方法 storeEvidence(userId, operationType, contentHash)
         *
         * Web3j web3j = Web3j.build(new HttpService("https://polygon-rpc.com"));
         * LegacyVaultContract contract = LegacyVaultContract.load(
         *     contractAddress, web3j, credentials, StaticGasProvider);
         * TransactionReceipt receipt = contract.storeEvidence(
         *     BigInteger.valueOf(userId), operationType, contentHash).send();
         * result.put("txHash", receipt.getTransactionHash());
         */
        return result;
    }

    /**
     * 验证链上存证
     *
     * @param txHash 交易哈希
     * @return 存证信息
     */
    public Map<String, Object> verifyOnChain(String txHash) {
        log.info("【Mock区块链】验证存证 | txHash={}", txHash);

        Map<String, Object> result = new HashMap<>();
        result.put("txHash", txHash);
        result.put("valid", true);
        result.put("network", "polygon-mock");
        result.put("message", "Mock模式：存证验证通过");
        return result;
    }
}
