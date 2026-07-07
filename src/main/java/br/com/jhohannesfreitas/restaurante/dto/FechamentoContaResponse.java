package br.com.jhohannesfreitas.restaurante.dto;

import br.com.jhohannesfreitas.restaurante.domain.entity.FechamentoConta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FechamentoContaResponse(
        Long id,
        Long pedidoId,
        Integer numeroMesa,
        BigDecimal subtotal, // Sempre usar BigDecimal para valores monetários
        BigDecimal taxaServico,
        BigDecimal desconto,
        BigDecimal total,
        LocalDateTime dataFechamento
) {

    public static FechamentoContaResponse fromEntity(FechamentoConta fechamento) {
        return new FechamentoContaResponse(
                fechamento.getId(),
                fechamento.getPedido().getId(),
                fechamento.getPedido().getMesa().getNumero(),
                fechamento.getSubtotal(),
                fechamento.getTaxaServico(),
                fechamento.getDesconto(),
                fechamento.getTotal(),
                fechamento.getDataFechamento()
        );
    }

}
