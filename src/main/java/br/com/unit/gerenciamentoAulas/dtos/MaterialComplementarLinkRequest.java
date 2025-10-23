package br.com.unit.gerenciamentoAulas.dtos;

public class MaterialComplementarLinkRequest {
    private String titulo;
    private String url;
    private String tipo;

    public MaterialComplementarLinkRequest() {}

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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}