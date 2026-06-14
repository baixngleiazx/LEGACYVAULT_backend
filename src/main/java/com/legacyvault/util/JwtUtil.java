package com.legacyvault.util;

import com.legacyvault.config.LegacyVaultProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 用于生成和解析JWT Token
 *
 * @author LegacyVault
 */
@Slf4j
@Component
public class JwtUtil {

    @Autowired
    private LegacyVaultProperties properties;

    /**
     * 生成JWT Token
     *
     * @param userId 用户ID
     * @return JWT Token字符串
     */
    public String generateToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        long expireMs = properties.getJwt().getExpireHours() * 3600 * 1000L;
        Date expireDate = new Date(System.currentTimeMillis() + expireMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, properties.getJwt().getSecret())
                .compact();
    }

    /**
     * 解析JWT Token，获取用户ID
     *
     * @param token JWT Token字符串
     * @return 用户ID，解析失败返回null
     */
    public Long parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(properties.getJwt().getSecret())
                    .parseClaimsJws(token)
                    .getBody();
            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            log.warn("JWT解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 生成一次性交付链接JWT（24小时有效）
     *
     * @param deliveryLinkId 交付链接ID
     * @param heirId         继承人ID
     * @return JWT Token
     */
    public String generateDeliveryToken(Long deliveryLinkId, Long heirId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("deliveryLinkId", deliveryLinkId);
        claims.put("heirId", heirId);
        claims.put("type", "delivery");

        // 交付链接24小时有效
        Date expireDate = new Date(System.currentTimeMillis() + 24 * 3600 * 1000L);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(heirId))
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, properties.getJwt().getSecret())
                .compact();
    }

    /**
     * 验证Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(properties.getJwt().getSecret())
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
