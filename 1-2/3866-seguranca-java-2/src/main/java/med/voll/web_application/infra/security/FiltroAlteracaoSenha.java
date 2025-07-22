package med.voll.web_application.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import med.voll.web_application.domain.RegraDeNegocioException;
import med.voll.web_application.domain.paciente.Paciente;
import med.voll.web_application.domain.paciente.PacienteRepository;
import med.voll.web_application.domain.usuario.Perfil;
import med.voll.web_application.domain.usuario.Usuario;
import med.voll.web_application.domain.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class FiltroAlteracaoSenha extends OncePerRequestFilter {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().contains(".css") || request.getRequestURI().contains(".png") ||
                request.getRequestURI().equals("/login")){
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !request.getRequestURI().equals("/alterar-senha")){
            Usuario usuario = (Usuario) auth.getPrincipal();
            if  (!usuario.getSenhaAlterada()){
                response.sendRedirect("/alterar-senha");
                return;
            }

            if (usuario.getPerfil() == Perfil.PACIENTE) {
                Paciente paciente = pacienteRepository.findById(usuario.getId())
                        .orElseThrow(() -> new RegraDeNegocioException("Paciente não vinculado a esse usuário"));

                if (!paciente.getIsAtivo()){
                    String mensagem = "Necessário ativar o seu acesso através do link enviado para o email: " + usuario.getUsername();
                    request.getSession().setAttribute("mensagemErro", mensagem);
                    response.sendRedirect("/login");
                    return;
                }
            }

        }

        filterChain.doFilter(request, response);
    }
}
