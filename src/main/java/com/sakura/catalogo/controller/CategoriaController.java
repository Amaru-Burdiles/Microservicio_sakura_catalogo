package com.sakura.catalogo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sakura.catalogo.client.SesionGuard;
import com.sakura.catalogo.model.Categoria;
import com.sakura.catalogo.service.CategoriaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaController {

	@Autowired
	private CategoriaService categoriaService;

	@Autowired
	private SesionGuard sesionGuard;

	@GetMapping
	public List<Categoria> listar() {
		return categoriaService.listar();
	}

	@GetMapping("/{id}")
	public Categoria obtenerPorId(@PathVariable Long id) {
		return categoriaService.buscar(id);
	}

	@PostMapping
	public ResponseEntity<Categoria> crear(
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@Valid @RequestBody Categoria categoria) {
		sesionGuard.exigirAdmin(authorization);
		return new ResponseEntity<>(categoriaService.crear(categoria), HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public Categoria actualizar(
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@PathVariable Long id,
			@Valid @RequestBody Categoria categoria) {
		sesionGuard.exigirAdmin(authorization);
		return categoriaService.actualizar(id, categoria);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@PathVariable Long id) {
		sesionGuard.exigirAdmin(authorization);
		categoriaService.eliminar(id);
		return ResponseEntity.noContent().build();
	}

}
