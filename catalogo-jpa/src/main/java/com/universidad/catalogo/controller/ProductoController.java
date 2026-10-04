package com.universidad.catalogo.controller;

import com.universidad.catalogo.model.Categoria;
import com.universidad.catalogo.model.Producto;
import com.universidad.catalogo.service.CategoriaService;
import com.universidad.catalogo.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    public ProductoController(ProductoService productoService, CategoriaService categoriaService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    @GetMapping({"", "/"})
    public String listar(Model model) {
        model.addAttribute("productos", productoService.listarTodos());
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "productos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("titulo", "Nuevo Producto");
        return "productos/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Producto producto = productoService.buscarPorId(id);
            model.addAttribute("producto", producto);
            model.addAttribute("categoriaSeleccionadaId", producto.getCategoria() != null ? producto.getCategoria().getId() : null);
            model.addAttribute("categorias", categoriaService.listarTodas());
            model.addAttribute("titulo", "Editar Producto");
            return "productos/formulario";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/productos";
        }
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("producto") Producto producto,
                          BindingResult result,
                          @RequestParam(value = "categoriaId", required = false) Long categoriaId,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (categoriaId == null) {
            model.addAttribute("errorCategoria", "Debe seleccionar una categoría válida.");
        }

        if (result.hasErrors() || categoriaId == null) {
            model.addAttribute("categorias", categoriaService.listarTodas());
            model.addAttribute("categoriaSeleccionadaId", categoriaId);
            model.addAttribute("titulo", producto.getId() == null ? "Nuevo Producto" : "Editar Producto");
            return "productos/formulario";
        }

        try {
            productoService.guardar(producto, categoriaId);
            redirectAttributes.addFlashAttribute("mensajeExito", "Producto guardado exitosamente.");
        } catch (Exception e) {
            model.addAttribute("mensajeError", e.getMessage());
            model.addAttribute("categorias", categoriaService.listarTodas());
            model.addAttribute("categoriaSeleccionadaId", categoriaId);
            model.addAttribute("titulo", producto.getId() == null ? "Nuevo Producto" : "Editar Producto");
            return "productos/formulario";
        }

        return "redirect:/productos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            productoService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Producto eliminado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/productos";
    }

    @GetMapping("/categoria/{categoriaId}/precio-mayor")
    public String filtrarPorPrecioMayor(@PathVariable("categoriaId") Long categoriaId,
                                        @RequestParam("minimo") BigDecimal minimo,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        try {
            List<Producto> productos = productoService.listarPorCategoriaConPrecioMayorA(categoriaId, minimo);
            Categoria categoria = categoriaService.buscarPorId(categoriaId);
            model.addAttribute("productos", productos);
            model.addAttribute("categoria", categoria);
            model.addAttribute("precioMinimo", minimo);
            return "productos/filtrados";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/productos";
        }
    }
}
