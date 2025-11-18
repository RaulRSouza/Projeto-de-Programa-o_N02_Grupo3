package br.com.unit.gerenciamentoAulas.entidades;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "instrutores")
public class Instrutor extends Usuario {
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false)
    private String senha;
    
    @Column(length = 100)
    private String especialidade;
    
    @Column(unique = true, length = 50)
    private String registro;
    
    @OneToMany(mappedBy = "instrutor")
    private List<Aula> aulasMinistradas;

    public Instrutor() {
        super();
        this.aulasMinistradas = new ArrayList<>();
    }

    public Instrutor(Long id, String nome, String cpf, String telefone, 
                     String email, String senha, String especialidade, String registro) {
        super(id, nome, cpf, telefone);
        this.email = email;
        this.senha = senha;
        this.especialidade = especialidade;
        this.registro = registro;
        this.aulasMinistradas = new ArrayList<>();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public String getRegistro() {
        return registro;
    }

    public void setRegistro(String registro) {
        this.registro = registro;
    }

    public List<Aula> getAulasMinistradas() {
        return aulasMinistradas;
    }

    public void setAulasMinistradas(List<Aula> aulasMinistradas) {
        this.aulasMinistradas = aulasMinistradas;
    }

    public void adicionarAula(Aula aula) {
        this.aulasMinistradas.add(aula);
    }

    public void removerAula(Aula aula) {
        this.aulasMinistradas.remove(aula);
    }

    @Override
    public String toString() {
        return "Instrutor{" +
                "id=" + getId() +
                ", nome='" + getNome() + '\'' +
                ", especialidade='" + especialidade + '\'' +
                ", registro='" + registro + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}