package com.gradingsystem.util;

import com.gradingsystem.dto.common.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.function.Function;

public final class PageUtils {

    private PageUtils() {}

    public static Pageable toPageable(int page, int size, String sortBy, String direction) {
        // TODO: validate sortBy against an allowed-field whitelist to prevent injection
        Sort sort = direction.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }

    public static <T, R> PageResponse<R> toPageResponse(Page<T> page, Function<T, R> mapper) {
        // TODO: consider adding navigation links (HATEOAS)
        return PageResponse.<R>builder()
            .content(page.getContent().stream().map(mapper).toList())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .last(page.isLast())
            .build();
    }
}
