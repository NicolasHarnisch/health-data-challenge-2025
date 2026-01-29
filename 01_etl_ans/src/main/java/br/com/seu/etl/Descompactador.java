package br.com.seu.etl;

import java.io.*;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Descompactador {

    private static final Logger log = Logger.getLogger(Descompactador.class.getName());
    private static final String RAW_PATH = "data/raw/";
    private static final String TEMP_PATH = "data/temp/";

    public void executar() {
        log.info("Extraindo arquivos...");

        File[] zips = new File(RAW_PATH).listFiles((d, name) -> name.toLowerCase().endsWith(".zip"));

        if (zips == null || zips.length == 0) {
            log.warning("Nenhum ZIP encontrado em " + RAW_PATH);
            return;
        }

        for (File zip : zips) {
            unzip(zip);
        }
    }

    private void unzip(File zipFile) {
        String folderName = zipFile.getName().replace(".zip", "");
        File targetDir = new File(TEMP_PATH + folderName);

        // ISO-8859-1 é mandatório para arquivos legados do governo
        Charset charset = Charset.forName("ISO-8859-1");

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile), charset)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(targetDir, entry.getName());

                if (entry.isDirectory()) {
                    outFile.mkdirs();
                    continue;
                }

                new File(outFile.getParent()).mkdirs();

                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    byte[] buffer = new byte[8192]; // 8KB buffer
                    int len;
                    while ((len = zis.read(buffer)) > 0) fos.write(buffer, 0, len);
                }
            }
        } catch (IOException e) {
            log.severe("Falha ao descompactar " + zipFile.getName() + ": " + e.getMessage());
        }
    }
}