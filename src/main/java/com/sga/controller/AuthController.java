package com.sga.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sga.model.Usuario;
import com.sga.security.JwtService;
import com.sga.service.UsuarioService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000") // ✅ ADICIONADO: CORS para frontend
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder; // ✅ CORRIGIDO

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        UserDetails userDetails = usuarioService.loadUserByUsername(request.getUsername());
        String jwt = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    @PostMapping("/registrar")
    public ResponseEntity<Usuario> registrar(@RequestBody RegistrarRequest request) {
        Usuario usuario = usuarioService.criarUsuario(
            request.getUsername(),
            request.getPassword(),
            request.getEmail(),
            request.getNome(),
            request.getRole()
        );
        return ResponseEntity.ok(usuario);
    }

    // ✅ MÉTODO PARA CRIAR USUÁRIO DE TESTE
    @PostMapping("/criar-usuario-teste")
    public ResponseEntity<?> criarUsuarioTeste() {
        try {
            // Verifica se o usuário admin já existe
            if (usuarioService.findByUsername("admin").isPresent()) {
                return ResponseEntity.ok("Usuário admin já existe");
            }
            
            // Cria usuário admin
            Usuario usuario = new Usuario();
            usuario.setUsername("admin");
            usuario.setPassword(passwordEncoder.encode("admin123")); // ✅ AGORA FUNCIONA
            usuario.setEmail("admin@sga.com");
            usuario.setNome("Administrador");
            usuario.setRole("ADMIN");
            usuario.setAtivo(true);
            
            usuarioService.save(usuario);
            
            return ResponseEntity.ok("Usuário admin criado com sucesso! Use: admin / admin123");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao criar usuário: " + e.getMessage());
        }
    }

    @GetMapping("/teste")
    public ResponseEntity<String> teste() {
        return ResponseEntity.ok("Backend funcionando! " + System.currentTimeMillis());
    }
    
    // Inner classes SEM Lombok - com getters e setters manuais
    public static class LoginRequest {
        private String username;
        private String password;
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class RegistrarRequest {
        private String username;
        private String password;
        private String email;
        private String nome;
        private String role = "USER";
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getNome() {
            return nome;
        }
        
        public void setNome(String nome) {
            this.nome = nome;
        }
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
    }

    public static class JwtResponse {
        private String token;
        private String type = "Bearer";

        public JwtResponse(String token) {
            this.token = token;
        }
        
        public String getToken() {
            return token;
        }
        
        public void setToken(String token) {
            this.token = token;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
    }
}