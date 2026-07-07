package br.com.jhohannesfreitas.restaurante.dto;

import br.com.jhohannesfreitas.restaurante.domain.entity.CategoriaProduto;
import br.com.jhohannesfreitas.restaurante.domain.entity.Produto;

import java.math.BigDecimal;

public record ProdutoRequest(
        Long categoriaId,
        String nome,
        String descricao,
        BigDecimal preco,
        Boolean disponivel,
        Integer tempoPreparoMinutos
) {

    // Mapper de entrada: Transforma uma DTO em uma Entidade
    public Produto toEntity(CategoriaProduto categoria) {
        Produto produto = new Produto();
        prencher(produto, categoria);
        return produto;
    }

    public void prencher(Produto produto, CategoriaProduto categoria) {
        produto.setCategoria(categoria);
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setPreco(preco);
        produto.setDisponivel(disponivel != null ? disponivel: true);
        produto.setTempoPreparoMinutos(tempoPreparoMinutos);
    }

}
