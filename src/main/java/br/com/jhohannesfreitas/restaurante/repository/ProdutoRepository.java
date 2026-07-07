package br.com.jhohannesfreitas.restaurante.repository;

import br.com.jhohannesfreitas.restaurante.domain.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}
