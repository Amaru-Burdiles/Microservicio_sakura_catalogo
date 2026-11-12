package com.sakura.catalogo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sakura.catalogo.model.Servicio;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

	List<Servicio> findByActivoTrueOrderByNombreAsc();

	boolean existsByCategoriaId(Long categoriaId);

}
