package br.com.seu;

import br.com.seu.service.BaixadorCadastro;
import br.com.seu.service.ProcessadorJoin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("Módulo 2: Validação");

            Path pastaRaiz = Paths.get(".").toAbsolutePath().normalize();
            Path consolidadoParte1 = pastaRaiz.resolve("data/processed/consolidado_despesas.csv");

            Path pastaTemp = pastaRaiz.resolve("data/temp");
            Path saidaFinal = pastaRaiz.resolve("data/processed/despesas_agregadas.csv");

            Files.createDirectories(pastaTemp);

            // 1. Baixar Cadastro
            BaixadorCadastro baixador = new BaixadorCadastro();
            Path arquivoCadastro = baixador.baixarArquivo(pastaTemp);

            // 2. Processar
            ProcessadorJoin processador = new ProcessadorJoin();
            processador.processar(consolidadoParte1, arquivoCadastro, saidaFinal);

            System.out.println("Processo Finalizado");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}