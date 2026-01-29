package br.com.seu.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValidadorCNPJTest {

    @Test
    void testCnpjValido() {
        boolean valido = ValidadorCNPJ.isValido("06.990.590/0001-23");
        Assertions.assertTrue(valido);

        Assertions.assertTrue(ValidadorCNPJ.isValido("06990590000123"));
    }

    @Test
    void testCnpjInvalido() {
        Assertions.assertFalse(ValidadorCNPJ.isValido("06.990.590/0001-24"));

        Assertions.assertFalse(ValidadorCNPJ.isValido("123"));

        Assertions.assertFalse(ValidadorCNPJ.isValido("00000000000000"));
    }
}