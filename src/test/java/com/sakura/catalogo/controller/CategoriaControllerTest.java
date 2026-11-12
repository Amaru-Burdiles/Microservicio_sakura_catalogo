package com.sakura.catalogo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sakura.catalogo.client.SesionGuard;
import com.sakura.catalogo.exception.AccesoDenegadoException;
import com.sakura.catalogo.exception.RecursoNoEncontradoException;
import com.sakura.catalogo.exception.ReglaNegocioException;
import com.sakura.catalogo.model.Categoria;
import com.sakura.catalogo.service.CategoriaService;

@WebMvcTest(CategoriaController.class)
@ActiveProfiles("test")
class CategoriaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CategoriaService categoriaService;

	@MockitoBean
	private SesionGuard sesionGuard;

	private static final String AUTH = "Bearer token-admin";

	@Test
	void listar_Exito() throws Exception {
		when(categoriaService.listar()).thenReturn(List.of(categoria(1L, "Peluquería")));

		mockMvc.perform(get("/api/v1/categorias"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nombre").value("Peluquería"));
	}

	@Test
	void obtener_Exito() throws Exception {
		when(categoriaService.buscar(1L)).thenReturn(categoria(1L, "Peluquería"));

		mockMvc.perform(get("/api/v1/categorias/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1));
	}

	@Test
	void obtener_NoExiste_Responde404() throws Exception {
		when(categoriaService.buscar(99L)).thenThrow(new RecursoNoEncontradoException("No existe la categoría"));

		mockMvc.perform(get("/api/v1/categorias/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensaje").value("No existe la categoría"));
	}

	@Test
	void crear_Exito() throws Exception {
		when(categoriaService.crear(any(Categoria.class))).thenReturn(categoria(2L, "Estética"));

		mockMvc.perform(post("/api/v1/categorias")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Estética\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nombre").value("Estética"));
	}

	@Test
	void crear_CuerpoVacio_Responde400() throws Exception {
		mockMvc.perform(post("/api/v1/categorias")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void crear_NombreDuplicado_Responde409() throws Exception {
		when(categoriaService.crear(any(Categoria.class)))
				.thenThrow(new ReglaNegocioException("Ya existe una categoría con ese nombre"));

		mockMvc.perform(post("/api/v1/categorias")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Peluquería\"}"))
				.andExpect(status().isConflict());
	}

	@Test
	void actualizar_Exito() throws Exception {
		when(categoriaService.actualizar(any(), any(Categoria.class))).thenReturn(categoria(1L, "Color"));

		mockMvc.perform(put("/api/v1/categorias/1")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Color\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Color"));
	}

	@Test
	void eliminar_Exito() throws Exception {
		doNothing().when(categoriaService).eliminar(1L);

		mockMvc.perform(delete("/api/v1/categorias/1").header("Authorization", AUTH))
				.andExpect(status().isNoContent());
	}

	@Test
	void eliminar_SinPermiso_Responde403() throws Exception {
		doThrow(new AccesoDenegadoException("No tiene permiso")).when(sesionGuard).exigirAdmin(any());

		mockMvc.perform(delete("/api/v1/categorias/1").header("Authorization", AUTH))
				.andExpect(status().isForbidden());
	}

	private Categoria categoria(Long id, String nombre) {
		Categoria categoria = new Categoria();
		categoria.setId(id);
		categoria.setNombre(nombre);
		return categoria;
	}
}
