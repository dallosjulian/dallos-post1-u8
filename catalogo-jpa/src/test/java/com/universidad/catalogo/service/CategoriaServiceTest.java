package com.universidad.catalogo.service;

import com.universidad.catalogo.model.Categoria;
import com.universidad.catalogo.model.Producto;
import com.universidad.catalogo.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoria1;

    @BeforeEach
    void setUp() {
        categoria1 = new Categoria(1L, "Electrónica", "Artículos de tecnología");
    }

    @Test
    @DisplayName("Debe listar todas las categorías")
    void testListarTodas() {
        Categoria categoria2 = new Categoria(2L, "Hogar", "Artículos para el hogar");
        when(categoriaRepository.findAll()).thenReturn(Arrays.asList(categoria1, categoria2));

        List<Categoria> resultado = categoriaService.listarTodas();

        assertEquals(2, resultado.size());
        verify(categoriaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe buscar categoría por ID exitosamente")
    void testBuscarPorIdExitoso() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria1));

        Categoria resultado = categoriaService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals("Electrónica", resultado.getNombre());
        verify(categoriaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al buscar categoría inexistente")
    void testBuscarPorIdInexistente() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> categoriaService.buscarPorId(99L));
        verify(categoriaRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Debe guardar categoría válida cuando el nombre no existe")
    void testGuardarCategoriaNuevaExitosa() {
        Categoria nueva = new Categoria(null, "Ropa", "Prendas de vestir");
        when(categoriaRepository.findByNombreIgnoreCase("Ropa")).thenReturn(Optional.empty());
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> {
            Categoria c = invocation.getArgument(0);
            c.setId(3L);
            return c;
        });

        Categoria guardada = categoriaService.guardar(nueva);

        assertNotNull(guardada.getId());
        assertEquals("Ropa", guardada.getNombre());
        verify(categoriaRepository, times(1)).save(nueva);
    }

    @Test
    @DisplayName("Debe lanzar excepción al guardar categoría con nombre duplicado")
    void testGuardarCategoriaNombreDuplicado() {
        Categoria existente = new Categoria(1L, "Electrónica", "Descripción previa");
        Categoria duplicada = new Categoria(null, "electrónica", "Otra descripción");

        when(categoriaRepository.findByNombreIgnoreCase("electrónica")).thenReturn(Optional.of(existente));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> categoriaService.guardar(duplicada));
        assertTrue(ex.getMessage().contains("Ya existe una categoría con ese nombre."));
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    @DisplayName("Debe eliminar categoría sin productos asociados")
    void testEliminarCategoriaSinProductos() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria1));

        categoriaService.eliminar(1L);

        verify(categoriaRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Debe impedir eliminar categoría con productos asociados")
    void testEliminarCategoriaConProductosAsociados() {
        Producto producto = new Producto(10L, "Teclado", new BigDecimal("45.00"), 10, categoria1);
        categoria1.addProducto(producto);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria1));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> categoriaService.eliminar(1L));
        assertTrue(ex.getMessage().contains("No se puede eliminar la categoría: tiene 1 producto(s) asociado(s)."));
        verify(categoriaRepository, never()).deleteById(anyLong());
    }
}
