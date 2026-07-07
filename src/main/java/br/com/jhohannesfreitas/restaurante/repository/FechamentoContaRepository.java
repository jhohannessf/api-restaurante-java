package br.com.jhohannesfreitas.restaurante.repository;

import br.com.jhohannesfreitas.restaurante.domain.entity.FechamentoConta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FechamentoContaRepository extends JpaRepository<FechamentoConta, Long> {

    boolean existsByPedidoId(long pedidoId);
    
    Optional<FechamentoConta> findByPedidoId(long pedidoId);
}
