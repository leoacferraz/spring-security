package med.voll.web_application.domain.paciente;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pacientes")
public class Paciente {

    @Id
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String cpf;
    private Boolean isAtivo = false;

    @Deprecated
    public Paciente(){}

    public Paciente(Long id, DadosCadastroPaciente dados) {
        this.id = id;
        modificarDados(dados);
    }

    public Paciente(Long id, DadosCadastroPaciente dados, Boolean isAtivo) {
        this.id = id;
        this.isAtivo = isAtivo;
        modificarDados(dados);
    }

    public void modificarDados(DadosCadastroPaciente dados) {
        this.nome = dados.nome();
        this.email = dados.email();
        this.telefone = dados.telefone();
        this.cpf = dados.cpf();
        this.isAtivo = false;
    }
    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getCpf() {
        return cpf;
    }

    public Boolean getIsAtivo() {
        return isAtivo;
    }

    public void ativarPaciente(){
        this.isAtivo = true;
    }
}