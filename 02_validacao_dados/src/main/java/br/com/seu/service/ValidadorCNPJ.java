package br.com.seu.service;

public class ValidadorCNPJ {

    public static boolean isValido(String cnpj) {
        if (cnpj == null || cnpj.length() == 0) return false;

        String numeros = cnpj.replaceAll("\\D", "");

        if (numeros.length() != 14 || numeros.equals("00000000000000") || numeros.equals("11111111111111") ||
                numeros.equals("22222222222222") || numeros.equals("33333333333333") ||
                numeros.equals("44444444444444") || numeros.equals("55555555555555") ||
                numeros.equals("66666666666666") || numeros.equals("77777777777777") ||
                numeros.equals("88888888888888") || numeros.equals("99999999999999")) {
            return false;
        }

        char dig13, dig14;
        int sm, i, r, num, peso;

        try {
            sm = 0;
            peso = 2;
            for (i = 11; i >= 0; i--) {
                num = (int) (numeros.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10) peso = 2;
            }

            r = sm % 11;
            if ((r == 0) || (r == 1)) dig13 = '0';
            else dig13 = (char) ((11 - r) + 48);

            sm = 0;
            peso = 2;
            for (i = 12; i >= 0; i--) {
                num = (int) (numeros.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10) peso = 2;
            }

            r = sm % 11;
            if ((r == 0) || (r == 1)) dig14 = '0';
            else dig14 = (char) ((11 - r) + 48);

            if ((dig13 == numeros.charAt(12)) && (dig14 == numeros.charAt(13)))
                return true;
            else
                return false;

        } catch (Exception e) {
            return false;
        }
    }
}