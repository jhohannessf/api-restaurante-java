package br.com.jhohannesfreitas.restaurante.service;

import br.com.jhohannesfreitas.restaurante.domain.entity.Mesa;
import br.com.jhohannesfreitas.restaurante.domain.entity.Pedido;
import br.com.jhohannesfreitas.restaurante.domain.entity.PedidoItem;
import br.com.jhohannesfreitas.restaurante.domain.entity.Produto;
import br.com.jhohannesfreitas.restaurante.domain.enums.StatusItemPedido;
import br.com.jhohannesfreitas.restaurante.domain.enums.StatusMesa;
import br.com.jhohannesfreitas.restaurante.domain.enums.StatusPedido;
import br.com.jhohannesfreitas.restaurante.dto.PedidoItemRequest;
import br.com.jhohannesfreitas.restaurante.dto.PedidoItemResponse;
import br.com.jhohannesfreitas.restaurante.dto.PedidoRequest;
import br.com.jhohannesfreitas.restaurante.dto.PedidoResponse;
import br.com.jhohannesfreitas.restaurante.exception.RegraNegocioException;
import br.com.jhohannesfreitas.restaurante.repository.MesaRepository;
import br.com.jhohannesfreitas.restaurante.repository.PedidoItemRepository;
import br.com.jhohannesfreitas.restaurante.repository.PedidoRepository;
import br.com.jhohannesfreitas.restaurante.repository.ProdutoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final MesaRepository mesaRepository;
    private final ProdutoRepository produtoRepository;
    private final PedidoItemRepository pedidoItemRepository;

    public PedidoService(PedidoRepository pedidoRepository, MesaRepository mesaRepository,ProdutoRepository produtoRepository, PedidoItemRepository pedidoItemRepository) {
        this.pedidoRepository = pedidoRepository;
        this.mesaRepository = mesaRepository;
        this.produtoRepository = produtoRepository;
        this.pedidoItemRepository = pedidoItemRepository;
    }

    public PedidoResponse abrirPedido(PedidoRequest pedidoRequest) {

        // Regra de negócio
        Mesa mesa = mesaRepository.findById(pedidoRequest.mesaId())
                .orElseThrow(() -> new RegraNegocioException("Mesa inexistente"));
        if (mesa.getStatus() != StatusMesa.LIVRE) {
            throw new RegraNegocioException("Mesa não está livre para abertura de pedido.");
        }

        // Cria o pedido
        Pedido pedido = new Pedido();
        pedido.setMesa(mesa);
        pedido.setStatus(StatusPedido.ABERTO);
        pedido.setObservacao(pedidoRequest.observacao());

        // Altera o Status da Mesa
        mesa.setStatus(StatusMesa.OCUPADA);

        // Salva o Pedido e a Mesa
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        mesaRepository.save(mesa);

        return PedidoResponse.fromEntity(pedidoSalvo);
    }

    public Page<PedidoResponse> listar(Pageable pageable) {
        return pedidoRepository.findAll(pageable).map(PedidoResponse::fromEntity);
    }

    public PedidoResponse buscarPorId(Long id) {
        Pedido pedido = buscarPedidoPorId(id);
        return PedidoResponse.fromEntity(pedido);
    }

    public PedidoItemResponse adicionarItemPedido(Long pedidoId, PedidoItemRequest request) {
        Pedido pedido = buscarPedidoPorId(pedidoId);
        if (pedido.getStatus() != StatusPedido.ABERTO) {
            throw new RegraNegocioException("Só é possível adicionar itens em pedido abertos.");
        }

        Produto produto = produtoRepository.findById(request.produtoId())
                .orElseThrow(() -> new RegraNegocioException("Produto não encontrado."));

        if (!produto.getDisponivel()) {
            throw new RegraNegocioException("Produto indisponível no cardápio");
        }

        if(request.quantidade() == null || request.quantidade() <= 0) {
            throw new RegraNegocioException("A quantidade deve ser maior que zero");
        }

        PedidoItem pedidoItem = new PedidoItem();
        pedidoItem.setPedido(pedido);
        pedidoItem.setProduto(produto);
        pedidoItem.setQuantidade(request.quantidade());
        pedidoItem.setPrecoUnitario(produto.getPreco());
        pedidoItem.setObservacao(request.observacao());
        pedidoItem.setStatus(StatusItemPedido.PENDENTE);

        PedidoItem itemSalvo = pedidoItemRepository.save(pedidoItem);

        return PedidoItemResponse.fromEntity(itemSalvo);

    }

    public List<PedidoItemResponse> listarItens(Long pedidoId) {
        buscarPedidoPorId(pedidoId);

        return pedidoItemRepository.findByPedidoId(pedidoId)
                .stream()
                .map(PedidoItemResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private Pedido buscarPedidoPorId(Long pedidoId) {
        return pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RegraNegocioException("Pedido não encontrado"));
    }
}
