package com.sakura.catalogo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sakura.catalogo.client.InventarioCliente;
import com.sakura.catalogo.client.ServicioInsumoPedido;
import com.sakura.catalogo.client.ServicioInsumoVista;
import com.sakura.catalogo.client.SesionGuard;
import com.sakura.catalogo.model.Servicio;
import com.sakura.catalogo.service.ServicioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/servicios")
public class ServicioController {

	@Autowired
	private ServicioService servicioService;

	@Autowired
	private SesionGuard sesionGuard;

	@Autowired
	private InventarioCliente inventarioCliente;

	@GetMapping
	public List<Servicio> listar(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) Long categoriaId,
			@RequestParam(required = false) String orden) {
		if (q == null && categoriaId == null && orden == null) {
			return servicioService.listarPublico();
		}
		return servicioService.buscar(q, categoriaId, orden);
	}

	@GetMapping("/gestion")
	public List<Servicio> listarGestion(
			@RequestHeader(value = "Authorization", required = false) String authorization) {
		sesionGuard.exigirAdmin(authorization);
		return servicioService.listarTodos();
	}

	@GetMapping("/{id}")
	public Servicio obtenerPorId(@PathVariable Long id) {
		return servicioService.buscar(id);
	}

	@PostMapping
	public ResponseEntity<Servicio> crear(
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@Valid @RequestBody Servicio servicio) {
		sesionGuard.exigirAdmin(authorization);
		return new ResponseEntity<>(servicioService.crear(servicio), HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public Servicio actualizar(
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@PathVariable Long id,
			@Valid @RequestBody Servicio servicio) {
		sesionGuard.exigirAdmin(authorization);
		return servicioService.actualizar(id, servicio);
	}

	@PostMapping("/{id}/desactivar")
	public Servicio desactivar(@RequestHeader(value = "Authorization", required = false) String authorization,
			@PathVariable Long id) {
		sesionGuard.exigirAdmin(authorization);
		return servicioService.desactivar(id);
	}

	@PostMapping("/{id}/duplicar")
	public ResponseEntity<Servicio> duplicar(
			@RequestHeader(value = "Authorization", required = false) String authorization, @PathVariable Long id) {
		sesionGuard.exigirAdmin(authorization);
		return new ResponseEntity<>(servicioService.duplicar(id), HttpStatus.CREATED);
	}

	@GetMapping("/{id}/insumos")
	public List<ServicioInsumoVista> insumos(@PathVariable Long id) {
		servicioService.buscar(id);
		return inventarioCliente.listarDeServicio(id);
	}

	@PutMapping("/{id}/insumos")
	public List<ServicioInsumoVista> asociarInsumos(
			@RequestHeader(value = "Authorization", required = false) String authorization,
			@PathVariable Long id, @RequestBody List<ServicioInsumoPedido> pedidos) {
		sesionGuard.exigirAdmin(authorization);
		servicioService.buscar(id);
		return inventarioCliente.asociar(id, pedidos, authorization);
	}

}
