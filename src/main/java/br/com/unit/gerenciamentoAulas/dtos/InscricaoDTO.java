package br.com.unit.gerenciamentoAulas.dtos;

import java.time.LocalDateTime;

public class InscricaoDTO {

    private Long id;
    private Long alunoId;
    private String nomeAluno;
    private Long aulaId;
    private String nomeAula;
    private LocalDateTime dataInscricao;
    private String status;

    public InscricaoDTO() {
    }

    public InscricaoDTO(Long id, Long alunoId, String nomeAluno, Long aulaId, String nomeAula,
                        LocalDateTime dataInscricao, String status) {
        this.id = id;
        this.alunoId = alunoId;
        this.nomeAluno = nomeAluno;
        this.aulaId = aulaId;
        this.nomeAula = nomeAula;
        this.dataInscricao = dataInscricao;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(Long alunoId) {
        this.alunoId = alunoId;
    }

    public String getNomeAluno() {
        return nomeAluno;
    }

    public void setNomeAluno(String nomeAluno) {
        this.nomeAluno = nomeAluno;
    }

    public Long getAulaId() {
        return aulaId;
    }

    public void setAulaId(Long aulaId) {
        this.aulaId = aulaId;
    }

    public String getNomeAula() {
        return nomeAula;
    }

    public void setNomeAula(String nomeAula) {
        this.nomeAula = nomeAula;
    }

    public LocalDateTime getDataInscricao() {
        return dataInscricao;
    }

    public void setDataInscricao(LocalDateTime dataInscricao) {
        this.dataInscricao = dataInscricao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
