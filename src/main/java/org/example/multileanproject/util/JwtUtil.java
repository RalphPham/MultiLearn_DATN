package org.example.multileanproject.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    /** Access Token sống 30 phút */
    private static final long ACCESS_TOKEN_EXPIRY_MS  = 1000L * 60 * 30;

    /**
     * Hàm cũ – giữ lại để không bể code đang dùng (register, các chỗ khác).
     * Vẫn tạo token 24h như trước.
     * @deprecated Dùng {@link #generateAccessToken(Map, String)} thay thế cho luồng login mới.
     */
    @Deprecated
    public String generateToken(String username) {
        return generateToken(new HashMap<>(), username);
    }

    /**
     * Hàm cũ – giữ lại tương thích.
     * @deprecated Dùng {@link #generateAccessToken(Map, String)} thay thế.
     */
    @Deprecated
    public String generateToken(Map<String, Object> extraClaims, String username) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * [MỚI] Tạo Access Token ngắn hạn – 30 phút.
     * Dùng trong luồng login mới và /refresh.
     */
    public String generateAccessToken(Map<String, Object> extraClaims, String username) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY_MS))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Overload không cần extraClaims */
    public String generateAccessToken(String username) {
        return generateAccessToken(new HashMap<>(), username);
    }

    // ── Các hàm extract / validate giữ nguyên ────────────────────────────

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    public boolean validateToken(String token, String username) {
        final String extracted = extractUsername(token);
        return extracted.equals(username) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}