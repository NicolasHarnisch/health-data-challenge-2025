package br.com.seu.parser;

import br.com.seu.model.Despesa;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LeitorCSVTest {

    @Test
    void processaCsv(@TempDir Path tempDir) throws IOException {
        File arquivo = tempDir.resolve("1T2025.csv").toFile();
        String conteudo = """
                REG_ANS;NM_RAZAO_SOCIAL;VL_SALDO_FINAL
                123456;Operadora Saúde e Vida;1.500,50
                999999;Operadora Teste Zero;0,00""";
        Files.writeString(arquivo.toPath(), conteudo, StandardCharsets.ISO_8859_1);

        LeitorCSV leitor = new LeitorCSV();
        List<Despesa> resultado = leitor.parse(arquivo);

        assertThat(resultado.size()).isEqualTo(2);
        assertThat(resultado.getFirst().getRazaoSocial()).contains("Saúde");
    }

    @Test
    void nomeArquivoInvalido(@TempDir Path tempDir) throws IOException {
        File arquivo = tempDir.resolve("arquivo_aleatorio.csv").toFile();
        Files.writeString(arquivo.toPath(), "REG_ANS;Valor\n1;10", StandardCharsets.ISO_8859_1);

        LeitorCSV leitor = new LeitorCSV();
        List<Despesa> resultado = leitor.parse(arquivo);

        assertThat(resultado).isNotEmpty();
        assertThat(resultado.getFirst().getTrimestre()).isEqualTo("N/A");
        assertThat(resultado.getFirst().getAno()).isEqualTo(0);
    }
}
