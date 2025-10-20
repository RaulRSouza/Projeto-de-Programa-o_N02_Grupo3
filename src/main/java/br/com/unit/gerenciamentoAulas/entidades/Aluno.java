package br.com.unit.gerenciamentoAulas.entidades;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "alunos")
public class Aluno extends Usuario {
    
    @Column(unique = true, nullable = false, length = 20)
    private String matricula;
    
    @Column(length = 100)
    private String curso;
    
    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inscricao> inscricoes;

    public Aluno() {
        super();
        this.inscricoes = new ArrayList<>();
    }

    public Aluno(Long id, String nome, String email, String senha, String cpf, 
                 String telefone, String matricula, String curso) {
        super(id, nome, email, senha, cpf, telefone);
        this.matricula = matricula;
        this.curso = curso;
        this.inscricoes = new ArrayList<>();
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public List<Inscricao> getInscricoes() {
        return inscricoes;
    }

    public void setInscricoes(List<Inscricao> inscricoes) {
        this.inscricoes = inscricoes;
    }

    @Override
    public String toString() {
        return "Aluno{" +
                "id=" + getId() +
                ", nome='" + getNome() + '\'' +
                ", matricula='" + matricula + '\'' +
                ", curso='" + curso + '\'' +
                ", email='" + getEmail() + '\'' +
                '}';
    }
}