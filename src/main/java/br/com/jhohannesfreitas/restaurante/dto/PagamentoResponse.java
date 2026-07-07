package br.com.jhohannesfreitas.restaurante.dto;

public record PagamentoResponse(
        String status,
        String codigoTransacao
) {
}
