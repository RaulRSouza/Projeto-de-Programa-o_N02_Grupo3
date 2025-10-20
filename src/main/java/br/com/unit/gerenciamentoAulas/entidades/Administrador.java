package br.com.unit.gerenciamentoAulas.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "administradores")
public class Administrador extends Usuario {
    
    @Column(length = 100)
    private String setor;
    
    @Column(name = "nivel_acesso", length = 50)
    private String nivelAcesso;

    public Administrador() {
        super();
    }

    public Administrador(Long id, String nome, String email, String senha, String cpf, 
                        String telefone, String setor, String nivelAcesso) {
        super(id, nome, email, senha, cpf, telefone);
        this.setor = setor;
        this.nivelAcesso = nivelAcesso;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public String getNivelAcesso() {
        return nivelAcesso;
    }

    public void setNivelAcesso(String nivelAcesso) {
        this.nivelAcesso = nivelAcesso;
    }

    @Override
    public String toString() {
        return "Administrador{" +
                "id=" + getId() +
                ", nome='" + getNome() + '\'' +
                ", setor='" + setor + '\'' +
                ", nivelAcesso='" + nivelAcesso + '\'' +
                ", email='" + getEmail() + '\'' +
                '}';
    }
}