package br.com.jhohannesfreitas.restaurante.dto;

public record PedidoItemRequest(
        Long produtoId,
        Integer quantidade,
        String observacao
) {
}
