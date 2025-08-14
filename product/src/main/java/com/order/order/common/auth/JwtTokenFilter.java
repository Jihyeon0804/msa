package com.order.order.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class JwtTokenFilter extends GenericFilter {

    @Value("${jwt.secretKeyAt}")
    private String secretKeyAt;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken == null) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }

            // Auth Type이 Bearer Token 인 경우 토큰 앞에 bearer이 붙어서 나가기 때문에 잘라주어야 함.
            String token = bearerToken.substring(7);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKeyAt)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role")));

            Authentication authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
