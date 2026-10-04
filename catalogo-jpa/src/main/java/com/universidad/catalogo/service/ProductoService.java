package com.universidad.catalogo.service;

import com.universidad.catalogo.model.Categoria;
import com.universidad.catalogo.model.Producto;
import com.universidad.catalogo.repository.CategoriaRepository;
import com.universidad.catalogo.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return productoRepository.findAllConCategoria();
    }

    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
    }

    @Transactional
    public Producto guardar(Producto producto, Long categoriaId) {
        if (categoriaId == null) {
            throw new IllegalArgumentException("El ID de la categoría es obligatorio.");
        }
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + categoriaId));
        producto.setCategoria(categoria);
        return productoRepository.save(producto);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new IllegalArgumentException("Producto no encontrado con ID: " + id);
        }
        productoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Producto> listarPorCategoriaConPrecioMayorA(Long categoriaId, BigDecimal precioMinimo) {
        if (categoriaId == null) {
            throw new IllegalArgumentException("El ID de la categoría es requerido para el filtro.");
        }
        BigDecimal minimo = (precioMinimo != null) ? precioMinimo : BigDecimal.ZERO;
        return productoRepository.buscarPorCategoriaConPrecioMayorA(categoriaId, minimo);
    }
}
