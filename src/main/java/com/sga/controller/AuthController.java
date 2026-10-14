package com.sga.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sga.model.Menu;
import com.sga.model.Perfil;
import com.sga.model.Permissao;
import com.sga.model.Usuario;
import com.sga.repository.MenuRepository;
import com.sga.security.JwtService;
import com.sga.service.LogAcessoService;
import com.sga.service.PerfilService;
import com.sga.service.UsuarioService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = { "http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:5173",
		"http://127.0.0.1:5173", "http://localhost:5174", "http://127.0.0.1:5174" })
public class AuthController {

	private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private PerfilService perfilService;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

	@Autowired
	private LogAcessoService logAcessoService;

	@Autowired
	private com.sga.service.SessaoService sessaoService;

	// 🔥 NOVO — repositório dedicado para buscar menus (evita LAZY)
	@Autowired
	private MenuRepository menuRepository;

	// ============================================================
	// LOGIN
	//
	// 🔥 OTIMIZAÇÕES (sem alterar contrato):
	//   1) Busca usuário + perfil + permissões em 1 query (JOIN FETCH)
	//   2) Busca menus com LEFT JOIN FETCH m.menuPai (sem LAZY fora de tx)
	//   3) Constrói árvore de menus em memória (sem N+1)
	// ============================================================
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {

		final String ip = httpRequest.getRemoteAddr();
		final String userAgent = httpRequest.getHeader("User-Agent");

		try {

			logger.info("🔐 Tentando login para: {}", request.getUsername());

			// ============================================================
			// 🔥 VERIFICAÇÃO MANUAL DE BLOQUEIO (antes de autenticar)
			// ============================================================
			Optional<Usuario> usuarioOpt = usuarioService.findByUsername(request.getUsername());
			if (usuarioOpt.isPresent() && usuarioOpt.get().estaBloqueado()) {
				logger.warn("🔒 Tentativa de login em conta bloqueada: {}", request.getUsername());

				logAcessoService.registrarAcesso(null, request.getUsername(), ip, userAgent, "LOGIN_LOCKED", "FAILED",
						"Conta bloqueada");

				Map<String, Object> body = new HashMap<>();
				body.put("error", "Conta bloqueada");
				body.put("message", "Sua conta está bloqueada. Contate o administrador.");
				return ResponseEntity.status(HttpStatus.LOCKED).body(body);
			}

			// ============================================================
			// AUTENTICAÇÃO
			// ============================================================
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

			// 🔥 Carrega usuário com perfil + permissões em 1 query
			//    (evita LazyInitializationException em getAuthorities())
			Usuario usuario = usuarioService.findByUsernameWithPerfil(request.getUsername())
					.orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

			logger.info("✅ Usuário autenticado: {}", usuario.getUsername());

			// 🔥 Reseta tentativas de login + atualiza último login/IP
			usuarioService.atualizarUltimoLogin(usuario.getId(), ip);

			// ============================================================
			// CARREGAR PERFIL + MENUS (2 queries separadas e seguras)
			//
			// - Menus: MenuRepository.findMenusByPerfilIdComPai() → List<Menu>
			//          (uma única query, com menuPai já carregado)
			// - Permissões: PerfilRepository.findByIdComPermissoes() → Set<Permissao>
			//
			// ⚠️ NÃO usamos mais perfil.getMenus() porque:
			//    1) A coleção é LAZY e fora de transação pode vir vazia
			//    2) O JOIN FETCH em p.menus + p.permissoes gera produto cartesiano
			//    3) A deduplicação do Hibernate em Set<> pode perder itens
			// ============================================================
			Long perfilId = usuario.getPerfilId() != null
					? usuario.getPerfilId()
					: (usuario.getPerfil() != null ? usuario.getPerfil().getId() : null);

			Perfil perfil = null;
			List<Menu> menusDoPerfil = new ArrayList<>();
			List<MenuDTO> menus = new ArrayList<>();
			List<String> permissoes = new ArrayList<>();

			if (perfilId != null) {
				// 🔥 Query 1: menus do perfil (com menuPai carregado)
				menusDoPerfil = menuRepository.findMenusByPerfilIdComPai(perfilId);

				// 🔥 Query 2: perfil com as permissões
				perfil = perfilService.findByIdComPermissoes(perfilId).orElse(null);

				logger.info("🔍 [LOGIN] perfilId={} | menusDoPerfil={} | perfil={}",
						perfilId, menusDoPerfil.size(),
						perfil != null ? perfil.getNome() : "null");
			}

			if (perfil != null) {
				logger.info("✅ Perfil encontrado: {}", perfil.getNome());

				// 🔥 Constrói a árvore em memória a partir da lista flat
				if (!menusDoPerfil.isEmpty()) {
					menus = construirArvoreMenusDeLista(menusDoPerfil);
					logger.info("🔍 [LOGIN] Árvore construída: {} raízes", menus.size());
				}

				// 🔥 Permissões
				if (perfil.getPermissoes() != null) {
					permissoes = perfil.getPermissoes().stream()
							.map(Permissao::getNome)
							.collect(Collectors.toList());
				}
			} else {
				logger.warn("⚠️ Usuário sem perfil associado!");
				if (usuario.getRole() != null) {
					permissoes.add("ROLE_" + usuario.getRole());
				}
			}

			// Gerar token JWT
			String token = jwtService.generateToken(usuario);

			// 🔥 Cria a sessão no banco (permite revogação futura)
			LocalDateTime expiracao = LocalDateTime.now().plusNanos(jwtService.getExpirationTime() * 1_000_000);
			sessaoService.criarSessao(usuario.getId(), token, ip, userAgent, expiracao);

			// Registrar log de acesso
			logAcessoService.registrarAcesso(usuario.getId(), usuario.getUsername(), ip, userAgent, "LOGIN", "SUCCESS",
					"Login realizado com sucesso");

			// Construir resposta
			AuthResponse response = new AuthResponse();
			response.setToken(token);
			response.setTokenType("Bearer");

			UsuarioDTO usuarioDTO = new UsuarioDTO(
					usuario.getId(),
					usuario.getUsername(),
					usuario.getEmail(),
					usuario.getNome(),
					usuario.isAtivo(),
					usuario.getBloqueado() != null ? usuario.getBloqueado() : false,
					usuario.getUltimoLogin(),
					perfil != null ? perfil.getId() : null,
					perfil != null ? perfil.getNome() : usuario.getRole(),
					usuario.getRole());
			usuarioDTO.setFotoBase64(usuario.getFotoBase64());
			response.setUsuario(usuarioDTO);

			response.setMenus(menus);
			response.setPermissoes(permissoes);

			logger.info("✅ Login OK: {} | menus={} | permissões={}", usuario.getUsername(), menus.size(),
					permissoes.size());

			return ResponseEntity.ok(response);

		} catch (BadCredentialsException e) {
			// ============================================================
			// CREDENCIAIS INVÁLIDAS — incrementa tentativas
			// ============================================================
			int tentativas = usuarioService.incrementarTentativasLogin(request.getUsername());
			int restantes = Math.max(0, UsuarioService.MAX_TENTATIVAS_LOGIN - tentativas);

			logger.warn("❌ Credenciais inválidas para: {} | tentativa {}/{}", request.getUsername(), tentativas,
					UsuarioService.MAX_TENTATIVAS_LOGIN);

			logAcessoService.registrarAcesso(null, request.getUsername(), ip, userAgent, "LOGIN_FAILED", "FAILED",
					"Tentativa " + tentativas + "/" + UsuarioService.MAX_TENTATIVAS_LOGIN);

			if (tentativas >= UsuarioService.MAX_TENTATIVAS_LOGIN) {
				Map<String, Object> body = new HashMap<>();
				body.put("error", "Conta bloqueada");
				body.put("message", "Após " + UsuarioService.MAX_TENTATIVAS_LOGIN
						+ " tentativas incorretas, sua conta foi bloqueada. Contate o administrador.");
				body.put("tentativasRestantes", 0);
				return ResponseEntity.status(HttpStatus.LOCKED).body(body);
			}

			Map<String, Object> body = new HashMap<>();
			body.put("error", "Credenciais inválidas");
			body.put("message", "Usuário ou senha incorretos.");
			body.put("tentativasRestantes", restantes);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);

		} catch (Exception e) {
			logger.error("❌ Erro inesperado no login: {}", e.getMessage(), e);

			logAcessoService.registrarAcesso(null, request.getUsername(), ip, userAgent, "LOGIN_ERROR", "FAILED",
					e.getMessage());

			Map<String, Object> body = new HashMap<>();
			body.put("error", "Erro no login");
			body.put("message", "Não foi possível autenticar. Tente novamente.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
		}
	}

	// ============================================================
	// TROCAR MINHA SENHA — qualquer usuário autenticado troca a própria
	// ============================================================
	@PostMapping("/me/trocar-senha")
	public ResponseEntity<?> trocarMinhaSenha(@RequestBody TrocarSenhaRequest request, HttpServletRequest httpRequest) {
		final String ip = httpRequest.getRemoteAddr();
		final String userAgent = httpRequest.getHeader("User-Agent");

		try {
			String authHeader = httpRequest.getHeader("Authorization");
			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token não fornecido"));
			}

			String token = authHeader.substring(7);
			String username = jwtService.extractUsername(token);

			logger.info("🔑 Troca de senha solicitada por: {}", username);

			usuarioService.alterarSenhaPorUsername(username, request.getSenhaAtual(), request.getSenhaNova());

			logger.info("✅ Senha alterada com sucesso para: {}", username);

			logAcessoService.registrarAcesso(null, username, ip, userAgent, "CHANGE_PASSWORD", "SUCCESS",
					"Senha alterada pelo próprio usuário");

			return ResponseEntity.ok(Map.of("success", true, "message", "Senha alterada com sucesso."));

		} catch (RuntimeException e) {
			logger.warn("⚠️ Erro na troca de senha: {}", e.getMessage());

			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", "Troca de senha falhou", "message", e.getMessage()));
		} catch (Exception e) {
			logger.error("❌ Erro inesperado na troca de senha: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", "Erro", "message", "Não foi possível alterar a senha."));
		}
	}

	// ============================================================
	// 🔥 ATUALIZAR MINHA FOTO (avatar) — qualquer usuário autenticado
	//
	// 🔥 OTIMIZAÇÃO: NÃO devolve mais o fotoBase64 no response.
	//    O frontend já tem a string localmente; devolver 1-2MB era
	//    desnecessário e causava lentidão.
	// ============================================================
	@PostMapping("/me/foto")
	public ResponseEntity<?> atualizarMinhaFoto(@RequestBody FotoRequest request, HttpServletRequest httpRequest) {
		final String ip = httpRequest.getRemoteAddr();
		final String userAgent = httpRequest.getHeader("User-Agent");

		try {
			String username = extrairUsernameDoToken(httpRequest);
			if (username == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("error", "Token inválido"));
			}

			logger.info("📸 Atualizando foto de: {}", username);

			usuarioService.atualizarFotoPorUsername(username, request.getFotoBase64());

			logAcessoService.registrarAcesso(null, username, ip, userAgent, "UPDATE_PHOTO", "SUCCESS",
					"Foto atualizada pelo próprio usuário");

			// 🔥 Response leve — sem devolver 1-2MB
			return ResponseEntity.ok(Map.of(
					"success", true,
					"message", "Foto atualizada com sucesso."));

		} catch (RuntimeException e) {
			logger.warn("⚠️ Erro ao atualizar foto: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", "Erro", "message", e.getMessage()));
		} catch (Exception e) {
			logger.error("❌ Erro inesperado ao atualizar foto: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", "Erro", "message", "Não foi possível atualizar a foto."));
		}
	}

	// ============================================================
	// 🔥 REMOVER MINHA FOTO — qualquer usuário autenticado
	// ============================================================
	@DeleteMapping("/me/foto")
	public ResponseEntity<?> removerMinhaFoto(HttpServletRequest httpRequest) {
		final String ip = httpRequest.getRemoteAddr();
		final String userAgent = httpRequest.getHeader("User-Agent");

		try {
			String username = extrairUsernameDoToken(httpRequest);
			if (username == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("error", "Token inválido"));
			}

			logger.info("🗑️ Removendo foto de: {}", username);

			usuarioService.removerFotoPorUsername(username);

			logAcessoService.registrarAcesso(null, username, ip, userAgent, "DELETE_PHOTO", "SUCCESS",
					"Foto removida pelo próprio usuário");

			return ResponseEntity.ok(Map.of(
					"success", true,
					"message", "Foto removida com sucesso."));

		} catch (Exception e) {
			logger.error("❌ Erro ao remover foto: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", "Erro", "message", "Não foi possível remover a foto."));
		}
	}

	// ============================================================
	// LOGOUT — Encerra a sessão atual
	// ============================================================
	@PostMapping("/logout")
	public ResponseEntity<Map<String, Object>> logout(HttpServletRequest httpRequest) {
		final String ip = httpRequest.getRemoteAddr();
		final String userAgent = httpRequest.getHeader("User-Agent");

		try {
			String authHeader = httpRequest.getHeader("Authorization");
			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Token não fornecido"));
			}

			String token = authHeader.substring(7);
			String username = jwtService.extractUsername(token);

			sessaoService.encerrarSessao(token);

			logger.info("🚪 Logout realizado: {}", username);

			if (username != null) {
				logAcessoService.registrarAcesso(null, username, ip, userAgent, "LOGOUT", "SUCCESS",
						"Logout realizado com sucesso");
			}

			return ResponseEntity.ok(Map.of("success", true, "message", "Logout realizado com sucesso."));

		} catch (Exception e) {
			logger.error("❌ Erro no logout: {}", e.getMessage(), e);
			return ResponseEntity.ok(Map.of("success", true, "message", "Logout processado."));
		}
	}

	// ============================================================
	// LOGOUT ALL — Encerra TODAS as sessões do usuário
	// ============================================================
	@PostMapping("/logout-all")
	@org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
	public ResponseEntity<Map<String, Object>> logoutAll(HttpServletRequest httpRequest) {
		final String ip = httpRequest.getRemoteAddr();
		final String userAgent = httpRequest.getHeader("User-Agent");

		try {
			String authHeader = httpRequest.getHeader("Authorization");
			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Token não fornecido"));
			}

			String token = authHeader.substring(7);
			String username = jwtService.extractUsername(token);

			Usuario usuario = usuarioService.findByUsername(username)
					.orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

			sessaoService.encerrarSessoesUsuario(usuario.getId());

			logger.info("🚪 Logout ALL realizado: {}", username);

			logAcessoService.registrarAcesso(usuario.getId(), username, ip, userAgent, "LOGOUT_ALL", "SUCCESS",
					"Todas as sessões encerradas");

			return ResponseEntity.ok(Map.of("success", true, "message", "Todas as sessões foram encerradas."));

		} catch (Exception e) {
			logger.error("❌ Erro no logout-all: {}", e.getMessage(), e);
			return ResponseEntity.ok(Map.of("success", true, "message", "Logout processado."));
		}
	}

	// ============================================================
	// TESTE — health check leve
	// ============================================================
	@GetMapping("/teste")
	public ResponseEntity<String> teste() {
		return ResponseEntity.ok("OK");
	}

	// ============================================================
	// MÉTODOS AUXILIARES
	// ============================================================

	/**
	 * 🔥 Constrói a árvore de menus em memória — SEM N+1.
	 *
	 * Recebe uma List<Menu> flat (vinda do MenuRepository) e devolve
	 * uma List<MenuDTO> com subMenus aninhados.
	 *
	 * Estratégia:
	 *   1) Cria um DTO flat para cada menu (sem recursão).
	 *   2) Agrupa por ID em um mapa.
	 *   3) Para cada menu, encontra o pai no mapa e adiciona como submenu.
	 *   4) Os que não têm pai ficam como raiz.
	 *
	 * O(N) — antes era O(N²) com lazy loading.
	 */
	private List<MenuDTO> construirArvoreMenusDeLista(List<Menu> menusDoPerfil) {
		if (menusDoPerfil == null || menusDoPerfil.isEmpty()) {
			return new ArrayList<>();
		}

		// 1) Cria DTOs flats
		Map<Long, MenuDTO> dtoMap = new HashMap<>();
		for (Menu m : menusDoPerfil) {
			MenuDTO dto = new MenuDTO();
			dto.setId(m.getId());
			dto.setNome(m.getNome());
			dto.setCaminho(m.getCaminho());
			dto.setIcone(m.getIcone());
			dto.setOrdem(m.getOrdem());
			dto.setAtivo(m.getAtivo());
			dto.setSubMenus(new ArrayList<>());

			if (m.getMenuPai() != null) {
				dto.setMenuPaiId(m.getMenuPai().getId());
				dto.setMenuPaiNome(m.getMenuPai().getNome());
			}

			dtoMap.put(m.getId(), dto);
		}

		// 2) Monta a árvore
		List<MenuDTO> roots = new ArrayList<>();
		for (Menu m : menusDoPerfil) {
			MenuDTO dto = dtoMap.get(m.getId());
			if (dto == null) continue;

			if (m.getMenuPai() != null && dtoMap.containsKey(m.getMenuPai().getId())) {
				MenuDTO pai = dtoMap.get(m.getMenuPai().getId());
				pai.getSubMenus().add(dto);
			} else {
				roots.add(dto);
			}
		}

		// 3) Ordena raízes e submenus por ordem
		roots.sort((a, b) -> {
			int oa = a.getOrdem() != null ? a.getOrdem() : 0;
			int ob = b.getOrdem() != null ? b.getOrdem() : 0;
			return Integer.compare(oa, ob);
		});
		for (MenuDTO r : roots) {
			ordenarSubMenus(r);
		}

		return roots;
	}

	private void ordenarSubMenus(MenuDTO dto) {
		if (dto.getSubMenus() == null || dto.getSubMenus().isEmpty()) return;
		dto.getSubMenus().sort((a, b) -> {
			int oa = a.getOrdem() != null ? a.getOrdem() : 0;
			int ob = b.getOrdem() != null ? b.getOrdem() : 0;
			return Integer.compare(oa, ob);
		});
		for (MenuDTO filho : dto.getSubMenus()) {
			ordenarSubMenus(filho);
		}
	}

	/**
	 * 🔥 Helper — extrai o username do token JWT no header Authorization
	 */
	private String extrairUsernameDoToken(HttpServletRequest httpRequest) {
		String authHeader = httpRequest.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return null;
		}
		try {
			String token = authHeader.substring(7);
			return jwtService.extractUsername(token);
		} catch (Exception e) {
			return null;
		}
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
		private String fotoBase64;

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

		public String getFotoBase64() {
			return fotoBase64;
		}

		public void setFotoBase64(String fotoBase64) {
			this.fotoBase64 = fotoBase64;
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

	public static class TrocarSenhaRequest {
		private String senhaAtual;
		private String senhaNova;

		public String getSenhaAtual() {
			return senhaAtual;
		}

		public void setSenhaAtual(String senhaAtual) {
			this.senhaAtual = senhaAtual;
		}

		public String getSenhaNova() {
			return senhaNova;
		}

		public void setSenhaNova(String senhaNova) {
			this.senhaNova = senhaNova;
		}
	}

	/**
	 * 🔥 Inner class para requisição de foto.
	 *
	 * O @JsonProperty(WRITE_ONLY) faz com que o Jackson:
	 *   - ACEITE o campo no JSON de entrada (POST).
	 *   - OMITA o campo em serialização de saída (útil quando o
	 *     SistemaLogAspect serializa os argumentos para log).
	 */
	public static class FotoRequest {

		private String fotoBase64;

		@com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
		public String getFotoBase64() {
			return fotoBase64;
		}

		public void setFotoBase64(String fotoBase64) {
			this.fotoBase64 = fotoBase64;
		}
	}
}