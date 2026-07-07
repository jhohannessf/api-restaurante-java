package br.com.jhohannesfreitas.restaurante.repository;

import br.com.jhohannesfreitas.restaurante.domain.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
