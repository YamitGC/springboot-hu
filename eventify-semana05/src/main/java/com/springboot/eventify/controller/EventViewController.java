package com.springboot.eventify.controller;

import com.springboot.eventify.dto.EventRequest;
import com.springboot.eventify.dto.EventSummaryDTO;
import com.springboot.eventify.exception.InvalidDataException;
import com.springboot.eventify.exception.ResourceNotFoundException;
import com.springboot.eventify.service.CategoryService;
import com.springboot.eventify.service.EventService;
import com.springboot.eventify.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/events")
public class EventViewController {

    private final EventService eventService;
    private final VenueService venueService;
    private final CategoryService categoryService;

    public EventViewController(EventService eventService,
                               VenueService venueService,
                               CategoryService categoryService) {
        this.eventService = eventService;
        this.venueService = venueService;
        this.categoryService = categoryService;
    }

    // ---------- LISTADO con filtros y Slice ----------
    @GetMapping
    public String listar(@RequestParam(required = false) String ciudad,
                         @RequestParam(required = false) String categoria,
                         @RequestParam(required = false) Integer capacidadMin,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                         @PageableDefault(size = 10) Pageable pageable,
                         Model model) {

        Slice<EventSummaryDTO> pagina =
                eventService.search(ciudad, categoria, capacidadMin, desde, hasta, pageable);

        model.addAttribute("eventos", pagina.getContent());
        model.addAttribute("pagina", pagina);

        // Se devuelven los filtros para que el formulario de búsqueda los muestre
        model.addAttribute("ciudad", ciudad);
        model.addAttribute("categoria", categoria);
        model.addAttribute("capacidadMin", capacidadMin);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);

        // URLs de navegación que CONSERVAN los filtros activos
        if (pagina.hasPrevious()) {
            model.addAttribute("urlAnterior", urlPagina(pagina.getNumber() - 1, pagina.getSize(),
                    ciudad, categoria, capacidadMin, desde, hasta));
        }
        if (pagina.hasNext()) {
            model.addAttribute("urlSiguiente", urlPagina(pagina.getNumber() + 1, pagina.getSize(),
                    ciudad, categoria, capacidadMin, desde, hasta));
        }
        return "events/list";
    }

    // ---------- DETALLE ----------
    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findById(id));   // entidad con venue y categorías ya cargados
        return "events/detail";
    }

    // ---------- CREAR ----------
    @GetMapping("/new")
    public String mostrarFormulario(Model model) {
        model.addAttribute("event", new EventRequest());
        cargarCatalogos(model);
        return "events/form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("event") EventRequest event,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            cargarCatalogos(model);          // el <select> y los checkboxes necesitan sus listas otra vez
            return "events/form";
        }
        eventService.save(event);
        redirectAttributes.addFlashAttribute("mensaje", "Evento creado exitosamente");
        redirectAttributes.addFlashAttribute("successMessage", "Evento creado exitosamente");
        return "redirect:/admin/events";
    }

    // ---------- EDITAR ----------
    @GetMapping("/{id}/edit")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        model.addAttribute("event", EventRequest.from(eventService.findById(id)));
        cargarCatalogos(model);
        return "events/form";
    }

    @PostMapping("/{id}/edit")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("event") EventRequest event,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            cargarCatalogos(model);
            return "events/form";
        }
        eventService.update(id, event);
        redirectAttributes.addFlashAttribute("mensaje", "Evento actualizado exitosamente");
        redirectAttributes.addFlashAttribute("successMessage", "Evento actualizado exitosamente");
        return "redirect:/admin/events";
    }

    // ---------- ELIMINAR (borrado lógico) ----------
    @PostMapping("/{id}/delete")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        eventService.softDelete(id);
        redirectAttributes.addFlashAttribute("mensaje", "Evento eliminado exitosamente");
        redirectAttributes.addFlashAttribute("successMessage", "Evento eliminado exitosamente");
        return "redirect:/admin/events";
    }

    // ---------- Manejo de errores de negocio propio de la vista (sin cambios) ----------
    @ExceptionHandler(ResourceNotFoundException.class)
    public String manejarNoEncontrado(ResourceNotFoundException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/admin/events";
    }

    @ExceptionHandler(InvalidDataException.class)
    public String manejarDatoInvalido(InvalidDataException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/admin/events";
    }

    // ---------- Auxiliares ----------
    private void cargarCatalogos(Model model) {
        model.addAttribute("venues", venueService.findAll());
        model.addAttribute("categorias", categoryService.findAll());
    }

    private String urlPagina(int page, int size, String ciudad, String categoria,
                             Integer capacidadMin, LocalDate desde, LocalDate hasta) {
        UriComponentsBuilder b = UriComponentsBuilder.fromPath("/admin/events")
                .queryParam("page", page)
                .queryParam("size", size);
        if (StringUtils.hasText(ciudad))    b.queryParam("ciudad", ciudad.trim());
        if (StringUtils.hasText(categoria)) b.queryParam("categoria", categoria.trim());
        if (capacidadMin != null)           b.queryParam("capacidadMin", capacidadMin);
        if (desde != null)                  b.queryParam("desde", desde);
        if (hasta != null)                  b.queryParam("hasta", hasta);
        return b.build().encode().toUriString();
    }
}
