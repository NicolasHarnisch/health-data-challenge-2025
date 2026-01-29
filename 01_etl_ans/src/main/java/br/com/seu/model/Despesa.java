package br.com.seu.model;

import java.math.BigDecimal;

public class Despesa {
    private String cnpj;
    private String razaoSocial;
    private String trimestre;
    private int ano;
    private BigDecimal valor;

    public Despesa() {}

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }

    public String getTrimestre() { return trimestre; }
    public void setTrimestre(String trimestre) { this.trimestre = trimestre; }

    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    @Override
    public String toString() {
        return String.format("%s (%s/%d): %s", razaoSocial, trimestre, ano, valor);
    }
}