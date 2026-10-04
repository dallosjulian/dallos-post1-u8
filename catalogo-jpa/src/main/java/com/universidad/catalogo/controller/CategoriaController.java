package com.universidad.catalogo.controller;

import com.universidad.catalogo.model.Categoria;
import com.universidad.catalogo.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping({"", "/"})
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "categorias/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("categoria", new Categoria());
        model.addAttribute("titulo", "Nueva Categoría");
        return "categorias/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("categoria") Categoria categoria,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("titulo", categoria.getId() == null ? "Nueva Categoría" : "Editar Categoría");
            return "categorias/formulario";
        }

        try {
            categoriaService.guardar(categoria);
            redirectAttributes.addFlashAttribute("mensajeExito", "Categoría guardada exitosamente.");
        } catch (IllegalStateException e) {
            result.rejectValue("nombre", "error.categoria", e.getMessage());
            model.addAttribute("titulo", categoria.getId() == null ? "Nueva Categoría" : "Editar Categoría");
            return "categorias/formulario";
        }

        return "redirect:/categorias";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Categoria categoria = categoriaService.buscarPorId(id);
            model.addAttribute("categoria", categoria);
            model.addAttribute("titulo", "Editar Categoría");
            return "categorias/formulario";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/categorias";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String confirmarEliminar(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Categoria categoria = categoriaService.buscarPorId(id);
            model.addAttribute("categoria", categoria);
            return "categorias/confirmar-eliminar";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/categorias";
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoriaService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Categoría eliminada con éxito.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/categorias";
    }
}
