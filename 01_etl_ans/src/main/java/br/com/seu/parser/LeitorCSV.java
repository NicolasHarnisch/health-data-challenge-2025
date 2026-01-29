package br.com.seu.parser;

import br.com.seu.model.Despesa;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LeitorCSV {

    private static final Pattern YEAR_PATTERN = Pattern.compile("20\\d{2}");

    public List<Despesa> parse(File file) {
        List<Despesa> result = new ArrayList<>();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();

        // Encoding ISO-8859-1 para suportar acentos
        try (Reader reader = new InputStreamReader(new FileInputStream(file), Charset.forName("ISO-8859-1"));
             CSVParser parser = new CSVParser(reader, format)) {

            for (CSVRecord record : parser) {
                Despesa d = mapRecord(record, file.getName());
                if (d != null) {
                    result.add(d);
                }
            }

        } catch (Exception e) {
            System.err.println("Erro de parsing no arquivo " + file.getName() + ": " + e.getMessage());
        }
        return result;
    }

    private Despesa mapRecord(CSVRecord record, String filename) {
        try {
            Despesa d = new Despesa();

            // Lógica de fallback para colunas que mudam de nome
            if (record.isMapped("REG_ANS")) d.setCnpj(record.get("REG_ANS"));
            else if (record.isMapped("CD_OPERADORA")) d.setCnpj(record.get("CD_OPERADORA"));

            if (record.isMapped("NM_RAZAO_SOCIAL")) d.setRazaoSocial(record.get("NM_RAZAO_SOCIAL"));
            else if (record.isMapped("Razao_Social")) d.setRazaoSocial(record.get("Razao_Social"));

            String valStr = "0";
            if (record.isMapped("VL_SALDO_FINAL")) valStr = record.get("VL_SALDO_FINAL");
            else if (record.isMapped("Valor")) valStr = record.get("Valor");

            d.setValor(parseCurrency(valStr));
            d.setTrimestre(extractQuarter(filename));
            d.setAno(extractYear(filename));

            return d;
        } catch (Exception e) {
            return null; // Linha inválida, ignora
        }
    }

    private BigDecimal parseCurrency(String val) {
        if (val == null || val.isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(val.replace(".", "").replace(",", "."));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private String extractQuarter(String filename) {
        if (filename.contains("1T")) return "1T";
        if (filename.contains("2T")) return "2T";
        if (filename.contains("3T")) return "3T";
        if (filename.contains("4T")) return "4T";
        return "N/A";
    }

    private int extractYear(String filename) {
        Matcher m = YEAR_PATTERN.matcher(filename);
        return m.find() ? Integer.parseInt(m.group()) : 0;
    }
}