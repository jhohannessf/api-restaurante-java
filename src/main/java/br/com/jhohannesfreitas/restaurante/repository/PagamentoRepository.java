package br.com.jhohannesfreitas.restaurante.repository;

import br.com.jhohannesfreitas.restaurante.domain.entity.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
}
