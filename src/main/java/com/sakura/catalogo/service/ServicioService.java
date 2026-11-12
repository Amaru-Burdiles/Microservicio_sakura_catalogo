package com.sakura.catalogo.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sakura.catalogo.model.Categoria;
import com.sakura.catalogo.model.Servicio;
import com.sakura.catalogo.repository.ServicioRepository;
import com.sakura.catalogo.exception.RecursoNoEncontradoException;
import com.sakura.catalogo.exception.ReglaNegocioException;

@Service
public class ServicioService {

	@Autowired
	private ServicioRepository servicioRepository;

	@Autowired
	private CategoriaService categoriaService;

	public List<Servicio> listarPublico() {
		return servicioRepository.findByActivoTrueOrderByNombreAsc();
	}

	public List<Servicio> buscar(String q, Long categoriaId, String orden) {
		List<Servicio> lista = new ArrayList<>(listarPublico());
		if (q != null && !q.isBlank()) {
			String texto = q.toLowerCase();
			lista = new ArrayList<>(lista.stream().filter(servicio -> servicio.getNombre().toLowerCase().contains(texto)).toList());
		}
		if (categoriaId != null) {
			lista = new ArrayList<>(lista.stream()
					.filter(servicio -> servicio.getCategoria() != null && categoriaId.equals(servicio.getCategoria().getId()))
					.toList());
		}
		if (orden != null) {
			Comparator<Servicio> comparador = switch (orden) {
				case "precio_asc" -> Comparator.comparing(Servicio::getPrecio);
				case "precio_desc" -> Comparator.comparing(Servicio::getPrecio).reversed();
				case "duracion_asc" -> Comparator.comparing(Servicio::getDuracionMinutos);
				case "duracion_desc" -> Comparator.comparing(Servicio::getDuracionMinutos).reversed();
				default -> null;
			};
			if (comparador != null) {
				lista.sort(comparador);
			}
		}
		return lista;
	}

	public Servicio desactivar(Long id) {
		Servicio servicio = buscar(id);
		servicio.setActivo(false);
		return servicioRepository.save(servicio);
	}

	public Servicio duplicar(Long id) {
		Servicio origen = buscar(id);
		Servicio copia = new Servicio();
		copia.setNombre("Copia de " + origen.getNombre());
		copia.setDescripcion(origen.getDescripcion());
		copia.setPrecio(origen.getPrecio());
		copia.setDuracionMinutos(origen.getDuracionMinutos());
		copia.setCategoria(origen.getCategoria());
		copia.setActivo(true);
		return servicioRepository.save(copia);
	}

	public List<Servicio> listarTodos() {
		return servicioRepository.findAll();
	}

	public Servicio buscar(Long id) {
		return servicioRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el servicio"));
	}

	public Servicio crear(Servicio datos) {
		datos.setId(null);
		datos.setCategoria(resolverCategoria(datos));
		datos.setActivo(true);
		return servicioRepository.save(datos);
	}

	public Servicio actualizar(Long id, Servicio datos) {
		Servicio actual = buscar(id);
		actual.setNombre(datos.getNombre());
		actual.setDescripcion(datos.getDescripcion());
		actual.setPrecio(datos.getPrecio());
		actual.setDuracionMinutos(datos.getDuracionMinutos());
		actual.setCategoria(resolverCategoria(datos));
		actual.setActivo(datos.isActivo());
		return servicioRepository.save(actual);
	}

	private Categoria resolverCategoria(Servicio datos) {
		if (datos.getCategoria() == null || datos.getCategoria().getId() == null) {
			throw new ReglaNegocioException("Debe indicar la categoría");
		}
		return categoriaService.buscar(datos.getCategoria().getId());
	}

}
