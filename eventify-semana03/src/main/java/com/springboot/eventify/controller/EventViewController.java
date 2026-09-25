package com.springboot.eventify.controller;

import com.springboot.eventify.exception.InvalidDataException;
import com.springboot.eventify.exception.ResourceNotFoundException;
import com.springboot.eventify.model.Event;
import com.springboot.eventify.service.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/events")
public class EventViewController {

    private final EventService eventService;

    public EventViewController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public String listar(@PageableDefault(size = 10)Pageable pageable, Model model){
        Page<Event> pagina = eventService.findAll(pageable);
        model.addAttribute("eventos", pagina.getContent());
        model.addAttribute("pagina", pagina);
        return "events/list";
    }

    // Buscar por ID
    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findById(id));
        return "events/detail";
    }

    @GetMapping("/new")
    public String mostrarFormulario(Model model){
        model.addAttribute("event", new Event());
        return "events/form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("event") Event event, BindingResult result, RedirectAttributes redirectAttributes){
        // No redirigimos: volvemos a mostrar el mismo formulario con los
        // datos ya escritos por el usuario y los mensajes de error de
        // Bean Validation (@NotBlank, @Size, @NotNull en la entidad Event).
        if(result.hasErrors()){
            return "events/form";
        }
        eventService.save(event);
        redirectAttributes.addFlashAttribute("mensaje", "Evento creado exitosamente");
        return  "redirect:/admin/events";
    }

    // UPDATE — formulario precargado
    @GetMapping("/{id}/edit")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findById(id));
        return "events/form";
    }

    // UPDATE — procesar
    @PostMapping("/{id}/edit")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("event") Event event, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "events/form";
        }
        eventService.update(id, event);
        redirectAttributes.addFlashAttribute("mensaje", "Evento actualizado exitosamente");
        return "redirect:/admin/events";
    }

    // DELETE
    @PostMapping("/{id}/delete")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        eventService.delete(id);
        redirectAttributes.addFlashAttribute("mensaje", "Evento eliminado exitosamente");
        return "redirect:/admin/events";
    }

    // Manejo de errores de negocio propio de la vista: NO se delega al
    // GlobalExceptionHandler de la API (ver Paso 6), porque esa clase
    // responde JSON y aquí el cliente es un navegador esperando HTML.
    @ExceptionHandler(ResourceNotFoundException.class)
    public String manejarNoEncontrado(ResourceNotFoundException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/admin/events";
    }

    @ExceptionHandler(InvalidDataException.class)
    public String manejarDatoInvalido(InvalidDataException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/admin/events";
    }
}
