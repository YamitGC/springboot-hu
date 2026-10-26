package com.springboot.eventify.service;

import com.springboot.eventify.exception.InvalidDataException;
import com.springboot.eventify.model.Category;
import com.springboot.eventify.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAllByOrderByNombreAsc();
    }

    public Set<Category> findAllByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new InvalidDataException("El evento debe tener al menos una categoría");
        }
        List<Category> encontradas = categoryRepository.findAllById(ids);
        if (encontradas.size() != new HashSet<>(ids).size()) {
            throw new InvalidDataException("Una o más categorías seleccionadas no existen");
        }
        return new HashSet<>(encontradas);
    }
}
