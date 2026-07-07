package br.com.jhohannesfreitas.restaurante.service;

import br.com.jhohannesfreitas.restaurante.domain.entity.FechamentoConta;
import br.com.jhohannesfreitas.restaurante.domain.entity.Pedido;
import br.com.jhohannesfreitas.restaurante.domain.entity.PedidoItem;
import br.com.jhohannesfreitas.restaurante.domain.enums.StatusItemPedido;
import br.com.jhohannesfreitas.restaurante.domain.enums.StatusPedido;
import br.com.jhohannesfreitas.restaurante.dto.FechamentoContaRequest;
import br.com.jhohannesfreitas.restaurante.dto.FechamentoContaResponse;
import br.com.jhohannesfreitas.restaurante.exception.RegraNegocioException;
import br.com.jhohannesfreitas.restaurante.repository.FechamentoContaRepository;
import br.com.jhohannesfreitas.restaurante.repository.PedidoItemRepository;
import br.com.jhohannesfreitas.restaurante.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FechamentoContaService {

    private final FechamentoContaRepository fechamentoContaRepository;
    private final PedidoRepository pedidoRepository;
    private final PedidoItemRepository pedidoItemRepository;

    public FechamentoContaService(FechamentoContaRepository fechamentoContaRepository, PedidoRepository pedidoRepository, PedidoItemRepository pedidoItemRepository) {
        this.fechamentoContaRepository = fechamentoContaRepository;
        this.pedidoRepository = pedidoRepository;
        this.pedidoItemRepository = pedidoItemRepository;
    }

    @Transactional
    public FechamentoContaResponse fecharConta(Long pedidoId, FechamentoContaRequest request) {
        Pedido pedido = buscarPedidoPorId(pedidoId);

        // Regras de negócio
        if (pedido.getStatus() == StatusPedido.FECHADO) {
            throw new RegraNegocioException("Pedido já está fechado.");
        }
        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new RegraNegocioException("Pedido cancelado não pode ser fechado.");
        }
        if (fechamentoContaRepository.existsByPedidoId(pedidoId)) {
            throw new RegraNegocioException("Já existe fechamento para este pedido.");
        }

        List<PedidoItem> itens = pedidoItemRepository.findByPedidoId(pedidoId);
        if (itens.isEmpty()) {
            throw new RegraNegocioException("Não é possível fechar uma conta sem itens.");
        }

        List<PedidoItem> itensNaoEntregue = pedidoItemRepository.findByPedidoIdAndStatusNot(pedidoId, StatusItemPedido.ENTREGUE);
        if (!itensNaoEntregue.isEmpty()) {
            throw new RegraNegocioException("Todos os itens precisam estar entregues para fechar a conta.");
        }

        BigDecimal subtotal = itens.stream()
                .map(item -> item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()))) // Pra cada item, preço x quantidade
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Inicia em Zero e Reduz vários valores para um único resultado. O '::add' é um método usado para somar

        BigDecimal taxaServico = request.taxaServico() != null ? request.taxaServico() : BigDecimal.ZERO; // if ternário: "Se existir taxa de serviço, usa ela. Caso contrário, usa 0."
        BigDecimal desconto = request.desconto() != null ? request.desconto() : BigDecimal.ZERO;

        if (taxaServico.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("Taxa de Serviço não pode ser negativa.");
        }

        if (desconto.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("Desconto não pode ser negativa.");
        }

        BigDecimal total = subtotal.add(taxaServico).subtract(desconto);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraNegocioException("Total da conta não pode ser negativo.");
        }

        FechamentoConta fechamento = new FechamentoConta();
        fechamento.setPedido(pedido);
        fechamento.setSubtotal(subtotal);
        fechamento.setTaxaServico(taxaServico);
        fechamento.setDesconto(desconto);
        fechamento.setTotal(total);

        pedido.setStatus(StatusPedido.FECHADO);
        pedido.setDataFechamento(LocalDateTime.now());

        FechamentoConta fechamentoSalvo = fechamentoContaRepository.save(fechamento);
        pedidoRepository.save(pedido);

        return FechamentoContaResponse.fromEntity(fechamentoSalvo);
    }

    public FechamentoContaResponse buscarPorPedido(Long pedidoId) {
        FechamentoConta fechamento = fechamentoContaRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RegraNegocioException("Fechamento não encontrado."));
        return FechamentoContaResponse.fromEntity(fechamento);
    }

    private Pedido buscarPedidoPorId(Long pedidoId) {
        return pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RegraNegocioException("Pedido não encontrado."));
    }
}
