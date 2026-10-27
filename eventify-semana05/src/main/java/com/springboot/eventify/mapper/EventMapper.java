package com.springboot.eventify.mapper;

import com.springboot.eventify.dto.EventCreateDTO;
import com.springboot.eventify.dto.EventResponseDTO;
import com.springboot.eventify.model.Category;
import com.springboot.eventify.model.Event;
import com.springboot.eventify.model.Venue;
import com.springboot.eventify.service.CategoryService;
import com.springboot.eventify.service.VenueService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public abstract class EventMapper {

    @Autowired
    protected VenueService venueService;

    @Autowired
    protected CategoryService categoryService;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", constant = "true")
    @Mapping(target = "venue", source = "venueId", qualifiedByName = "mapVenueIdToVenue")
    @Mapping(target = "categories", source = "categoryIds", qualifiedByName = "mapCategoryIdsToCategories")
    public abstract Event toEntity(EventCreateDTO dto);

    @Mapping(source = "venue.nombre", target = "venueName")
    @Mapping(source = "categories", target = "categoryNames", qualifiedByName = "mapCategoriesToNames")
    public abstract EventResponseDTO toResponseDTO(Event event);

    public abstract List<EventResponseDTO> toResponseDTOList(List<Event> events);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "venue", source = "venueId", qualifiedByName = "mapVenueIdToVenue")
    @Mapping(target = "categories", source = "categoryIds", qualifiedByName = "mapCategoryIdsToCategories")
    public abstract void updateEntityFromDTO(EventCreateDTO dto, @MappingTarget Event event);

    @Named("mapVenueIdToVenue")
    protected Venue mapVenueIdToVenue(Long venueId) {
        if (venueId == null) {
            return null;
        }
        return venueService.findById(venueId);
    }

    @Named("mapCategoryIdsToCategories")
    protected Set<Category> mapCategoryIdsToCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Collections.emptySet();
        }
        return categoryService.findAllByIds(categoryIds);
    }

    @Named("mapCategoriesToNames")
    protected List<String> mapCategoriesToNames(Set<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return Collections.emptyList();
        }
        return categories.stream()
                .map(Category::getNombre)
                .sorted()
                .toList();
    }
}

