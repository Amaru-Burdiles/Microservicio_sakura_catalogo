package com.sakura.catalogo.service;

import com.sakura.catalogo.exception.RecursoNoEncontradoException;
import com.sakura.catalogo.exception.ReglaNegocioException;
import com.sakura.catalogo.model.Categoria;
import com.sakura.catalogo.repository.CategoriaRepository;
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
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ServicioRepository servicioRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Belleza");
    }

    // --- TEST LISTAR ---

    @Test
    void listar_DebeRetornarListaDeCategorias() {
        when(categoriaRepository.findAll()).thenReturn(List.of(categoria));

        List<Categoria> resultado = categoriaService.listar();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(categoriaRepository, times(1)).findAll();
    }

    // --- TEST BUSCAR ---

    @Test
    void buscar_CuandoExiste_DebeRetornarCategoria() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        Categoria resultado = categoriaService.buscar(1L);

        assertNotNull(resultado);
        assertEquals("Belleza", resultado.getNombre());
    }

    @Test
    void buscar_CuandoNoExiste_DebeLanzarExcepcion() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> categoriaService.buscar(1L));
    }

    // --- TEST CREAR ---

    @Test
    void crear_CuandoEsValido_DebeGuardarYRetornarCategoria() {
        when(categoriaRepository.existsByNombre("Belleza")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        Categoria resultado = categoriaService.crear(categoria);

        assertNotNull(resultado);
        verify(categoriaRepository, times(1)).save(categoria);
    }

    @Test
    void crear_CuandoNombreExiste_DebeLanzarExcepcion() {
        when(categoriaRepository.existsByNombre("Belleza")).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> categoriaService.crear(categoria));
        verify(categoriaRepository, never()).save(any());
    }

    // --- TEST ACTUALIZAR ---

    @Test
    void actualizar_CuandoEsValido_DebeActualizar() {
        Categoria datosNuevos = new Categoria();
        datosNuevos.setNombre("Estética");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombre("Estética")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        Categoria resultado = categoriaService.actualizar(1L, datosNuevos);

        assertNotNull(resultado);
        assertEquals("Estética", resultado.getNombre());
        verify(categoriaRepository).existsByNombre("Estética");
    }

    @Test
    void actualizar_MismoNombre_NoConsultaDuplicados() {
        Categoria datos = new Categoria();
        datos.setNombre("Belleza");
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(i -> i.getArgument(0));

        Categoria resultado = categoriaService.actualizar(1L, datos);

        assertEquals("Belleza", resultado.getNombre());
        verify(categoriaRepository, never()).existsByNombre(any());
    }

    @Test
    void actualizar_CuandoNombreYaExiste_DebeLanzarExcepcion() {
        Categoria datosNuevos = new Categoria();
        datosNuevos.setNombre("Barbería");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombre("Barbería")).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> categoriaService.actualizar(1L, datosNuevos));
        verify(categoriaRepository, never()).save(any());
    }

    // --- TEST ELIMINAR ---

    @Test
    void eliminar_SinServiciosAsociados_DebeEliminar() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(servicioRepository.existsByCategoriaId(1L)).thenReturn(false);

        categoriaService.eliminar(1L);

        verify(categoriaRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_ConServiciosAsociados_DebeLanzarExcepcion() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(servicioRepository.existsByCategoriaId(1L)).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> categoriaService.eliminar(1L));
        verify(categoriaRepository, never()).deleteById(any());
    }
}