package com.universidad.catalogo.service;

import com.universidad.catalogo.model.Categoria;
import com.universidad.catalogo.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));
    }

    @Transactional
    public Categoria guardar(Categoria categoria) {
        if (categoria.getNombre() != null) {
            String nombreLimpio = categoria.getNombre().trim();
            categoria.setNombre(nombreLimpio);
            categoriaRepository.findByNombreIgnoreCase(nombreLimpio)
                    .ifPresent(existente -> {
                        if (categoria.getId() == null || !existente.getId().equals(categoria.getId())) {
                            throw new IllegalStateException("Ya existe una categoría con ese nombre.");
                        }
                    });
        }
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = buscarPorId(id);
        if (categoria.getProductos() != null && !categoria.getProductos().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar la categoría: tiene " + categoria.getProductos().size() + " producto(s) asociado(s).");
        }
        categoriaRepository.deleteById(id);
    }
}
