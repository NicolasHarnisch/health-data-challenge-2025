package br.com.seu.etl;

import br.com.seu.model.Despesa;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Consolidador {

    private static final Logger log = Logger.getLogger(Consolidador.class.getName());
    private static final String OUTPUT_DIR = "data/processed/";
    private static final String CSV_FILE = "consolidado_despesas.csv";
    private static final String ZIP_FILE = "consolidado_despesas.zip";

    public void executar(List<Despesa> data) {
        log.info("Iniciando consolidação...");

        if (data == null || data.isEmpty()) {
            log.warning("Lista vazia. Nada a fazer.");
            return;
        }

        new File(OUTPUT_DIR).mkdirs();

        if (createCsv(data)) {
            compress();
        }
    }

    private boolean createCsv(List<Despesa> data) {
        String path = OUTPUT_DIR + CSV_FILE;

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("CNPJ", "RazaoSocial", "Trimestre", "Ano", "ValorDespesas")
                .setDelimiter(';')
                .build();

        int count = 0;
        int skipped = 0;

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path), StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, format)) {

            for (Despesa d : data) {
                // Filtra valores <= 0
                if (d.getValor() == null || d.getValor().compareTo(BigDecimal.ZERO) <= 0) {
                    skipped++;
                    continue;
                }

                printer.printRecord(
                        cleanCnpj(d.getCnpj()),
                        d.getRazaoSocial(),
                        d.getTrimestre(),
                        d.getAno(),
                        d.getValor()
                );
                count++;
            }

            log.info(String.format("CSV gerado. Válidos: %d | Removidos: %d", count, skipped));
            return true;

        } catch (IOException e) {
            log.severe("Erro ao criar CSV: " + e.getMessage());
            return false;
        }
    }

    private void compress() {
        String zipPath = OUTPUT_DIR + ZIP_FILE;
        String csvPath = OUTPUT_DIR + CSV_FILE;

        try (FileOutputStream fos = new FileOutputStream(zipPath);
             ZipOutputStream zos = new ZipOutputStream(fos);
             FileInputStream fis = new FileInputStream(csvPath)) {

            zos.putNextEntry(new ZipEntry(CSV_FILE));

            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) >= 0) {
                zos.write(buffer, 0, length);
            }
            log.info("ZIP gerado com sucesso: " + zipPath);

        } catch (IOException e) {
            log.severe("Erro ao compactar: " + e.getMessage());
        }
    }

    private String cleanCnpj(String text) {
        return text == null ? "" : text.replaceAll("\\D", "");
    }
}