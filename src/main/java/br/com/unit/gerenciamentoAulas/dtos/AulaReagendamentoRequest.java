package br.com.unit.gerenciamentoAulas.dtos;

import java.time.LocalDateTime;

public class AulaReagendamentoRequest {

    private LocalDateTime novaDataHoraInicio;
    private LocalDateTime novaDataHoraFim;
    private Long novoLocalId;
    private Integer novasVagasTotais;
    private String observacoes;

    public LocalDateTime getNovaDataHoraInicio() {
        return novaDataHoraInicio;
    }

    public void setNovaDataHoraInicio(LocalDateTime novaDataHoraInicio) {
        this.novaDataHoraInicio = novaDataHoraInicio;
    }

    public LocalDateTime getNovaDataHoraFim() {
        return novaDataHoraFim;
    }

    public void setNovaDataHoraFim(LocalDateTime novaDataHoraFim) {
        this.novaDataHoraFim = novaDataHoraFim;
    }

    public Long getNovoLocalId() {
        return novoLocalId;
    }

    public void setNovoLocalId(Long novoLocalId) {
        this.novoLocalId = novoLocalId;
    }

    public Integer getNovasVagasTotais() {
        return novasVagasTotais;
    }

    public void setNovasVagasTotais(Integer novasVagasTotais) {
        this.novasVagasTotais = novasVagasTotais;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
