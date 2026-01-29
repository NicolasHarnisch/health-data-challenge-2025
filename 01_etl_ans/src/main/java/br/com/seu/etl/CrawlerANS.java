package br.com.seu.etl;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class CrawlerANS {

    private static final Logger log = Logger.getLogger(CrawlerANS.class.getName());

    private static final String BASE_URL = "https://dadosabertos.ans.gov.br/FTP/PDA/demonstracoes_contabeis/";
    private static final String OUTPUT_DIR = "data/raw/";

    // Patterns compilados para evitar recompile no loop
    private static final Pattern YEAR_PATTERN = Pattern.compile("20\\d{2}");
    private static final Pattern QUARTER_PATTERN = Pattern.compile("\\d[Tt]20\\d{2}");

    public void executar() {
        log.info("Verificando atualizações na ANS...");

        try {
            // Busca anos disponíveis
            List<String> years = fetchLinks(BASE_URL, YEAR_PATTERN);
            // LinkedHashSet remove duplicatas mantendo ordem de inserção
            years = new ArrayList<>(new LinkedHashSet<>(years));
            years.sort(Collections.reverseOrder());

            List<String> toDownload = new ArrayList<>();

            // Varre diretórios buscando os últimos 3 trimestres
            for (String year : years) {
                if (toDownload.size() >= 3) break;

                String yearUrl = fixUrl(BASE_URL, year);
                List<String> quarters = fetchLinks(yearUrl, QUARTER_PATTERN);
                quarters.sort(Collections.reverseOrder());

                for (String q : quarters) {
                    if (toDownload.size() >= 3) break;

                    String fullUrl = fixUrl(yearUrl, q);
                    if (!toDownload.contains(fullUrl)) {
                        toDownload.add(fullUrl);
                    }
                }
            }

            log.info("Arquivos identificados: " + toDownload.size());

            for (String url : toDownload) {
                download(url);
            }

        } catch (Exception e) {
            log.log(Level.SEVERE, "Falha no Crawler", e);
        }
    }

    private String fixUrl(String base, String path) {
        if (path.startsWith("http")) return path;
        return base.endsWith("/") ? base + path : base + "/" + path;
    }

    private List<String> fetchLinks(String url, Pattern pattern) throws IOException {
        Document doc = Jsoup.connect(url)
                .timeout(10000)
                // User-Agent genérico para evitar bloqueio simples
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .get();

        List<String> matches = new ArrayList<>();
        for (Element link : doc.select("a[href]")) {
            String href = link.attr("href");
            if (href.contains("Parent Directory")) continue;

            if (pattern.matcher(href).find()) {
                matches.add(href);
            }
        }
        return matches;
    }

    private void download(String url) {
        try {
            // Se for diretório, entra e busca o ZIP. Se for ZIP, baixa direto.
            if (url.toLowerCase().endsWith(".zip")) {
                saveFile(url);
            } else {
                String folderUrl = fixUrl(url, "");
                Document doc = Jsoup.connect(folderUrl).get();
                Elements zips = doc.select("a[href$=.zip]");

                for (Element zip : zips) {
                    saveFile(fixUrl(folderUrl, zip.attr("href")));
                }
            }
        } catch (IOException e) {
            log.warning("Erro ao processar URL: " + url + " -> " + e.getMessage());
        }
    }

    private void saveFile(String url) throws IOException {
        String filename = url.substring(url.lastIndexOf("/") + 1);
        File dest = new File(OUTPUT_DIR + filename);

        if (dest.exists()) {
            log.fine("Cache hit: " + filename);
            return;
        }

        log.info("Baixando: " + filename);
        // Timeout maior para download (60s)
        FileUtils.copyURLToFile(new URL(url), dest, 15000, 60000);
    }
}