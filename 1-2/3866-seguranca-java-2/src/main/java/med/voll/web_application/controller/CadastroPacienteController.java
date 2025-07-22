package med.voll.web_application.controller;

import jakarta.validation.Valid;
import med.voll.web_application.domain.RegraDeNegocioException;
import med.voll.web_application.domain.paciente.DadosCadastroPacienteUsuario;
import med.voll.web_application.domain.paciente.PacienteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CadastroPacienteController {

    private static final String LOGIN = "redirect:login";
    private static final String PAGINA_AUTO_CADASTRO = "paciente/formulario-cadastro";

    private final PacienteService service;

    public CadastroPacienteController(PacienteService service) {
        this.service = service;
    }

    @GetMapping("/cadastro")
    public String carregarFormularioCadastro(Model model){
        DadosCadastroPacienteUsuario dados = new DadosCadastroPacienteUsuario(null, "", "", "", "", "",false);
        model.addAttribute("dados", dados);
        return PAGINA_AUTO_CADASTRO;
    }

    @PostMapping("/cadastro")
    public String cadastrarPaciente(@Valid @ModelAttribute("dados") DadosCadastroPacienteUsuario dados, BindingResult result, Model model){
        if (result.hasErrors()){
            model.addAttribute("dados", dados);
            return PAGINA_AUTO_CADASTRO;
        }

        try{
            service.cadastrarAuto(dados);
            return "redirect:/cadastro?cadastroSucesso";
        }catch (RegraDeNegocioException e){

            model.addAttribute("erro", e.getMessage());
            model.addAttribute("dados", dados);
            return PAGINA_AUTO_CADASTRO;
        }
    }

}
