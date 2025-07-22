package med.voll.web_application.domain.usuario;

import med.voll.web_application.domain.RegraDeNegocioException;
import med.voll.web_application.domain.paciente.Paciente;
import med.voll.web_application.domain.paciente.PacienteRepository;
import med.voll.web_application.domain.usuario.email.EmailService;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encriptador;
    private final EmailService emailService;
    private final PacienteRepository pacienteRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder encriptador, EmailService emailService, PacienteRepository pacienteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.encriptador = encriptador;
        this.emailService = emailService;
        this.pacienteRepository = pacienteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("O usuário não foi encontrado!"));

        if (usuario.getPerfil() == Perfil.PACIENTE) {
            Paciente paciente = pacienteRepository.findById(usuario.getId()).orElseThrow(
                    () -> new RegraDeNegocioException("Não existe paciente vinculado a esse usuário"));
            if (!paciente.getIsAtivo()) {
                throw new DisabledException("Conta ainda não foi ativada. Verifique o e-mail.");
            }
        }
        return usuario;
    }

    public Long salvarUsuario(String nome, String email, Perfil perfil) {
        String primeiraSenha = UUID.randomUUID().toString().substring(0, 8);
        String senhaCriptografada = encriptador.encode(primeiraSenha);
        Usuario usuario = usuarioRepository.save(new Usuario(nome, email, senhaCriptografada, perfil));

        emailService.enviarSenhaProvisoria(usuario , primeiraSenha);

        return usuario.getId();
    }

    public Long salvarUsuarioAuto(String nome, String email, String senha, Perfil perfil) {
        String token = UUID.randomUUID().toString();
        String senhaCriptografada = encriptador.encode(senha);
        Usuario usuario = usuarioRepository.save(new Usuario(nome, email, senhaCriptografada, perfil));

        usuario.setSenhaAlterada(true);
        usuario.setToken(token);

        emailService.enviarEmailAtivacaoCadastro(usuario);

        return usuario.getId();
    }

    public void excluir(Long id) {
        usuarioRepository.deleteById(id);
    }

    public void alterarSenha(DadosAlteracaoSenha dados, Usuario logado){
        if(!encriptador.matches(dados.senhaAtual(), logado.getPassword())){
            throw new RegraDeNegocioException("Senha digitada não confere com a senha atual!");
        }
        if(!dados.novaSenha().equals(dados.novaSenhaConfirmacao())){
            throw new RegraDeNegocioException("Senha e confirmação não conferem!");
        }

        String senhaCriptografada = encriptador.encode(dados.novaSenha());

        logado.alterarSenha(senhaCriptografada);

        logado.setSenhaAlterada(true);

        usuarioRepository.save(logado);
    }

    public void enviarToken(String email){
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow(
                () -> new RegraDeNegocioException("Usuário não encontrado!")
        );
        String token = UUID.randomUUID().toString();
        usuario.setToken(token);
        usuario.setExpiracaoToken(LocalDateTime.now().plusMinutes(15));

        usuarioRepository.save(usuario);
        emailService.enviarEmailSenha(usuario);
    }

    public void recuperarConta(String codigo, DadosRecuperacaoConta dados) {
        Usuario usuario = usuarioRepository.findByTokenIgnoreCase(codigo)
                .orElseThrow( () -> new RegraDeNegocioException("Link inválido!")
        );

        if (usuario.getExpiracaoToken().isBefore(LocalDateTime.now())){
            throw new RegraDeNegocioException("Link expirado!");
        }

        if(!dados.novaSenha().equals(dados.novaSenhaConfirmacao())){
            throw new RegraDeNegocioException("Senha e confirmação não conferem!");
        }

        String senhaCriptografada = encriptador.encode(dados.novaSenha());
        usuario.alterarSenha(senhaCriptografada);

        usuario.setToken(null);
        usuario.setExpiracaoToken(null);

        usuarioRepository.save(usuario);
    }
}
