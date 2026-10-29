package com.sjqcf.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.Map;

public class JwtUtils {
    private static final String SECRET_KEY = "U0pRQ0Y=";//密钥
    private static final long EXPIRATION_TIME = 12 * 60 * 60 * 1000;//12小时

    public static String generateJwt(Map<String, Object> claims) {//生成JWT
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .addClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .compact();
    }

    /**
     * 使用 Jwts.parser() 创建一个 JWT 解析器。
     * 通过 .setSigningKey(SECRET_KEY) 设置签名密钥，用于验证 JWT 的签名是否有效。
     * 调用 .parseClaimsJws(jwt) 对传入的 JWT 进行解析和验证。
     * 最后调用 .getBody() 获取 JWT 中的有效载荷部分（payload），即声明信息，并将其赋值给 claims 变量返回。
     */
    public static Claims parseJwt(String jwt) {//解析JWT
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(jwt)
                .getBody();
        return claims;
    }
}
