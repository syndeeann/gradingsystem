package com.gradingsystem.security;

import com.gradingsystem.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Stateless JWT utility. Tokens carry userId, role, and the full
 * permissions list so downstream filters never hit the database.
 *
 * Claim keys are package-visible so JwtAuthFilter can reference them
 * without magic strings.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {

    static final String CLAIM_USER_ID    = "userId";
    static final String CLAIM_ROLE       = "role";
    static final String CLAIM_PERMISSIONS = "permissions";

    private final JwtConfig jwtConfig;

    // -----------------------------------------------------------------------
    // Token generation
    // -----------------------------------------------------------------------

    /**
     * Generates a signed JWT with userId, role, and the full permissions list
     * embedded as custom claims.
     *
     * @param userDetails  Spring Security principal (sub = username)
     * @param userId       UUID of the user row
     * @param role         role name, e.g. "ADMIN"
     * @param permissions  authority strings, e.g. ["GRADES_CREATE", "GRADES_READ"]
     */
    public String generateToken(UserDetails userDetails,
                                String userId,
                                String role,
                                List<String> permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_ROLE, role);
        claims.put(CLAIM_PERMISSIONS, permissions);
        return buildToken(claims, userDetails.getUsername());
    }

    private String buildToken(Map<String, Object> extraClaims, String subject) {
        return Jwts.builder()
            .claims(extraClaims)
            .subject(subject)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
            .signWith(signingKey())
            .compact();
    }

    // -----------------------------------------------------------------------
    // Validation
    // -----------------------------------------------------------------------

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // Claim extraction
    // -----------------------------------------------------------------------

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserId(String token) {
        return extractClaim(token, c -> c.get(CLAIM_USER_ID, String.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, c -> c.get(CLAIM_ROLE, String.class));
    }

    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        return extractClaim(token, c -> (List<String>) c.get(CLAIM_PERMISSIONS));
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    // -----------------------------------------------------------------------
    // Key
    // -----------------------------------------------------------------------

    private SecretKey signingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtConfig.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
