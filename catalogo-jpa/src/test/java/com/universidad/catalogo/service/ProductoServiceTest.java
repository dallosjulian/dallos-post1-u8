package com.universidad.catalogo.service;

import com.universidad.catalogo.model.Categoria;
import com.universidad.catalogo.model.Producto;
import com.universidad.catalogo.repository.CategoriaRepository;
import com.universidad.catalogo.repository.ProductoRepository;
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
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService productoService;

    private Categoria categoria;
    private Producto producto1;
    private Producto producto2;

    @BeforeEach
    void setUp() {
        categoria = new Categoria(1L, "Tecnología", "Dispositivos electrónicos");
        producto1 = new Producto(1L, "Laptop", new BigDecimal("1200.00"), 5, categoria);
        producto2 = new Producto(2L, "Mouse", new BigDecimal("25.00"), 50, categoria);
    }

    @Test
    @DisplayName("Debe listar todos los productos con JOIN FETCH de categoría")
    void testListarTodos() {
        when(productoRepository.findAllConCategoria()).thenReturn(Arrays.asList(producto1, producto2));

        List<Producto> resultado = productoService.listarTodos();

        assertEquals(2, resultado.size());
        verify(productoRepository, times(1)).findAllConCategoria();
    }

    @Test
    @DisplayName("Debe buscar producto por ID")
    void testBuscarPorId() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));

        Producto resultado = productoService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals("Laptop", resultado.getNombre());
        verify(productoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe guardar producto asignando la categoría correspondiente")
    void testGuardarProductoExitoso() {
        Producto nuevo = new Producto(null, "Teclado Mecánico", new BigDecimal("80.00"), 20, null);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto p = invocation.getArgument(0);
            p.setId(3L);
            return p;
        });

        Producto guardado = productoService.guardar(nuevo, 1L);

        assertNotNull(guardado.getId());
        assertEquals("Teclado Mecánico", guardado.getNombre());
        assertEquals(categoria, guardado.getCategoria());
        verify(categoriaRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).save(nuevo);
    }

    @Test
    @DisplayName("Debe fallar al guardar producto si la categoría no existe")
    void testGuardarProductoCategoriaInexistente() {
        Producto nuevo = new Producto(null, "Monitor", new BigDecimal("300.00"), 10, null);
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> productoService.guardar(nuevo, 99L));
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe listar productos por categoría con precio mayor a un monto")
    void testListarPorCategoriaConPrecioMayorA() {
        when(productoRepository.buscarPorCategoriaConPrecioMayorA(1L, new BigDecimal("100.00")))
                .thenReturn(List.of(producto1));

        List<Producto> filtrados = productoService.listarPorCategoriaConPrecioMayorA(1L, new BigDecimal("100.00"));

        assertEquals(1, filtrados.size());
        assertEquals("Laptop", filtrados.get(0).getNombre());
        verify(productoRepository, times(1)).buscarPorCategoriaConPrecioMayorA(1L, new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Debe eliminar producto por ID")
    void testEliminarProducto() {
        when(productoRepository.existsById(1L)).thenReturn(true);

        productoService.eliminar(1L);

        verify(productoRepository, times(1)).deleteById(1L);
    }
}
