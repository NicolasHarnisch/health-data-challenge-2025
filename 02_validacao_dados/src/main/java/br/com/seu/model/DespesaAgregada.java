package br.com.seu.model;

import java.math.BigDecimal;

public class DespesaAgregada {
    private String razaoSocial;
    private String uf;
    private String modalidade;
    private BigDecimal valorTotal = BigDecimal.ZERO;
    private BigDecimal mediaTrimestral = BigDecimal.ZERO;
    private BigDecimal desvioPadrao = BigDecimal.ZERO;
    private boolean cnpjValido;

    public DespesaAgregada() {
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getModalidade() {
        return modalidade;
    }

    public void setModalidade(String modalidade) {
        this.modalidade = modalidade;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public BigDecimal getMediaTrimestral() {
        return mediaTrimestral;
    }

    public void setMediaTrimestral(BigDecimal mediaTrimestral) {
        this.mediaTrimestral = mediaTrimestral;
    }

    public BigDecimal getDesvioPadrao() {
        return desvioPadrao;
    }

    public void setDesvioPadrao(BigDecimal desvioPadrao) {
        this.desvioPadrao = desvioPadrao;
    }

    public boolean isCnpjValido() {
        return cnpjValido;
    }

    public void setCnpjValido(boolean cnpjValido) {
        this.cnpjValido = cnpjValido;
    }
}
