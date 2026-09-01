package com.sga.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sga.model.Menu;
import com.sga.model.Perfil;
import com.sga.model.Permissao;
import com.sga.model.Usuario;
import com.sga.security.JwtService;
import com.sga.service.LogAcessoService;
import com.sga.service.PerfilService;
import com.sga.service.UsuarioService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = { "http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:5173",
		"http://127.0.0.1:5173", "http://localhost:5174", "http://127.0.0.1:5174" })
public class AuthController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private PerfilService perfilService;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private LogAcessoService logAcessoService;

	// ============================================================
	// LOGIN
	// ============================================================
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {

		try {
			System.out.println("🔐 Tentando login para: " + request.getUsername());
			System.out.println("📝 Senha recebida: " + request.getPassword());

			// Autenticar
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

			// Carregar usuário completo
			Usuario usuario = usuarioService.findByUsername(request.getUsername())
					.orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

			System.out.println("✅ Usuário autenticado: " + usuario.getUsername());

			// Carregar perfil e permissões
			Perfil perfil = usuario.getPerfil();
			List<MenuDTO> menus = new ArrayList<>();
			List<String> permissoes = new ArrayList<>();

			if (perfil != null) {
				System.out.println("✅ Perfil encontrado: " + perfil.getNome());

				// Buscar menus do perfil
				if (perfil.getMenus() != null) {
					menus = perfil.getMenus().stream().map(this::toMenuDTO).collect(Collectors.toList());
					System.out.println("✅ Menus encontrados: " + menus.size());
				}

				// Buscar permissões do perfil
				if (perfil.getPermissoes() != null) {
					permissoes = perfil.getPermissoes().stream().map(Permissao::getNome).collect(Collectors.toList());
					System.out.println("✅ Permissões encontradas: " + permissoes.size());
				}
			} else {
				System.out.println("⚠️ Usuário sem perfil associado!");
				// Fallback: usar role do usuário
				if (usuario.getRole() != null) {
					permissoes.add("ROLE_" + usuario.getRole());
				}
			}

			// Gerar token JWT
			String token = jwtService.generateToken(usuario);

			// Registrar log de acesso
			logAcessoService.registrarAcesso(usuario.getId(), usuario.getUsername(), httpRequest.getRemoteAddr(),
					httpRequest.getHeader("User-Agent"), "LOGIN", "SUCCESS", "Login realizado com sucesso");

			// Construir resposta
			AuthResponse response = new AuthResponse();
			response.setToken(token);
			response.setTokenType("Bearer");
			response.setUsuario(
					new UsuarioDTO(usuario.getId(), usuario.getUsername(), usuario.getEmail(), usuario.getNome(),
							usuario.isAtivo(), usuario.getBloqueado() != null ? usuario.getBloqueado() : false,
							usuario.getUltimoLogin(), perfil != null ? perfil.getId() : null,
							perfil != null ? perfil.getNome() : usuario.getRole(), usuario.getRole()));
			response.setMenus(menus);
			response.setPermissoes(permissoes);

			System.out.println("✅ Login realizado com sucesso para: " + usuario.getUsername());
			System.out.println("📋 Menus retornados: " + menus.size());

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			System.err.println("❌ Erro no login: " + e.getMessage());
			e.printStackTrace();

			// Registrar log de falha
			logAcessoService.registrarAcesso(null, request.getUsername(), httpRequest.getRemoteAddr(),
					httpRequest.getHeader("User-Agent"), "LOGIN_FAILED", "FAILED", e.getMessage());

			throw e;
		}
	}

	// ============================================================
	// REGISTRAR
	// ============================================================
	@PostMapping("/registrar")
	public ResponseEntity<Usuario> registrar(@RequestBody RegistrarRequest request) {
		Usuario usuario = usuarioService.criarUsuario(request.getUsername(), request.getPassword(), request.getEmail(),
				request.getNome(), request.getRole());
		return ResponseEntity.ok(usuario);
	}

	// ============================================================
	// CRIAR USUÁRIO DE TESTE (admin123)
	// ============================================================
	@PostMapping("/criar-usuario-teste")
	public ResponseEntity<?> criarUsuarioTeste() {
		try {
			if (usuarioService.findByUsername("admin").isPresent()) {
				return ResponseEntity.ok("Usuário admin já existe");
			}

			Usuario usuario = new Usuario();
			usuario.setUsername("admin");
			usuario.setPassword(passwordEncoder.encode("admin123"));
			usuario.setEmail("admin@sga.com");
			usuario.setNome("Administrador");
			usuario.setRole("ADMIN");
			usuario.setAtivo(true);
			usuario.setBloqueado(false);
			usuario.setTentativasLogin(0);

			usuarioService.save(usuario);

			return ResponseEntity.ok("Usuário admin criado com sucesso! Use: admin / admin123");

		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Erro ao criar usuário: " + e.getMessage());
		}
	}

	// ============================================================
	// CRIAR ADMIN COM SENHA PERSONALIZADA (POST)
	// ============================================================
	@PostMapping("/create-admin-direct")
	public ResponseEntity<Map<String, Object>> createAdminDirect(@RequestBody Map<String, String> request) {
		Map<String, Object> response = new HashMap<>();
		try {
			String password = request.get("password");
			if (password == null || password.isEmpty()) {
				password = "admin@2025";
			}

			System.out.println("🔄 Criando usuário admin com senha: " + password);

			// Deletar se existir
			usuarioService.findByUsername("admin").ifPresent(u -> {
				System.out.println("🗑️ Deletando admin existente...");
				usuarioService.deletar(u.getId());
			});

			// Criar novo admin
			Usuario admin = new Usuario();
			admin.setUsername("admin");
			admin.setPassword(passwordEncoder.encode(password));
			admin.setEmail("admin@sga.com");
			admin.setNome("Administrador do Sistema");
			admin.setRole("ADMIN");
			admin.setAtivo(true);
			admin.setBloqueado(false);
			admin.setPerfilId(1L);

			// Buscar perfil - CORRIGIDO
			try {
				Perfil perfil = perfilService.buscarEntidadePorId(1L);
				if (perfil != null) {
					admin.setPerfil(perfil);
					System.out.println("✅ Perfil encontrado: " + perfil.getNome());
				}
			} catch (Exception e) {
				System.out.println("⚠️ Perfil não encontrado, continuando sem perfil...");
			}

			usuarioService.save(admin);
			System.out.println("✅ Admin criado com sucesso!");

			// Verificar se o hash foi gerado corretamente
			String hash = admin.getPassword();
			boolean isValid = hash != null && hash.startsWith("$2a$10$") && hash.length() == 60;

			response.put("success", true);
			response.put("message", "Admin criado com sucesso!");
			response.put("username", "admin");
			response.put("password", password);
			response.put("hash", hash);
			response.put("hashValid", isValid);
			response.put("hashLength", hash != null ? hash.length() : 0);

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			System.err.println("❌ Erro ao criar admin: " + e.getMessage());
			e.printStackTrace();
			response.put("success", false);
			response.put("error", e.getMessage());
			return ResponseEntity.badRequest().body(response);
		}
	}

	// ============================================================
	// CRIAR ADMIN COM SENHA PERSONALIZADA (GET)
	// ============================================================
	@GetMapping("/create-admin-quick")
	public ResponseEntity<Map<String, Object>> createAdminQuick() {
		Map<String, Object> response = new HashMap<>();
		try {
			String password = "admin@2025";
			System.out.println("🔄 Criando usuário admin com senha: " + password);

			// Deletar se existir
			usuarioService.findByUsername("admin").ifPresent(u -> {
				System.out.println("🗑️ Deletando admin existente...");
				usuarioService.deletar(u.getId());
			});

			Usuario admin = new Usuario();
			admin.setUsername("admin");
			admin.setPassword(passwordEncoder.encode(password));
			admin.setEmail("admin@sga.com");
			admin.setNome("Administrador do Sistema");
			admin.setRole("ADMIN");
			admin.setAtivo(true);
			admin.setBloqueado(false);
			admin.setPerfilId(1L);

			// Buscar perfil - CORRIGIDO
			try {
				Perfil perfil = perfilService.buscarEntidadePorId(1L);
				if (perfil != null) {
					admin.setPerfil(perfil);
					System.out.println("✅ Perfil encontrado: " + perfil.getNome());
				}
			} catch (Exception e) {
				System.out.println("⚠️ Perfil não encontrado, continuando sem perfil...");
			}

			usuarioService.save(admin);
			System.out.println("✅ Admin criado com sucesso!");

			String hash = admin.getPassword();
			boolean isValid = hash != null && hash.startsWith("$2a$10$") && hash.length() == 60;

			response.put("success", true);
			response.put("message", "Admin criado com sucesso!");
			response.put("username", "admin");
			response.put("password", password);
			response.put("hash", hash);
			response.put("hashValid", isValid);
			response.put("hashLength", hash != null ? hash.length() : 0);

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			System.err.println("❌ Erro ao criar admin: " + e.getMessage());
			e.printStackTrace();
			response.put("success", false);
			response.put("error", e.getMessage());
			return ResponseEntity.badRequest().body(response);
		}
	}

	// ============================================================
	// CRIAR ADMIN (Versão Simples - GET)
	// ============================================================
	@GetMapping("/create-admin")
	public ResponseEntity<String> createAdmin() {
		try {
			String password = "admin@2025";
			System.out.println("🔄 Criando usuário admin com senha: " + password);

			// Deletar se existir
			usuarioService.findByUsername("admin").ifPresent(u -> {
				System.out.println("🗑️ Deletando admin existente...");
				usuarioService.deletar(u.getId());
			});

			Usuario admin = new Usuario();
			admin.setUsername("admin");
			admin.setPassword(passwordEncoder.encode(password));
			admin.setEmail("admin@sga.com");
			admin.setNome("Administrador do Sistema");
			admin.setRole("ADMIN");
			admin.setAtivo(true);
			admin.setBloqueado(false);
			admin.setPerfilId(1L);

			// Buscar perfil - CORRIGIDO
			try {
				Perfil perfil = perfilService.buscarEntidadePorId(1L);
				if (perfil != null) {
					admin.setPerfil(perfil);
					System.out.println("✅ Perfil encontrado: " + perfil.getNome());
				}
			} catch (Exception e) {
				System.out.println("⚠️ Perfil não encontrado, continuando sem perfil...");
			}

			usuarioService.save(admin);
			System.out.println("✅ Admin criado com sucesso!");

			String hash = admin.getPassword();
			boolean isValid = hash != null && hash.startsWith("$2a$10$") && hash.length() == 60;

			return ResponseEntity.ok("✅ Admin criado com sucesso!\n" + "Usuário: admin\n" + "Senha: " + password + "\n"
					+ "Hash: " + hash + "\n" + "Hash válido: " + isValid);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("❌ Erro: " + e.getMessage());
		}
	}

	// ============================================================
	// GERAR HASH PARA UMA SENHA
	// ============================================================
	@GetMapping("/generate-hash")
	public ResponseEntity<String> generateHash() {
		String rawPassword = "admin123";
		String encodedPassword = passwordEncoder.encode(rawPassword);
		return ResponseEntity.ok("Novo hash para 'admin123': " + encodedPassword);
	}

	@GetMapping("/generate-hash/{password}")
	public ResponseEntity<String> generateHashForPassword(@PathVariable String password) {
		String encodedPassword = passwordEncoder.encode(password);
		return ResponseEntity.ok("Hash para '" + password + "': " + encodedPassword);
	}

	// ============================================================
	// TESTAR HASH (ATUALIZADO)
	// ============================================================
	@GetMapping("/test-hash")
	public ResponseEntity<Map<String, Object>> testHash() {
		Map<String, Object> result = new HashMap<>();

		String rawPassword = "admin@2025";

		// Verificar o usuário no banco
		Optional<Usuario> adminOpt = usuarioService.findByUsername("admin");
		if (adminOpt.isPresent()) {
			Usuario admin = adminOpt.get();
			String dbHash = admin.getPassword();
			boolean dbMatches = passwordEncoder.matches(rawPassword, dbHash);

			result.put("username", admin.getUsername());
			result.put("dbHash", dbHash);
			result.put("dbHashMatches", dbMatches);
			result.put("dbHashLength", dbHash != null ? dbHash.length() : 0);
			result.put("dbHashPrefix", dbHash != null ? dbHash.substring(0, Math.min(20, dbHash.length())) : "null");
			result.put("dbHashIsBCrypt", dbHash != null && dbHash.startsWith("$2a$10$"));
			result.put("adminExists", true);
			result.put("rawPassword", rawPassword);
		} else {
			result.put("adminExists", false);
			result.put("message", "Usuário admin não encontrado");
		}

		return ResponseEntity.ok(result);
	}

	// ============================================================
	// TESTE
	// ============================================================
	@GetMapping("/teste")
	public ResponseEntity<String> teste() {
		return ResponseEntity.ok("Backend funcionando! " + System.currentTimeMillis());
	}

	// ============================================================
	// MÉTODOS AUXILIARES
	// ============================================================
	private MenuDTO toMenuDTO(Menu menu) {
		MenuDTO dto = new MenuDTO();
		dto.setId(menu.getId());
		dto.setNome(menu.getNome());
		dto.setCaminho(menu.getCaminho());
		dto.setIcone(menu.getIcone());
		dto.setOrdem(menu.getOrdem());
		dto.setAtivo(menu.getAtivo());
		if (menu.getMenuPai() != null) {
			dto.setMenuPaiId(menu.getMenuPai().getId());
			dto.setMenuPaiNome(menu.getMenuPai().getNome());
		}
		if (menu.getSubMenus() != null && !menu.getSubMenus().isEmpty()) {
			dto.setSubMenus(menu.getSubMenus().stream().map(this::toMenuDTO).collect(Collectors.toList()));
		}
		return dto;
	}

	// ============================================================
	// INNER CLASSES
	// ============================================================

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

	public static class AuthResponse {
		private String token;
		private String tokenType = "Bearer";
		private UsuarioDTO usuario;
		private List<MenuDTO> menus;
		private List<String> permissoes;

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public String getTokenType() {
			return tokenType;
		}

		public void setTokenType(String tokenType) {
			this.tokenType = tokenType;
		}

		public UsuarioDTO getUsuario() {
			return usuario;
		}

		public void setUsuario(UsuarioDTO usuario) {
			this.usuario = usuario;
		}

		public List<MenuDTO> getMenus() {
			return menus;
		}

		public void setMenus(List<MenuDTO> menus) {
			this.menus = menus;
		}

		public List<String> getPermissoes() {
			return permissoes;
		}

		public void setPermissoes(List<String> permissoes) {
			this.permissoes = permissoes;
		}
	}

	public static class UsuarioDTO {
		private Long id;
		private String username;
		private String email;
		private String nomeCompleto;
		private Boolean ativo;
		private Boolean bloqueado;
		private java.time.LocalDateTime ultimoLogin;
		private Long perfilId;
		private String perfilNome;
		private String role;

		public UsuarioDTO() {
		}

		public UsuarioDTO(Long id, String username, String email, String nomeCompleto, Boolean ativo, Boolean bloqueado,
				java.time.LocalDateTime ultimoLogin, Long perfilId, String perfilNome, String role) {
			this.id = id;
			this.username = username;
			this.email = email;
			this.nomeCompleto = nomeCompleto;
			this.ativo = ativo;
			this.bloqueado = bloqueado;
			this.ultimoLogin = ultimoLogin;
			this.perfilId = perfilId;
			this.perfilNome = perfilNome;
			this.role = role;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getNomeCompleto() {
			return nomeCompleto;
		}

		public void setNomeCompleto(String nomeCompleto) {
			this.nomeCompleto = nomeCompleto;
		}

		public Boolean getAtivo() {
			return ativo;
		}

		public void setAtivo(Boolean ativo) {
			this.ativo = ativo;
		}

		public Boolean getBloqueado() {
			return bloqueado;
		}

		public void setBloqueado(Boolean bloqueado) {
			this.bloqueado = bloqueado;
		}

		public java.time.LocalDateTime getUltimoLogin() {
			return ultimoLogin;
		}

		public void setUltimoLogin(java.time.LocalDateTime ultimoLogin) {
			this.ultimoLogin = ultimoLogin;
		}

		public Long getPerfilId() {
			return perfilId;
		}

		public void setPerfilId(Long perfilId) {
			this.perfilId = perfilId;
		}

		public String getPerfilNome() {
			return perfilNome;
		}

		public void setPerfilNome(String perfilNome) {
			this.perfilNome = perfilNome;
		}

		public String getRole() {
			return role;
		}

		public void setRole(String role) {
			this.role = role;
		}
	}

	public static class MenuDTO {
		private Long id;
		private String nome;
		private String caminho;
		private String icone;
		private Integer ordem;
		private Boolean ativo;
		private Long menuPaiId;
		private String menuPaiNome;
		private List<MenuDTO> subMenus;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getNome() {
			return nome;
		}

		public void setNome(String nome) {
			this.nome = nome;
		}

		public String getCaminho() {
			return caminho;
		}

		public void setCaminho(String caminho) {
			this.caminho = caminho;
		}

		public String getIcone() {
			return icone;
		}

		public void setIcone(String icone) {
			this.icone = icone;
		}

		public Integer getOrdem() {
			return ordem;
		}

		public void setOrdem(Integer ordem) {
			this.ordem = ordem;
		}

		public Boolean getAtivo() {
			return ativo;
		}

		public void setAtivo(Boolean ativo) {
			this.ativo = ativo;
		}

		public Long getMenuPaiId() {
			return menuPaiId;
		}

		public void setMenuPaiId(Long menuPaiId) {
			this.menuPaiId = menuPaiId;
		}

		public String getMenuPaiNome() {
			return menuPaiNome;
		}

		public void setMenuPaiNome(String menuPaiNome) {
			this.menuPaiNome = menuPaiNome;
		}

		public List<MenuDTO> getSubMenus() {
			return subMenus;
		}

		public void setSubMenus(List<MenuDTO> subMenus) {
			this.subMenus = subMenus;
		}
	}
}