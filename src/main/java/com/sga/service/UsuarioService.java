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

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PerfilRepository perfilRepository;

    // ============================================================
    // MÉTODOS EXISTENTES (MANTIDOS)
    // ============================================================

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        // Se tiver perfil, usa as permissões do perfil
        if (usuario.getPerfil() != null && usuario.getPerfil().getPermissoes() != null 
            && !usuario.getPerfil().getPermissoes().isEmpty()) {
            return usuario;
        }

        // Fallback: mantém o comportamento original com role
        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .roles(usuario.getRole() != null ? usuario.getRole() : "USER")
                .build();
    }

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

    // ============================================================
    // 🔥 NOVOS MÉTODOS PARA O CONTROLLER
    // ============================================================

    @Transactional
    public UsuarioDTO criar(UsuarioRequest request) {
        // Validar se usuário já existe
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

    // ============================================================
    // MÉTODOS DE CRUD EXISTENTES
    // ============================================================

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public List<UsuarioDTO> listarTodosDTO() {
        return usuarioRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public UsuarioDTO buscarPorIdDTO(Long id) {
        Usuario usuario = buscarPorId(id);
        return toDTO(usuario);
    }

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
    public void bloquear(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setBloqueado(true);
        usuario.setDataAtualizacao(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void desbloquear(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setBloqueado(false);
        usuario.setTentativasLogin(0);
        usuario.setDataAtualizacao(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void deletar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuarioRepository.delete(usuario);
    }

    @Transactional
    public void incrementarTentativasLogin(String username) {
        usuarioRepository.findByUsername(username).ifPresent(usuario -> {
            usuario.setTentativasLogin(usuario.getTentativasLogin() + 1);
            usuario.setDataAtualizacao(LocalDateTime.now());
            usuarioRepository.save(usuario);
        });
    }

    // ============================================================
    // MÉTODO AUXILIAR - TO DTO
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
        
        if (usuario.getPerfil() != null) {
            dto.setPerfilId(usuario.getPerfil().getId());
            dto.setPerfilNome(usuario.getPerfil().getNome());
        } else if (usuario.getPerfilId() != null) {
            dto.setPerfilId(usuario.getPerfilId());
        }
        
        return dto;
    }
}