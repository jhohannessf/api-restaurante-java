package br.com.jhohannesfreitas.restaurante.repository;

import br.com.jhohannesfreitas.restaurante.domain.entity.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MesaRepository extends JpaRepository<Mesa, Long> {
}
