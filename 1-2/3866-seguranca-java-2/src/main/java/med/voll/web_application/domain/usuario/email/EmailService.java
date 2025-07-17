package med.voll.web_application.domain.usuario.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import med.voll.web_application.domain.RegraDeNegocioException;
import med.voll.web_application.domain.usuario.Usuario;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    private final JavaMailSender enviadorEmail;
    private final PasswordEncoder encoder;

    private static final String EMAIL_ORIGEM = "vollmed@email.com";
    private static final String NOME_ENVIADOR = "Clínica Voll Med";
    private static final String URL_SITE = "http://localhost:8080"; //"voll.med.com.br"

    public EmailService(JavaMailSender enviadorEmail, PasswordEncoder encoder) {
        this.enviadorEmail = enviadorEmail;
        this.encoder = encoder;
    }

    @Async
    private void enviarEmail(String emailUsuario, String assunto, String conteudo){
        MimeMessage message = enviadorEmail.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        try{
            helper.setFrom(EMAIL_ORIGEM, NOME_ENVIADOR);
            helper.setTo(emailUsuario);
            helper.setSubject(assunto);
            helper.setText(conteudo, true);
        } catch(MessagingException | UnsupportedEncodingException e){
            throw new RegraDeNegocioException("Erro ao enviar email");
        }

        enviadorEmail.send(message);
    }

    public void enviarEmailSenha(Usuario usuario){
        String assunto = "Aqui está seu link para alterar a senha";
        String conteudo = gerarConteudoEmail("Olá [[name]],<br>"
        + "Por favor clique no link abaixo para alterar a senha:<br>"
        + "<h3><a href=\"[[URL]]\" target=\"_self\">ALTERAR</a></h3>"
        + "Obrigado,<br>"
        + "Clínica Voll Med.", usuario.getNome(), URL_SITE + "/recuperar-conta?codigo=" +usuario.getToken());

        enviarEmail(usuario.getUsername(), assunto, conteudo);
    }

    public void enviarSenhaProvisoria(Usuario usuario, String senhaProvisoria){
        String assunto = "Cadastro Voll Med - Senha Provisória";
        String conteudo = gerarConteudoSenhaProvisoria("Olá [[name]],<br>"
        + "Seja Bem Vindo a Voll Med!<br>" +
                "<br>Segue a sua senha provisória: [[senhaProvisoria]]<br>" +
                "<br>Você deverá alterá-la no primeiro acesso!<br>" +
                "Obrigado,<br>" +
                "<br>Clínica Voll Med.", usuario.getNome(), senhaProvisoria);

        enviarEmail(usuario.getUsername(), assunto, conteudo);
    }

    private String gerarConteudoEmail(String template, String nome, String url){
        return template.replace("[[name]]", nome).replace("[[URL]]", url);
    }

    private String gerarConteudoSenhaProvisoria(String template, String nome, String token){
        return template.replace("[[name]]", nome).replace("[[senhaProvisoria]]", token);
    }


}
