package br.com.unit.gerenciamentoAulas.dtos;

import java.util.Arrays;
import java.util.Objects;

public class MaterialComplementarDTO {

    private Long aulaId;
    private String titulo;
    private String url;
    private String nomeArquivo;
    private String contentType;
    private byte[] arquivo;

    public MaterialComplementarDTO() {
    }

    public MaterialComplementarDTO(Long aulaId, String titulo, String url,
                                   String nomeArquivo, String contentType, byte[] arquivo) {
        this.aulaId = aulaId;
        this.titulo = titulo;
        this.url = url;
        this.nomeArquivo = nomeArquivo;
        this.contentType = contentType;
        setArquivo(arquivo);
    }

    public static MaterialComplementarDTO criarLink(Long aulaId, String titulo, String url) {
        MaterialComplementarDTO dto = new MaterialComplementarDTO();
        dto.setAulaId(aulaId);
        dto.setTitulo(titulo);
        dto.setUrl(url);
        return dto;
    }

    public static MaterialComplementarDTO criarArquivo(Long aulaId, String titulo, String nomeArquivo,
                                                       String contentType, byte[] arquivo) {
        MaterialComplementarDTO dto = new MaterialComplementarDTO();
        dto.setAulaId(aulaId);
        dto.setTitulo(titulo);
        dto.setNomeArquivo(nomeArquivo);
        dto.setContentType(contentType);
        dto.setArquivo(arquivo);
        return dto;
    }

    public Long getAulaId() {
        return aulaId;
    }

    public void setAulaId(Long aulaId) {
        this.aulaId = aulaId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getArquivo() {
        return arquivo == null ? null : Arrays.copyOf(arquivo, arquivo.length);
    }

    public void setArquivo(byte[] arquivo) {
        this.arquivo = arquivo == null ? null : Arrays.copyOf(arquivo, arquivo.length);
    }

    public boolean possuiLink() {
        return url != null && !url.isBlank();
    }

    public boolean possuiArquivo() {
        return arquivo != null && arquivo.length > 0;
    }

    public String obterNomeParaDownload() {
        if (nomeArquivo != null && !nomeArquivo.isBlank()) {
            return nomeArquivo;
        }
        if (titulo != null && !titulo.isBlank()) {
            return titulo.replaceAll("\\s+", "_") + ".pdf";
        }
        return "material.pdf";
    }

    public void validar() {
        boolean temLink = possuiLink();
        boolean temArquivo = possuiArquivo();

        if (temLink && temArquivo) {
            throw new IllegalStateException("Material complementar não pode ser link e arquivo simultaneamente.");
        }

        if (!temLink && !temArquivo) {
            throw new IllegalStateException("Material complementar precisa de um link ou arquivo.");
        }
    }

    @Override
    public String toString() {
        return "MaterialComplementarDTO{" +
                "aulaId=" + aulaId +
                ", titulo='" + titulo + '\'' +
                ", url='" + url + '\'' +
                ", nomeArquivo='" + nomeArquivo + '\'' +
                ", contentType='" + contentType + '\'' +
                ", possuiArquivo=" + possuiArquivo() +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(aulaId, titulo, url, nomeArquivo, contentType);
    }
}

