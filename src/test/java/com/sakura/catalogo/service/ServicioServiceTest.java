package com.sakura.catalogo.service;

import com.sakura.catalogo.exception.RecursoNoEncontradoException;
import com.sakura.catalogo.exception.ReglaNegocioException;
import com.sakura.catalogo.model.Categoria;
import com.sakura.catalogo.model.Servicio;
import com.sakura.catalogo.repository.ServicioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioServiceTest {

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private ServicioService servicioService;

    private Categoria categoria;
    private Servicio servicio1;
    private Servicio servicio2;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Peluquería");

        servicio1 = new Servicio();
        servicio1.setId(1L);
        servicio1.setNombre("Corte de Cabello");
        servicio1.setDescripcion("Corte básico");
        servicio1.setPrecio(15.00);
        servicio1.setDuracionMinutos(30);
        servicio1.setActivo(true);
        servicio1.setCategoria(categoria);

        servicio2 = new Servicio();
        servicio2.setId(2L);
        servicio2.setNombre("Tinte Completo");
        servicio2.setDescripcion("Tinte de cabello");
        servicio2.setPrecio(45.00);
        servicio2.setDuracionMinutos(90);
        servicio2.setActivo(true);
        servicio2.setCategoria(categoria);
    }

    // --- TEST LISTAR PUBLICO Y LISTAR TODOS ---

    @Test
    void listarPublico_DebeRetornarServiciosActivos() {
        when(servicioRepository.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(servicio1, servicio2));

        List<Servicio> resultado = servicioService.listarPublico();

        assertEquals(2, resultado.size());
        verify(servicioRepository, times(1)).findByActivoTrueOrderByNombreAsc();
    }

    @Test
    void listarTodos_DebeRetornarTodosLosServicios() {
        when(servicioRepository.findAll()).thenReturn(List.of(servicio1, servicio2));

        List<Servicio> resultado = servicioService.listarTodos();

        assertEquals(2, resultado.size());
        verify(servicioRepository, times(1)).findAll();
    }

    // --- TEST BUSCAR POR ID ---

    @Test
    void buscar_PorIdExistente_DebeRetornarServicio() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio1));

        Servicio resultado = servicioService.buscar(1L);

        assertNotNull(resultado);
        assertEquals("Corte de Cabello", resultado.getNombre());
    }

    @Test
    void buscar_PorIdNoExistente_DebeLanzarExcepcion() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicioService.buscar(1L));
    }

    // --- TEST BUSQUEDA CON FILTROS (q, categoriaId, orden) ---

    @Test
    void buscar_ConFiltroTextoYCategoriaYOrdenPrecioDesc_DebeFiltrarYOrdenar() {
        when(servicioRepository.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(servicio1, servicio2));

        // Filtramos por texto "corte", categoría 1 y orden precio descendente
        List<Servicio> resultado = servicioService.buscar("corte", 1L, "precio_desc");

        assertEquals(1, resultado.size());
        assertEquals("Corte de Cabello", resultado.get(0).getNombre());
    }

    @Test
    void buscar_ConOrdenDuracionAsc_DebeOrdenarCorrectamente() {
        when(servicioRepository.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(servicio2, servicio1));

        List<Servicio> resultado = servicioService.buscar(null, null, "duracion_asc");

        assertEquals(2, resultado.size());
        assertEquals(30, resultado.get(0).getDuracionMinutos()); // servicio1 (30 min) va primero que servicio2 (90 min)
    }

    @Test
    void buscar_ConOrdenPrecioAsc_DebeOrdenarDeMenorAMayor() {
        when(servicioRepository.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(servicio2, servicio1));

        List<Servicio> resultado = servicioService.buscar("  ", null, "precio_asc");

        assertEquals(15.00, resultado.get(0).getPrecio(), 0.001);
        assertEquals(45.00, resultado.get(1).getPrecio(), 0.001);
    }

    @Test
    void buscar_ConOrdenDuracionDesc_DebeOrdenarDeMayorAMenor() {
        when(servicioRepository.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(servicio1, servicio2));

        List<Servicio> resultado = servicioService.buscar(null, null, "duracion_desc");

        assertEquals(90, resultado.get(0).getDuracionMinutos());
        assertEquals(30, resultado.get(1).getDuracionMinutos());
    }

    @Test
    void buscar_ConOrdenDesconocido_NoReordena() {
        when(servicioRepository.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(servicio2, servicio1));

        List<Servicio> resultado = servicioService.buscar(null, null, "nombre");

        assertEquals("Tinte Completo", resultado.get(0).getNombre());
        assertEquals("Corte de Cabello", resultado.get(1).getNombre());
    }

    @Test
    void buscar_ServicioSinCategoria_NoCoincideConFiltroDeCategoria() {
        Servicio sinCategoria = new Servicio();
        sinCategoria.setNombre("Sin categoría");
        sinCategoria.setPrecio(10.00);
        sinCategoria.setDuracionMinutos(20);
        when(servicioRepository.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(sinCategoria, servicio1));

        List<Servicio> resultado = servicioService.buscar(null, 1L, null);

        assertEquals(1, resultado.size());
        assertEquals("Corte de Cabello", resultado.get(0).getNombre());
    }

    @Test
    void buscar_CategoriaDistinta_NoIncluyeElServicio() {
        when(servicioRepository.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(servicio1));

        List<Servicio> resultado = servicioService.buscar(null, 99L, null);

        assertTrue(resultado.isEmpty());
    }

    // --- TEST CREAR Y ACTUALIZAR ---

    @Test
    void crear_ConCategoriaValida_DebeGuardarServicio() {
        when(categoriaService.buscar(1L)).thenReturn(categoria);
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicio1);

        Servicio resultado = servicioService.crear(servicio1);

        assertNotNull(resultado);
        assertTrue(resultado.isActivo());
        verify(servicioRepository, times(1)).save(servicio1);
    }

    @Test
    void crear_SinCategoria_DebeLanzarExcepcion() {
        servicio1.setCategoria(null);

        assertThrows(ReglaNegocioException.class, () -> servicioService.crear(servicio1));
        verify(servicioRepository, never()).save(any());
    }

    @Test
    void crear_CategoriaSinId_DebeLanzarExcepcion() {
        Categoria sinId = new Categoria();
        servicio1.setCategoria(sinId);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> servicioService.crear(servicio1));

        assertEquals("Debe indicar la categoría", ex.getMessage());
        verify(categoriaService, never()).buscar(any());
    }

    @Test
    void actualizar_CategoriaSinId_DebeLanzarExcepcion() {
        Servicio datos = new Servicio();
        datos.setCategoria(new Categoria());
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio1));

        assertThrows(ReglaNegocioException.class, () -> servicioService.actualizar(1L, datos));
        verify(servicioRepository, never()).save(any());
    }

    @Test
    void actualizar_ConDatosValidos_DebeActualizarServicio() {
        Servicio datosNuevos = new Servicio();
        datosNuevos.setNombre("Corte Premium");
        datosNuevos.setDescripcion("Corte + Lavado");
        datosNuevos.setPrecio(20.00);
        datosNuevos.setDuracionMinutos(40);
        datosNuevos.setActivo(true);
        datosNuevos.setCategoria(categoria);

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio1));
        when(categoriaService.buscar(1L)).thenReturn(categoria);
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicio1);

        Servicio resultado = servicioService.actualizar(1L, datosNuevos);

        assertNotNull(resultado);
        assertEquals("Corte Premium", resultado.getNombre());
        verify(servicioRepository, times(1)).save(servicio1);
    }

    // --- TEST DESACTIVAR Y DUPLICAR ---

    @Test
    void desactivar_ServicioExistente_DebeCambiarEstadoAFalso() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio1));
        when(servicioRepository.save(any(Servicio.class))).thenAnswer(i -> i.getArgument(0));

        Servicio resultado = servicioService.desactivar(1L);

        assertFalse(resultado.isActivo());
        verify(servicioRepository, times(1)).save(servicio1);
    }

    @Test
    void duplicar_ServicioExistente_DebeCrearCopiaConPrefijo() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio1));
        when(servicioRepository.save(any(Servicio.class))).thenAnswer(i -> i.getArgument(0));

        Servicio copia = servicioService.duplicar(1L);

        assertNotNull(copia);
        assertEquals("Copia de Corte de Cabello", copia.getNombre());
        assertEquals(servicio1.getPrecio(), copia.getPrecio());
        assertTrue(copia.isActivo());
        verify(servicioRepository, times(1)).save(any(Servicio.class));
    }
}