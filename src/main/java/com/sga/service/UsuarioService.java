package com.sga.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.UsuarioDTO;
import com.sga.dto.UsuarioRequest;
import com.sga.dto.UsuarioUpdateRequest;
import com.sga.model.Perfil;
import com.sga.model.Usuario;
import com.sga.repository.PerfilRepository;
import com.sga.repository.UsuarioRepository;

@Service
public class UsuarioService implements UserDetailsService {

	// ============================================================
	// 🔥 CONFIGURAÇÃO DE BLOQUEIO
	// ============================================================
	public static final int MAX_TENTATIVAS_LOGIN = 5;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	@Lazy
	private PasswordEncoder passwordEncoder;

	@Autowired
	private PerfilRepository perfilRepository;

	// ============================================================
	// UserDetailsService
	//
	// 🔥 OTIMIZAÇÃO: usa findByUsernameWithPerfil para garantir
	//    que o perfil e as permissões venham carregados mesmo
	//    fora de transação (necessário para getAuthorities()).
	// ============================================================

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario usuario = usuarioRepository.findByUsernameWithPerfil(username)
				.orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

		if (usuario.getPerfil() != null && usuario.getPerfil().getPermissoes() != null
				&& !usuario.getPerfil().getPermissoes().isEmpty()) {
			return usuario;
		}

		return User.builder().username(usuario.getUsername()).password(usuario.getPassword())
				.roles(usuario.getRole() != null ? usuario.getRole() : "USER").build();
	}

	// ============================================================
	// 🔥 NOVOS MÉTODOS DE BUSCA OTIMIZADA
	// ============================================================

	/**
	 * 🔥 Busca o usuário junto com o perfil e as permissões.
	 * Use para autenticação/autorização onde getAuthorities() é chamado.
	 */
	public Optional<Usuario> findByUsernameWithPerfil(String username) {
		return usuarioRepository.findByUsernameWithPerfil(username);
	}

	/**
	 * 🔥 Busca o usuário por ID junto com o perfil e as permissões.
	 */
	public Optional<Usuario> findByIdWithPerfil(Long id) {
		return usuarioRepository.findByIdWithPerfil(id);
	}

	// ============================================================
	// MÉTODOS EXISTENTES (MANTIDOS)
	// ============================================================

	public Optional<Usuario> findByUsername(String username) {
		return usuarioRepository.findByUsername(username);
	}

	public Usuario save(Usuario usuario) {
		usuario.setDataAtualizacao(LocalDateTime.now());
		return usuarioRepository.save(usuario);
	}

	public Usuario criarUsuario(String username, String password, String email, String nome, String role) {
		Usuario usuario = new Usuario();
		usuario.setUsername(username);
		usuario.setPassword(passwordEncoder.encode(password));
		usuario.setEmail(email);
		usuario.setNome(nome);
		usuario.setRole(role != null ? role : "USER");
		usuario.setAtivo(true);
		usuario.setBloqueado(false);
		usuario.setTentativasLogin(0);
		usuario.setDataCriacao(LocalDateTime.now());
		usuario.setDataAtualizacao(LocalDateTime.now());

		if (role != null) {
			Optional<Perfil> perfil = perfilRepository.findByNome(role);
			perfil.ifPresent(p -> {
				usuario.setPerfilId(p.getId());
				usuario.setPerfil(p);
			});
		}

		return usuarioRepository.save(usuario);
	}

	@Transactional
	public UsuarioDTO criar(UsuarioRequest request) {
		if (usuarioRepository.existsByUsername(request.getUsername())) {
			throw new RuntimeException("Username já existe");
		}
		if (usuarioRepository.existsByEmail(request.getEmail())) {
			throw new RuntimeException("Email já existe");
		}

		Perfil perfil = perfilRepository.findById(request.getPerfilId())
				.orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

		Usuario usuario = new Usuario();
		usuario.setUsername(request.getUsername());
		usuario.setPassword(passwordEncoder.encode(request.getSenha()));
		usuario.setEmail(request.getEmail());
		usuario.setNome(request.getNomeCompleto());
		usuario.setAtivo(request.getAtivo() != null ? request.getAtivo() : true);
		usuario.setBloqueado(false);
		usuario.setTentativasLogin(0);
		usuario.setPerfilId(perfil.getId());
		usuario.setPerfil(perfil);
		usuario.setRole(perfil.getNome());
		usuario.setDataCriacao(LocalDateTime.now());
		usuario.setDataAtualizacao(LocalDateTime.now());

		usuario = usuarioRepository.save(usuario);
		return toDTO(usuario);
	}

	@Transactional
	public UsuarioDTO atualizar(Long id, UsuarioUpdateRequest request) {
		Usuario usuario = buscarPorId(id);

		Perfil perfil = perfilRepository.findById(request.getPerfilId())
				.orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

		usuario.setNome(request.getNomeCompleto());
		usuario.setEmail(request.getEmail());
		usuario.setPerfil(perfil);
		usuario.setPerfilId(perfil.getId());
		usuario.setRole(perfil.getNome());
		if (request.getAtivo() != null) {
			usuario.setAtivo(request.getAtivo());
		}
		usuario.setDataAtualizacao(LocalDateTime.now());

		usuario = usuarioRepository.save(usuario);
		return toDTO(usuario);
	}

	public List<Usuario> listarTodos() {
		return usuarioRepository.findAll();
	}

	public List<UsuarioDTO> listarTodosDTO() {
		return usuarioRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
	}

	public Usuario buscarPorId(Long id) {
		return usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
	}

	public UsuarioDTO buscarPorIdDTO(Long id) {
		Usuario usuario = buscarPorId(id);
		return toDTO(usuario);
	}

	// ============================================================
	// 🔥 ATUALIZAR ÚLTIMO LOGIN (reseta tentativas)
	// ============================================================
	@Transactional
	public void atualizarUltimoLogin(Long id, String ip) {
		usuarioRepository.findById(id).ifPresent(usuario -> {
			usuario.setUltimoLogin(LocalDateTime.now());
			usuario.setUltimoIp(ip);
			usuario.setTentativasLogin(0);
			usuario.setDataAtualizacao(LocalDateTime.now());
			usuarioRepository.save(usuario);
		});
	}

	// ============================================================
	// SENHAS
	// ============================================================

	@Transactional
	public void alterarSenha(Long id, String senhaAtual, String novaSenha) {
		Usuario usuario = buscarPorId(id);

		if (!passwordEncoder.matches(senhaAtual, usuario.getPassword())) {
			throw new RuntimeException("Senha atual incorreta");
		}

		usuario.setPassword(passwordEncoder.encode(novaSenha));
		usuario.setDataAtualizacao(LocalDateTime.now());
		usuarioRepository.save(usuario);
	}

	@Transactional
	public void resetarSenha(Long id, String novaSenha) {
		Usuario usuario = buscarPorId(id);
		usuario.setPassword(passwordEncoder.encode(novaSenha));
		usuario.setDataAtualizacao(LocalDateTime.now());
		usuarioRepository.save(usuario);
	}

	@Transactional
	public void alterarSenhaPorUsername(String username, String senhaAtual, String senhaNova) {
		Usuario usuario = usuarioRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

		if (!passwordEncoder.matches(senhaAtual, usuario.getPassword())) {
			throw new RuntimeException("Senha atual incorreta");
		}

		if (senhaNova == null || senhaNova.length() < 6) {
			throw new RuntimeException("Nova senha deve ter no mínimo 6 caracteres");
		}

		if (senhaNova.equals(senhaAtual)) {
			throw new RuntimeException("A nova senha não pode ser igual à atual");
		}

		if (!senhaNova.matches(".*[a-zA-Z].*") || !senhaNova.matches(".*\\d.*")) {
			throw new RuntimeException("A nova senha deve conter letras e números");
		}

		usuario.setPassword(passwordEncoder.encode(senhaNova));
		usuario.setDataAtualizacao(LocalDateTime.now());
		usuarioRepository.save(usuario);

		System.out.println("✅ Senha alterada para usuário: " + username);
	}

	// ============================================================
	// BLOQUEIO
	// ============================================================

	@Transactional
	public void bloquear(Long id) {
		Usuario usuario = buscarPorId(id);
		usuario.setBloqueado(true);
		usuario.setDataBloqueio(LocalDateTime.now());
		usuario.setMotivoBloqueio("Bloqueado manualmente pelo administrador");
		usuario.setDataAtualizacao(LocalDateTime.now());
		usuarioRepository.save(usuario);
	}

	@Transactional
	public void desbloquear(Long id) {
		Usuario usuario = buscarPorId(id);
		usuario.setBloqueado(false);
		usuario.setTentativasLogin(0);
		usuario.setDataBloqueio(null);
		usuario.setMotivoBloqueio(null);
		usuario.setDataAtualizacao(LocalDateTime.now());
		usuarioRepository.save(usuario);
	}

	// ============================================================
	// 🔥 ATUALIZAR FOTO DO USUÁRIO (avatar base64)
	// ============================================================
	@Transactional
	public void atualizarFotoPorUsername(String username, String fotoBase64) {
		Usuario usuario = usuarioRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

		if (fotoBase64 != null && !fotoBase64.isEmpty()) {
			if (!fotoBase64.startsWith("data:image/")) {
				throw new RuntimeException("Formato de imagem inválido. Use data URI (data:image/...)");
			}

			if (fotoBase64.length() > 2_800_000) {
				throw new RuntimeException("Imagem muito grande. Máximo permitido: 2MB.");
			}
		}

		usuario.setFotoBase64(fotoBase64);
		usuario.setDataAtualizacao(LocalDateTime.now());
		usuarioRepository.save(usuario);

		System.out.println("📸 Foto atualizada para usuário: " + username);
	}

	// ============================================================
	// 🔥 REMOVER FOTO DO USUÁRIO
	// ============================================================
	@Transactional
	public void removerFotoPorUsername(String username) {
		Usuario usuario = usuarioRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

		usuario.setFotoBase64(null);
		usuario.setDataAtualizacao(LocalDateTime.now());
		usuarioRepository.save(usuario);

		System.out.println("🗑️ Foto removida para usuário: " + username);
	}

	@Transactional
	public void deletar(Long id) {
		Usuario usuario = buscarPorId(id);
		usuarioRepository.delete(usuario);
	}

	// ============================================================
	// 🔥 INCREMENTAR TENTATIVAS (com bloqueio automático)
	// ============================================================
	@Transactional
	public int incrementarTentativasLogin(String username) {
		return usuarioRepository.findByUsername(username).map(usuario -> {
			int tentativas = (usuario.getTentativasLogin() != null ? usuario.getTentativasLogin() : 0) + 1;

			usuario.setTentativasLogin(tentativas);
			usuario.setDataAtualizacao(LocalDateTime.now());

			if (tentativas >= MAX_TENTATIVAS_LOGIN) {
				usuario.setBloqueado(true);
				usuario.setDataBloqueio(LocalDateTime.now());
				usuario.setMotivoBloqueio(
						"Bloqueado automaticamente após " + MAX_TENTATIVAS_LOGIN + " tentativas de login incorretas");
				System.out.println("🔒 Usuário bloqueado automaticamente: " + username);
			}

			usuarioRepository.save(usuario);
			return tentativas;
		}).orElse(0);
	}

	// ============================================================
	// TO DTO
	// ============================================================

	public UsuarioDTO toDTO(Usuario usuario) {
		UsuarioDTO dto = new UsuarioDTO();
		dto.setId(usuario.getId());
		dto.setUsername(usuario.getUsername());
		dto.setEmail(usuario.getEmail());
		dto.setNomeCompleto(usuario.getNome());
		dto.setAtivo(usuario.isAtivo());
		dto.setBloqueado(usuario.getBloqueado());
		dto.setUltimoLogin(usuario.getUltimoLogin());
		dto.setRole(usuario.getRole());
		dto.setFotoBase64(usuario.getFotoBase64());

		if (usuario.getPerfil() != null) {
			dto.setPerfilId(usuario.getPerfil().getId());
			dto.setPerfilNome(usuario.getPerfil().getNome());
		} else if (usuario.getPerfilId() != null) {
			dto.setPerfilId(usuario.getPerfilId());
		}

		return dto;
	}
}