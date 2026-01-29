package br.com.seu;

import br.com.seu.etl.Consolidador;
import br.com.seu.etl.CrawlerANS;
import br.com.seu.etl.Descompactador;
import br.com.seu.etl.ProcessadorETL;

public class Main {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        System.out.println(">>> Iniciando Job ETL");

        try {
            // 1. Ingestão
            new CrawlerANS().executar();

            // 2. Preparação
            new Descompactador().executar();

            // 3. Processamento
            ProcessadorETL processador = new ProcessadorETL();
            processador.executar();

            // 4. Consolidação
            new Consolidador().executar(ProcessadorETL.DESPESAS_TOTAIS);

        } catch (Exception e) {
            System.err.println("Erro fatal na execução: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.printf("Job finalizado em %ds%n", (System.currentTimeMillis() - start) / 1000);
    }
}