package com.sga.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.sga.security.JwtAuthenticationFilter;
import com.sga.service.UsuarioService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    @Lazy
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private UsuarioService usuarioService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 🔥 CORS é configurado em CorsConfig.java (fonte da verdade única)
            .cors(cors -> {})
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ============================================================
                // 🔓 ENDPOINTS PÚBLICOS
                // ============================================================
                .antMatchers("/api/auth/login").permitAll()
                .antMatchers("/api/auth/teste").permitAll()
                .antMatchers("/actuator/health").permitAll()

                // ============================================================
                // 🔒 SWAGGER — exige permissão SWAGGER_VIEW
                // ============================================================
                .antMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                        .hasAuthority("SWAGGER_VIEW")

                // ============================================================
                // 🔒 ADMIN
                // ============================================================
                .antMatchers("/admin/**")
                        .hasAnyAuthority("USUARIO_VIEW", "PERFIL_VIEW")
                .antMatchers("/api/admin/**")
                        .hasAnyAuthority(
                                "USUARIO_VIEW", "PERFIL_VIEW",
                                "PERFIL_CREATE", "PERFIL_EDIT", "PERFIL_DELETE")
                .antMatchers("/vendedor/**")
                        .hasAnyAuthority("VENDEDOR_VIEW", "VENDEDOR_CREATE")
                .antMatchers("/api/financeiro/**")
                        .hasAnyAuthority("FATURA_VIEW", "FATURA_PROCESS")

                // ============================================================
                // 🔒 TODO O RESTO — autenticado
                // ============================================================
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(usuarioService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2A, 10);
    }
}