package br.com.jhohannesfreitas.restaurante.worker;

import br.com.jhohannesfreitas.restaurante.domain.entity.PedidoItem;
import br.com.jhohannesfreitas.restaurante.domain.enums.StatusItemPedido;
import br.com.jhohannesfreitas.restaurante.repository.PedidoItemRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class CozinhaWorker {

    private final PedidoItemRepository pedidoItemRepository;

    // Para uso da virtual threads
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    public CozinhaWorker(PedidoItemRepository pedidoItemRepository) {
        this.pedidoItemRepository = pedidoItemRepository;
    }

    @Scheduled(fixedRate = 60000)
    public void verificarItensAtrasados() {
        List<PedidoItem> itensEmPreparo = pedidoItemRepository.buscarItensComProdutoEPedido(StatusItemPedido.EM_PREPARO);

        for (PedidoItem pedidoItem : itensEmPreparo) {
            executorService.submit(() -> verificarItem(pedidoItem));
        }
    }

    private void verificarItem(PedidoItem item) {
        if (item.getDataInicioPreparo() == null){
            return; // encerra a execução do método imediatamente
        }
        Integer tempoPreparo = item.getProduto().getTempoPreparoMinutos();
        if (tempoPreparo == null || tempoPreparo <= 0){
            return;
        }
        Long minutosEmPreparo = Duration.between(item.getDataInicioPreparo(), LocalDateTime.now()).toMinutes();
        if (minutosEmPreparo > tempoPreparo){
            System.out.println(
                    """
                    [ALERTA COZINHA]
                    Item atrasado:
                    Pedido: %d
                    Mesa: %d
                    Produto: %s
                    Tempo esperado: %d minutos
                    Tempo em preparo: %d minutos
                    """.formatted(
                            item.getPedido().getId(),
                            item.getPedido().getMesa().getNumero(),
                            item.getProduto().getNome(),
                            tempoPreparo,
                            minutosEmPreparo
                    ));
        }
    }




}
