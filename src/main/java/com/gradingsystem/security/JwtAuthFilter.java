package com.gradingsystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stateless JWT filter: builds Spring Security authentication entirely from
 * token claims — no database call per request.
 *
 * Authority strings stored in the token follow the RESOURCE_ACTION convention
 * (e.g. "GRADES_CREATE"). A ROLE_ prefix authority is synthesised from the
 * role claim so that both @PreAuthorize("hasRole('ADMIN')") and
 * @PreAuthorize("hasAuthority('GRADES_CREATE')") work out of the box.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX         = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && jwtUtil.validateToken(token)) {
            String username    = jwtUtil.extractUsername(token);
            String userId      = jwtUtil.extractUserId(token);
            String role        = jwtUtil.extractRole(token);
            List<String> perms = jwtUtil.extractPermissions(token);

            // Build authority list from token claims (zero DB queries)
            List<SimpleGrantedAuthority> authorities = Stream.concat(
                Stream.of(new SimpleGrantedAuthority("ROLE_" + role)),
                perms.stream().map(SimpleGrantedAuthority::new)
            ).collect(Collectors.toList());

            UserPrincipal principal = UserPrincipal.fromToken(userId, username, authorities);

            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
