package br.com.seu.util;

import br.com.seu.model.DespesaAgregada;
import br.com.seu.model.Operadora;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CsvUtil {

    // Configuração do formato CSV usando o Builder (Padrão moderno)
    private static final CSVFormat FORMATO_ANS = CSVFormat.DEFAULT.builder()
            .setDelimiter(';')
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreHeaderCase(true)
            .setTrim(true)
            .build();

    public static List<DespesaAgregada> lerDespesasConsolidadas(Path caminho) throws IOException {
        List<DespesaAgregada> lista = new ArrayList<>();

        // Try-with-resources garante que o arquivo será fechado automaticamente
        try (Reader reader = Files.newBufferedReader(caminho, StandardCharsets.UTF_8);
             CSVParser parser = FORMATO_ANS.parse(reader)) {

            for (CSVRecord record : parser) {
                try {
                    String regAns = record.isMapped("REG_ANS") ? record.get("REG_ANS") : record.get(0);
                    
                    String valorTexto = record.isMapped("VL_SALDO_FINAL") ? record.get("VL_SALDO_FINAL") : record.get(1);

                    if (valorTexto != null) {
                        valorTexto = valorTexto.replace("R$", "").replace(".", "").replace(",", ".").trim();
                    }

                    BigDecimal valor = (valorTexto != null && !valorTexto.isEmpty()) 
                                       ? new BigDecimal(valorTexto) 
                                       : BigDecimal.ZERO;

                    if (valor.doubleValue() < 0) continue;

                    String razao = record.isMapped("NM_RAZAO_SOCIAL") ? record.get("NM_RAZAO_SOCIAL") : "Sem Nome";

                    DespesaAgregada d = new DespesaAgregada();
                    // Ajustado de 'cnpj' para 'regAns' para ficar mais claro
                    d.setModalidade(regAns); 
                    d.setValorTotal(valor);
                    d.setRazaoSocial(razao);

                    lista.add(d);
                } catch (Exception e) {
                    // Ignora linhas com erro de parsing
                }
            }
        }
        return lista;
    }

    public static List<Operadora> lerCadastroOperadoras(Path caminho) throws IOException {
        List<Operadora> lista = new ArrayList<>();

        // Arquivos da ANS geralmente vêm em ISO_8859_1
        try (Reader reader = Files.newBufferedReader(caminho, StandardCharsets.ISO_8859_1);
             CSVParser parser = FORMATO_ANS.parse(reader)) {

            for (CSVRecord record : parser) {
                try {
                    Operadora op = new Operadora();
                    op.setRegistroAns(record.get(0));
                    
                    // Uso de .getOptional() ou try-catch simples para colunas opcionais
                    op.setCnpj(record.isMapped("CNPJ") ? record.get("CNPJ") : "");
                    op.setRazaoSocial(record.isMapped("Razao_Social") ? record.get("Razao_Social") : "");
                    op.setModalidade(record.isMapped("Modalidade") ? record.get("Modalidade") : "Desconhecida");
                    op.setUf(record.isMapped("UF") ? record.get("UF") : "ND");

                    lista.add(op);
                } catch (Exception e) {
                    continue;
                }
            }
        }
        return lista;
    }

    public static void escreverCsvFinal(List<DespesaAgregada> dados, Path caminho) throws IOException {
        CSVFormat formatoEscrita = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setHeader("Razao_Social", "UF", "Modalidade", "Valor_Total", "Media_Trimestral", "Desvio_Padrao", "CNPJ_Valido")
                .build();

        try (BufferedWriter writer = Files.newBufferedWriter(caminho, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, formatoEscrita)) {

            for (DespesaAgregada d : dados) {
                printer.printRecord(
                        d.getRazaoSocial(),
                        d.getUf(),
                        d.getModalidade(),
                        d.getValorTotal(),
                        d.getMediaTrimestral(),
                        d.getDesvioPadrao(),
                        d.isCnpjValido() ? "SIM" : "NAO"
                );
            }
            printer.flush();
        }
    }
}