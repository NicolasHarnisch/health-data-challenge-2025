package br.com.seu.etl;

import br.com.seu.model.Despesa;
import br.com.seu.parser.LeitorCSV;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProcessadorETL {

    // Lista estática para simplificar passagem de dados entre etapas
    public static List<Despesa> DESPESAS_TOTAIS = new ArrayList<>();
    private static final String TEMP_DIR = "data/temp/";

    public void executar() {
        System.out.println("Processando CSVs...");
        File root = new File(TEMP_DIR);

        if (!root.exists()) {
            System.err.println("Erro: Pasta temp não existe.");
            return;
        }

        walkAndProcess(root);
        System.out.println("Total carregado: " + DESPESAS_TOTAIS.size() + " registros.");
    }

    private void walkAndProcess(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                walkAndProcess(f);
            } else if (f.getName().toLowerCase().endsWith(".csv")) {
                try {
                    LeitorCSV parser = new LeitorCSV();
                    List<Despesa> batch = parser.parse(f);
                    DESPESAS_TOTAIS.addAll(batch);
                    System.out.println("  -> Lido: " + f.getName() + " (" + batch.size() + " linhas)");
                } catch (Exception e) {
                    System.err.println("  -> Erro ao ler " + f.getName() + ": " + e.getMessage());
                }
            }
        }
    }
}