package br.com.jhohannesfreitas.restaurante.service;

import br.com.jhohannesfreitas.restaurante.client.PagamentoClient;
import br.com.jhohannesfreitas.restaurante.domain.entity.FechamentoConta;
import br.com.jhohannesfreitas.restaurante.domain.entity.Mesa;
import br.com.jhohannesfreitas.restaurante.domain.entity.Pagamento;
import br.com.jhohannesfreitas.restaurante.domain.entity.Pedido;
import br.com.jhohannesfreitas.restaurante.domain.enums.FormaPagamento;
import br.com.jhohannesfreitas.restaurante.domain.enums.StatusMesa;
import br.com.jhohannesfreitas.restaurante.domain.enums.StatusPagamento;
import br.com.jhohannesfreitas.restaurante.domain.enums.StatusPedido;
import br.com.jhohannesfreitas.restaurante.dto.PagamentoRequest;
import br.com.jhohannesfreitas.restaurante.dto.PagamentoResponse;
import br.com.jhohannesfreitas.restaurante.exception.RegraNegocioException;
import br.com.jhohannesfreitas.restaurante.repository.FechamentoContaRepository;
import br.com.jhohannesfreitas.restaurante.repository.MesaRepository;
import br.com.jhohannesfreitas.restaurante.repository.PagamentoRepository;
import br.com.jhohannesfreitas.restaurante.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PagamentoService {

    private final PagamentoClient pagamentoClient;
    private final FechamentoContaRepository fechamentoContaRepository;
    private final PedidoRepository pedidoRepository;
    private final MesaRepository mesaRepository;
    private final PagamentoRepository pagamentoRepository;

    public PagamentoService(PagamentoClient pagamentoClient, FechamentoContaRepository fechamentoContaRepository, PedidoRepository pedidoRepository, MesaRepository mesaRepository, PagamentoRepository pagamentoRepository) {
        this.pagamentoClient = pagamentoClient;
        this.fechamentoContaRepository = fechamentoContaRepository;
        this.pedidoRepository = pedidoRepository;
        this.mesaRepository = mesaRepository;
        this.pagamentoRepository = pagamentoRepository;
    }

    @Transactional
    public void pagar(Long pedidoId, String formaPagamento){
        FechamentoConta fechamento = fechamentoContaRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RegraNegocioException("Conta não encontrada."));

        PagamentoResponse response = pagamentoClient.processar(
                new PagamentoRequest(
                        fechamento.getTotal(),
                        formaPagamento
                )
        );

        if ("APROVADO".equals(response.status())) {
            Pedido pedido = fechamento.getPedido();
            pedido.setStatus(StatusPedido.FECHADO);

            Mesa mesa = pedido.getMesa();
            mesa.setStatus(StatusMesa.LIVRE);

            Pagamento pagamento = new Pagamento();
            pagamento.setPedido(pedido);
            pagamento.setFormaPagamento(FormaPagamento.valueOf(formaPagamento));
            pagamento.setStatus(StatusPagamento.APROVADO);
            pagamento.setValor(fechamento.getTotal());
            pagamento.setDataPagamento(fechamento.getDataFechamento());

            pedidoRepository.save(pedido);
            mesaRepository.save(mesa);
            pagamentoRepository.save(pagamento);

        }

    }

}
