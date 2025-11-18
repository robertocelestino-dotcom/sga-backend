package com.sga.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
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

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	// @Autowired
	// private JwtService jwtService;

	@Autowired
	@Lazy
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.cors().and()
			.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						// URLs públicas
						.antMatchers("/api/auth/**").permitAll()
						.antMatchers("/api/importacao-spc/**").permitAll()
						.antMatchers("/api/auth/criar-usuario-teste").permitAll()
						.antMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
						// URLs com controle de acesso por role
						.antMatchers("/admin/**")
							.hasRole("ADMIN")
						.antMatchers("/vendedor/**")
							.hasAnyRole("VENDEDOR", "ADMIN")
						.antMatchers("/api/financeiro/**").hasAnyRole("FINANCEIRO", "ADMIN")

						// Todas as outras URLs exigem autenticação
						.anyRequest().authenticated())
				// .addFilterBefore(jwtAuthenticationFilter(),
				// UsernamePasswordAuthenticationFilter.class);
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/*
	 * @Bean public JwtAuthenticationFilter jwtAuthenticationFilter() { return new
	 * JwtAuthenticationFilter(); }
	 */

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
