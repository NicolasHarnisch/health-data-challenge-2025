package br.com.seu.service;

import br.com.seu.model.DespesaAgregada;
import br.com.seu.model.Operadora;
import br.com.seu.util.CsvUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessadorJoin {

    public void processar(Path arquivoConsolidado, Path arquivoCadastro, Path saida) throws IOException {
        System.out.println("Lendo cadastro de operadoras...");
        List<Operadora> listaOperadoras = CsvUtil.lerCadastroOperadoras(arquivoCadastro);

        Map<String, Operadora> mapa = new HashMap<>();
        for (Operadora op : listaOperadoras) {
            String chave = op.getRegistroAns();
            if (chave != null) {
                mapa.put(chave.trim(), op);
            }
        }
        System.out.println("Operadoras carregadas: " + mapa.size());

        System.out.println("Lendo despesas financeiras...");
        List<DespesaAgregada> despesas = CsvUtil.lerDespesasConsolidadas(arquivoConsolidado);
        System.out.println("Registros lidos: " + despesas.size());

        Map<String, List<BigDecimal>> agrupamento = new HashMap<>();
        for (DespesaAgregada d : despesas) {
            String regAns = d.getModalidade();
            if (regAns == null) regAns = "";

            if (!agrupamento.containsKey(regAns)) {
                agrupamento.put(regAns, new ArrayList<>());
            }
            agrupamento.get(regAns).add(d.getValorTotal());
        }

        List<DespesaAgregada> resultado = new ArrayList<>();
        int naoEncontrados = 0;

        for (String regAns : agrupamento.keySet()) {
            List<BigDecimal> valores = agrupamento.get(regAns);

            BigDecimal total = BigDecimal.ZERO;
            for (BigDecimal v : valores) {
                total = total.add(v);
            }

            BigDecimal media = BigDecimal.ZERO;
            if (valores.size() > 0) {
                media = total.divide(new BigDecimal(valores.size()), 2, RoundingMode.HALF_UP);
            }

            BigDecimal desvio = EstatisticaService.calcularDesvioPadrao(valores, media);

            Operadora op = mapa.get(regAns.trim());

            DespesaAgregada linha = new DespesaAgregada();
            linha.setValorTotal(total);
            linha.setMediaTrimestral(media);
            linha.setDesvioPadrao(desvio);

            if (op != null) {
                linha.setRazaoSocial(op.getRazaoSocial());
                linha.setUf(op.getUf());
                linha.setModalidade(op.getModalidade());
                linha.setCnpjValido(ValidadorCNPJ.isValido(op.getCnpj()));
            } else {
                naoEncontrados++;
                linha.setRazaoSocial("NAO ENCONTRADA (Reg: " + regAns + ")");
                linha.setUf("ND");
                linha.setModalidade("ND");
                linha.setCnpjValido(false);
            }

            resultado.add(linha);
        }

        resultado.sort((a, b) -> b.getValorTotal().compareTo(a.getValorTotal()));

        System.out.println("Processamento finalizado.");
        System.out.println("Total processado: " + resultado.size());
        System.out.println("Nao encontrados no cadastro: " + naoEncontrados);

        CsvUtil.escreverCsvFinal(resultado, saida);
    }
}