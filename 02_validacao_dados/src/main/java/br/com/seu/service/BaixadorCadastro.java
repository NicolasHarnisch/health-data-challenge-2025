package br.com.seu.service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class BaixadorCadastro {

    private static final String LINK_ARQUIVO = "https://dadosabertos.ans.gov.br/FTP/PDA/operadoras_de_plano_de_saude_ativas/Relatorio_cadop.csv";

    public Path baixarArquivo(Path pastaDestino) {
        try {
            if (!Files.exists(pastaDestino)) {
                Files.createDirectories(pastaDestino);
            }

            Path destino = pastaDestino.resolve("cadastro_operadoras.csv");
            System.out.println("Iniciando download do arquivo da ANS...");

            URL url = new URL(LINK_ARQUIVO);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                InputStream in = conn.getInputStream();
                Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
                in.close();
                System.out.println("Download finalizado: " + destino.toString());
                return destino;
            } else {
                throw new RuntimeException("Erro no download, codigo: " + responseCode);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao baixar: " + e.getMessage());
        }
    }
}