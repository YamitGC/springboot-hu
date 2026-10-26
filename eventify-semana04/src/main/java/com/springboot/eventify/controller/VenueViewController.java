package com.springboot.eventify.controller;

import com.springboot.eventify.exception.InvalidDataException;
import com.springboot.eventify.exception.ResourceNotFoundException;
import com.springboot.eventify.model.Venue;
import com.springboot.eventify.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/venues")
public class VenueViewController {

    private final VenueService venueService;

    public VenueViewController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping
    public String listar(@PageableDefault(size = 10) Pageable pageable, Model model){
        Page<Venue> pagina = venueService.findAll(pageable);
        model.addAttribute("lugares", pagina.getContent());
        model.addAttribute("pagina", pagina);
        return "venues/list";
    }

    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        model.addAttribute("venue", venueService.findById(id));
        return "venues/detail";
    }

    @GetMapping("/new")
    public String mostrarFormulario(Model model){
        model.addAttribute("venue", new Venue());
        return "venues/form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("venue") Venue venue, BindingResult result, RedirectAttributes redirectAttributes){
        if(result.hasErrors()){
            return "venues/form";
        }
        venueService.save(venue);
        redirectAttributes.addFlashAttribute("mensaje", "Lugar creado exitosamente");
        return  "redirect:/admin/venues";
    }

    @GetMapping("/{id}/edit")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        model.addAttribute("venue", venueService.findById(id));
        return "venues/form";
    }

    @PostMapping("/{id}/delete")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        venueService.delete(id);
        redirectAttributes.addFlashAttribute("mensaje", "Lugar eliminado exitosamente");
        return "redirect:/admin/venues";
    }

    @PostMapping("/{id}/edit")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("venue") Venue venue,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "venues/form";
        }
        venueService.update(id, venue);
        redirectAttributes.addFlashAttribute("mensaje", "Lugar actualizado exitosamente");
        return "redirect:/admin/venues";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String manejarNoEncontrado(ResourceNotFoundException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/admin/venues";
    }

    @ExceptionHandler(InvalidDataException.class)
    public String manejarDatoInvalido(InvalidDataException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/admin/venues";
    }
}
