package br.com.jhohannesfreitas.restaurante.dto;

import br.com.jhohannesfreitas.restaurante.domain.entity.PedidoItem;
import br.com.jhohannesfreitas.restaurante.domain.enums.StatusItemPedido;

import java.math.BigDecimal;

public record CozinhaItemResponse(
        Long itemId,
        Long pedidoId,
        Integer numeroMesa,
        String produtoNome,
        Integer quantidade,
        String observacao,
        BigDecimal precoUnitario,
        StatusItemPedido status
) {
    // Mapper de saída: Transforma uma entidade em um DTO
    public static CozinhaItemResponse fromEntity(PedidoItem item) {
        return new CozinhaItemResponse(
                item.getId(),
                item.getPedido().getId(),
                item.getPedido().getMesa().getNumero(),
                item.getProduto().getNome(),
                item.getQuantidade(),
                item.getObservacao(),
                item.getPrecoUnitario(),
                item.getStatus()
        );
    }



}
