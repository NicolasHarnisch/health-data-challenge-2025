package br.com.seu.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class EstatisticaService {

    public static BigDecimal calcularDesvioPadrao(List<BigDecimal> valores, BigDecimal media) {
        if (valores == null || valores.size() <= 1) {
            return BigDecimal.ZERO;
        }

        double mediaDouble = media.doubleValue();
        double somaDiferencas = 0.0;

        for (BigDecimal valor : valores) {
            double v = valor.doubleValue();
            double diferenca = v - mediaDouble;
            somaDiferencas += (diferenca * diferenca);
        }

        double variancia = somaDiferencas / valores.size();
        double desvio = Math.sqrt(variancia);

        return new BigDecimal(desvio).setScale(2, RoundingMode.HALF_UP);
    }
}