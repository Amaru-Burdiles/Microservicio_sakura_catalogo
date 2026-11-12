package com.sakura.catalogo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
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

import com.sakura.catalogo.client.InventarioCliente;
import com.sakura.catalogo.client.ServicioInsumoVista;
import com.sakura.catalogo.client.SesionGuard;
import com.sakura.catalogo.exception.NoAutenticadoException;
import com.sakura.catalogo.exception.RecursoNoEncontradoException;
import com.sakura.catalogo.model.Servicio;
import com.sakura.catalogo.service.ServicioService;

@WebMvcTest(ServicioController.class)
@ActiveProfiles("test")
class ServicioControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ServicioService servicioService;

	@MockitoBean
	private SesionGuard sesionGuard;

	@MockitoBean
	private InventarioCliente inventarioCliente;

	private static final String AUTH = "Bearer token-admin";
	private static final String CUERPO = "{\"nombre\":\"Corte\",\"precio\":15.0,\"duracionMinutos\":30,\"activo\":true,\"categoria\":{\"nombre\":\"Peluquería\"}}";

	@Test
	void listarPublico_Exito() throws Exception {
		when(servicioService.listarPublico()).thenReturn(List.of(servicio(1L)));

		mockMvc.perform(get("/api/v1/servicios"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nombre").value("Corte"));
	}

	@Test
	void listarConFiltro_Exito() throws Exception {
		when(servicioService.buscar("cor", 1L, "precio_asc")).thenReturn(List.of(servicio(1L)));

		mockMvc.perform(get("/api/v1/servicios").param("q", "cor").param("categoriaId", "1").param("orden", "precio_asc"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1));
	}

	@Test
	void gestion_Exito() throws Exception {
		when(servicioService.listarTodos()).thenReturn(List.of(servicio(1L)));

		mockMvc.perform(get("/api/v1/servicios/gestion").header("Authorization", AUTH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nombre").value("Corte"));
	}

	@Test
	void gestion_SinSesion_Responde401() throws Exception {
		doThrow(new NoAutenticadoException("Debe iniciar sesión")).when(sesionGuard).exigirAdmin(any());

		mockMvc.perform(get("/api/v1/servicios/gestion"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void obtener_Exito() throws Exception {
		when(servicioService.buscar(1L)).thenReturn(servicio(1L));

		mockMvc.perform(get("/api/v1/servicios/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.duracionMinutos").value(30));
	}

	@Test
	void obtener_NoExiste_Responde404() throws Exception {
		when(servicioService.buscar(99L)).thenThrow(new RecursoNoEncontradoException("No existe el servicio"));

		mockMvc.perform(get("/api/v1/servicios/99"))
				.andExpect(status().isNotFound());
	}

	@Test
	void crear_Exito() throws Exception {
		when(servicioService.crear(any(Servicio.class))).thenReturn(servicio(2L));

		mockMvc.perform(post("/api/v1/servicios")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(CUERPO))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(2));
	}

	@Test
	void crear_CuerpoVacio_Responde400() throws Exception {
		mockMvc.perform(post("/api/v1/servicios")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void actualizar_Exito() throws Exception {
		when(servicioService.actualizar(eq(1L), any(Servicio.class))).thenReturn(servicio(1L));

		mockMvc.perform(put("/api/v1/servicios/1")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(CUERPO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Corte"));
	}

	@Test
	void desactivar_Exito() throws Exception {
		Servicio inactivo = servicio(1L);
		inactivo.setActivo(false);
		when(servicioService.desactivar(1L)).thenReturn(inactivo);

		mockMvc.perform(post("/api/v1/servicios/1/desactivar").header("Authorization", AUTH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.activo").value(false));
	}

	@Test
	void duplicar_Exito() throws Exception {
		when(servicioService.duplicar(1L)).thenReturn(servicio(3L));

		mockMvc.perform(post("/api/v1/servicios/1/duplicar").header("Authorization", AUTH))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(3));
	}

	@Test
	void insumos_Exito() throws Exception {
		when(servicioService.buscar(1L)).thenReturn(servicio(1L));
		ServicioInsumoVista vista = new ServicioInsumoVista();
		vista.setCantidad(2);
		when(inventarioCliente.listarDeServicio(1L)).thenReturn(List.of(vista));

		mockMvc.perform(get("/api/v1/servicios/1/insumos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].cantidad").value(2));
	}

	@Test
	void asociarInsumos_Exito() throws Exception {
		when(servicioService.buscar(1L)).thenReturn(servicio(1L));
		ServicioInsumoVista vista = new ServicioInsumoVista();
		vista.setCantidad(1);
		when(inventarioCliente.asociar(eq(1L), any(), eq(AUTH))).thenReturn(List.of(vista));

		mockMvc.perform(put("/api/v1/servicios/1/insumos")
				.header("Authorization", AUTH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("[{\"insumoId\":4,\"cantidad\":1}]"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].cantidad").value(1));
	}

	private Servicio servicio(Long id) {
		Servicio servicio = new Servicio();
		servicio.setId(id);
		servicio.setNombre("Corte");
		servicio.setPrecio(15);
		servicio.setDuracionMinutos(30);
		servicio.setActivo(true);
		return servicio;
	}
}
