package br.com.seu.util;

import br.com.seu.model.DespesaAgregada;
import br.com.seu.model.Operadora;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CsvUtil {

    public static List<DespesaAgregada> lerDespesasConsolidadas(Path caminho) throws IOException {
        List<DespesaAgregada> lista = new ArrayList<>();

        Reader reader = Files.newBufferedReader(caminho, StandardCharsets.UTF_8);
        CSVParser parser = CSVFormat.DEFAULT.withDelimiter(';').withFirstRecordAsHeader().parse(reader);

        for (CSVRecord record : parser) {
            try {
                String cnpj = "";
                if (record.isMapped("REG_ANS")) {
                    cnpj = record.get("REG_ANS");
                } else {
                    cnpj = record.get(0);
                }

                String valorTexto = "";
                if (record.isMapped("VL_SALDO_FINAL")) {
                    valorTexto = record.get("VL_SALDO_FINAL");
                } else if (record.size() > 1) {
                    valorTexto = record.get(1);
                }

                if (valorTexto != null) {
                    valorTexto = valorTexto.replace("R$", "").replace(".", "").replace(",", ".").trim();
                }

                BigDecimal valor = BigDecimal.ZERO;
                if (valorTexto != null && !valorTexto.isEmpty()) {
                    valor = new BigDecimal(valorTexto);
                }

                if (valor.doubleValue() < 0) continue;

                String razao = "Desconhecida";
                if (record.isMapped("NM_RAZAO_SOCIAL")) {
                    razao = record.get("NM_RAZAO_SOCIAL");
                }
                if (razao == null || razao.trim().isEmpty()) razao = "Sem Nome";

                DespesaAgregada d = new DespesaAgregada();
                d.setModalidade(cnpj);
                d.setValorTotal(valor);
                d.setRazaoSocial(razao);

                lista.add(d);

            } catch (Exception e) {
                continue;
            }
        }
        return lista;
    }

    public static List<Operadora> lerCadastroOperadoras(Path caminho) throws IOException {
        List<Operadora> lista = new ArrayList<>();

        Reader reader = Files.newBufferedReader(caminho, StandardCharsets.ISO_8859_1);
        CSVParser parser = CSVFormat.DEFAULT.withDelimiter(';').withFirstRecordAsHeader().withIgnoreHeaderCase().parse(reader);

        for (CSVRecord record : parser) {
            try {
                Operadora op = new Operadora();
                op.setRegistroAns(record.get(0));

                try { op.setCnpj(record.get("CNPJ")); } catch (Exception e) { op.setCnpj(""); }
                try { op.setRazaoSocial(record.get("Razao_Social")); } catch (Exception e) { op.setRazaoSocial(""); }
                try { op.setModalidade(record.get("Modalidade")); } catch (Exception e) { op.setModalidade("Desconhecida"); }
                try { op.setUf(record.get("UF")); } catch (Exception e) { op.setUf("ND"); }

                lista.add(op);
            } catch (Exception e) {
                continue;
            }
        }
        return lista;
    }

    public static void escreverCsvFinal(List<DespesaAgregada> dados, Path caminho) throws IOException {
        FileWriter writer = new FileWriter(caminho.toFile(), StandardCharsets.UTF_8);
        CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withDelimiter(';').withHeader(
                "Razao_Social", "UF", "Modalidade", "Valor_Total", "Media_Trimestral", "Desvio_Padrao", "CNPJ_Valido"));

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
        printer.close();
    }
}