package com.sga.security;

import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sga.service.UsuarioService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    @Lazy
    private UsuarioService usuarioService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 🔥 IGNORAR ENDPOINTS PÚBLICOS - NÃO TENTAR AUTENTICAR
        String path = request.getRequestURI();
        
        // 🔥 URLs que NÃO devem passar pelo filtro JWT
        if (path.startsWith("/api/auth/") || 
            path.startsWith("/api/importacao-spc/") || 
            path.startsWith("/api-docs/") || 
            path.startsWith("/swagger-ui/") ||
            path.startsWith("/actuator/")) {
            System.out.println("🔓 Ignorando filtro JWT para: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("🔓 Sem token JWT, continuando...");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwt = authHeader.substring(7);
            username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = usuarioService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                            null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("✅ Usuário autenticado via JWT: " + username);
                } else {
                    System.out.println("⚠️ Token JWT inválido para: " + username);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao processar JWT: " + e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
}