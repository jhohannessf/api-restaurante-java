package br.com.jhohannesfreitas.restaurante.dto;

import br.com.jhohannesfreitas.restaurante.domain.entity.PedidoItem;
import br.com.jhohannesfreitas.restaurante.domain.enums.StatusItemPedido;

import java.math.BigDecimal;

public record PedidoItemResponse(
        Long id,
        Long produtoId,
        Long pedidoId,
        String produtoNome,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal total,
        String observacao,
        StatusItemPedido status
) {

    // Mapper de saída: Transforma uma entidade em um DTO
    public static PedidoItemResponse fromEntity(PedidoItem item) {
        BigDecimal total = item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()));

        return new PedidoItemResponse(
                item.getId(),
                item.getPedido().getId(),
                item.getProduto().getId(),
                item.getProduto().getNome(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                total,
                item.getObservacao(),
                item.getStatus()
        );
    }

}
