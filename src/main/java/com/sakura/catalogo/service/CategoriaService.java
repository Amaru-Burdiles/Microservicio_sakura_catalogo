package com.sakura.catalogo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sakura.catalogo.model.Categoria;
import com.sakura.catalogo.repository.CategoriaRepository;
import com.sakura.catalogo.repository.ServicioRepository;
import com.sakura.catalogo.exception.RecursoNoEncontradoException;
import com.sakura.catalogo.exception.ReglaNegocioException;

@Service
public class CategoriaService {

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private ServicioRepository servicioRepository;

	public List<Categoria> listar() {
		return categoriaRepository.findAll();
	}

	public Categoria buscar(Long id) {
		return categoriaRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe la categoría"));
	}

	public Categoria crear(Categoria categoria) {
		if (categoriaRepository.existsByNombre(categoria.getNombre())) {
			throw new ReglaNegocioException("Ya existe una categoría con ese nombre");
		}
		categoria.setId(null);
		return categoriaRepository.save(categoria);
	}

	public Categoria actualizar(Long id, Categoria datos) {
		Categoria actual = buscar(id);
		if (!actual.getNombre().equals(datos.getNombre()) && categoriaRepository.existsByNombre(datos.getNombre())) {
			throw new ReglaNegocioException("Ya existe una categoría con ese nombre");
		}
		actual.setNombre(datos.getNombre());
		return categoriaRepository.save(actual);
	}

	public void eliminar(Long id) {
		buscar(id);
		if (servicioRepository.existsByCategoriaId(id)) {
			throw new ReglaNegocioException("No se puede eliminar una categoría con servicios asociados");
		}
		categoriaRepository.deleteById(id);
	}

}
