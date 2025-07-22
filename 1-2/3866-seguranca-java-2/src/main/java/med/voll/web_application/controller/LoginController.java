package med.voll.web_application.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import med.voll.web_application.domain.RegraDeNegocioException;
import med.voll.web_application.domain.paciente.PacienteService;
import med.voll.web_application.domain.usuario.DadosAlteracaoSenha;
import med.voll.web_application.domain.usuario.Usuario;
import med.voll.web_application.domain.usuario.UsuarioService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final UsuarioService service;
    private final PacienteService pacienteService;

    public static final String FORMULARIO_ALTERACAO_SENHA = "autenticacao/formulario-alteracao-senha";
    public static final String LOGIN_ATIVACAO_CONTA = "autenticacao/login-ativacao-conta";

    public LoginController(UsuarioService service, PacienteService pacienteService) {
        this.service = service;
        this.pacienteService = pacienteService;
    }

    @GetMapping("/login")
    public String carregaPaginaLogin(){
        return "autenticacao/login";
    }

    @GetMapping("/alterar-senha")
    public String carregaPaginaAlteracao(){
        return FORMULARIO_ALTERACAO_SENHA;
    }

    @PostMapping("/alterar-senha")
    public String alterarSenha(@Valid @ModelAttribute("dados") DadosAlteracaoSenha dados, BindingResult result, Model model, @AuthenticationPrincipal Usuario logado) {
        if (result.hasErrors()) {
            model.addAttribute("dados", dados);
            return FORMULARIO_ALTERACAO_SENHA;
        }

        try {
            service.alterarSenha(dados, logado);
            return "redirect:home";
        } catch (RegraDeNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("dados", dados);
            return FORMULARIO_ALTERACAO_SENHA;
        }
    }

    @GetMapping("/login/ativar-conta")
    public String carregarPaginaLoginAtivacaoConta(@RequestParam(name = "codigo", required = false) String codigo, Model model){
        if (codigo != null)
            model.addAttribute("codigo", codigo);
        return LOGIN_ATIVACAO_CONTA;
    }

    @PostMapping("/login/ativar-conta")
    public String ativarConta(@RequestParam(name = "codigo", required = false) String codigo, Model model, HttpServletRequest request){
        try{
            Usuario usuario = pacienteService.ativarConta(codigo);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            request.getSession().setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    new SecurityContextImpl(authentication)
            );

            return "redirect:/";
        }catch(RegraDeNegocioException e){
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("codigo", codigo);
            return LOGIN_ATIVACAO_CONTA;
        }
    }


}
