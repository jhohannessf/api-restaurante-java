package br.com.jhohannesfreitas.restaurante.dto;

import java.math.BigDecimal;

public record FechamentoContaRequest(
        BigDecimal taxaServico,
        BigDecimal desconto
) {
}
