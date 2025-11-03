package br.com.unit.gerenciamentoAulas.entidades;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "aulas")
public class Aula {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;
    
    @ManyToOne
    @JoinColumn(name = "instrutor_id", nullable = false)
    private Instrutor instrutor;
    
    @ManyToOne
    @JoinColumn(name = "local_id", nullable = false)
    private Local local;
    
    @Column(name = "data_hora_inicio", nullable = false)
    private LocalDateTime dataHoraInicio;
    
    @Column(name = "data_hora_fim", nullable = false)
    private LocalDateTime dataHoraFim;
    
    @Column(name = "vagas_disponiveis", nullable = false)
    private int vagasDisponiveis;
    
    @Column(name = "vagas_totais", nullable = false)
    private int vagasTotais;
    
    @Column(nullable = false, length = 20)
    private String status;
    
    @Column(columnDefinition = "TEXT")
    private String observacoes;
    
    @OneToMany(mappedBy = "aula", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inscricao> inscricoes;

    @Column(name = "material_url", length = 500)
    private String materialComplementarUrl;

    @Column(name = "material_nome_arquivo", length = 255)
    private String materialComplementarNomeArquivo;

    @Column(name = "material_tipo", length = 100)
    private String materialComplementarTipo;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JsonIgnore
    @Column(name = "material_arquivo")
    private byte[] materialComplementarArquivo;

    public Aula() {
        this.inscricoes = new ArrayList<>();
        this.status = "AGENDADA";
    }

    public Aula(Long id, Curso curso, Instrutor instrutor, Local local, 
                LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim, int vagasTotais) {
        this.id = id;
        this.curso = curso;
        this.instrutor = instrutor;
        this.local = local;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.vagasTotais = vagasTotais;
        this.vagasDisponiveis = vagasTotais;
        this.status = "AGENDADA";
        this.inscricoes = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public Instrutor getInstrutor() {
        return instrutor;
    }

    public void setInstrutor(Instrutor instrutor) {
        this.instrutor = instrutor;
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public void setDataHoraInicio(LocalDateTime dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
    }

    public LocalDateTime getDataHoraFim() {
        return dataHoraFim;
    }

    public void setDataHoraFim(LocalDateTime dataHoraFim) {
        this.dataHoraFim = dataHoraFim;
    }

    public int getVagasDisponiveis() {
        return vagasDisponiveis;
    }

    public void setVagasDisponiveis(int vagasDisponiveis) {
        this.vagasDisponiveis = vagasDisponiveis;
    }

    public int getVagasTotais() {
        return vagasTotais;
    }

    public void setVagasTotais(int vagasTotais) {
        this.vagasTotais = vagasTotais;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public List<Inscricao> getInscricoes() {
        return inscricoes;
    }

    public void setInscricoes(List<Inscricao> inscricoes) {
        this.inscricoes = inscricoes;
    }

    public String getTitulo() { return titulo; }

    public void setTitulo(String titulo) { this.titulo = titulo;}

    public String getDescricao() { return descricao; }

    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getMaterialComplementarUrl() {
        return materialComplementarUrl;
    }

    public void setMaterialComplementarUrl(String materialComplementarUrl) {
        this.materialComplementarUrl = materialComplementarUrl;
    }

    public String getMaterialComplementarNomeArquivo() {
        return materialComplementarNomeArquivo;
    }

    public void setMaterialComplementarNomeArquivo(String materialComplementarNomeArquivo) {
        this.materialComplementarNomeArquivo = materialComplementarNomeArquivo;
    }

    public String getMaterialComplementarTipo() {
        return materialComplementarTipo;
    }

    public void setMaterialComplementarTipo(String materialComplementarTipo) {
        this.materialComplementarTipo = materialComplementarTipo;
    }

    public byte[] getMaterialComplementarArquivo() {
        return materialComplementarArquivo;
    }

    public void setMaterialComplementarArquivo(byte[] materialComplementarArquivo) {
        this.materialComplementarArquivo = materialComplementarArquivo;
    }

    public void limparMaterialComplementar() {
        this.materialComplementarArquivo = null;
        this.materialComplementarNomeArquivo = null;
        this.materialComplementarTipo = null;
        this.materialComplementarUrl = null;
    }

    public void adicionarInscricao(Inscricao inscricao) {
        this.inscricoes.add(inscricao);
        inscricao.setAula(this);
        this.vagasDisponiveis--;
    }

    public void removerInscricao(Inscricao inscricao) {
        this.inscricoes.remove(inscricao);
        inscricao.setAula(null);
        this.vagasDisponiveis++;
    }

    public boolean temVagasDisponiveis() {
        return vagasDisponiveis > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aula aula = (Aula) o;
        return Objects.equals(id, aula.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Aula{" +
                "id=" + id +
                ", curso=" + (curso != null ? curso.getNome() : "null") +
                ", instrutor=" + (instrutor != null ? instrutor.getNome() : "null") +
                ", local=" + (local != null ? local.getNome() : "null") +
                ", dataHoraInicio=" + dataHoraInicio +
                ", dataHoraFim=" + dataHoraFim +
                ", vagasDisponiveis=" + vagasDisponiveis +
                ", vagasTotais=" + vagasTotais +
                ", status='" + status + '\'' +
                '}';
    }
}