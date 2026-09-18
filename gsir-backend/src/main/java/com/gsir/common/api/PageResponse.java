package com.gsir.common.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

public record PageResponse<T>(
        @JsonProperty("items") List<T> items,
        @JsonProperty("page") int page,
        @JsonProperty("size") int size,
        @JsonProperty("totalElements") long totalElements,
        @JsonProperty("totalPages") int totalPages,
        @JsonProperty("first") boolean first,
        @JsonProperty("last") boolean last
) {
    public PageResponse {
        items = items != null ? List.copyOf(items) : Collections.emptyList();
    }

    @JsonProperty("content")
    public List<T> content() {
        return items;
    }

    public static <T> PageResponse<T> of(List<T> allItems, int page, int size) {
        if (allItems == null || allItems.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), Math.max(0, page), size <= 0 ? 20 : size, 0, 0, true, true);
        }
        int totalElements = allItems.size();
        int safeSize = size <= 0 ? 20 : size;
        int safePage = Math.max(0, page);
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        int fromIndex = Math.min(safePage * safeSize, totalElements);
        int toIndex = Math.min(fromIndex + safeSize, totalElements);
        List<T> paged = allItems.subList(fromIndex, toIndex);
        boolean first = safePage == 0;
        boolean last = safePage >= totalPages - 1 || totalPages == 0;
        return new PageResponse<>(paged, safePage, safeSize, totalElements, totalPages, first, last);
    }

    public static <T> PageResponse<T> of(List<T> items, int page, int size, long totalElements) {
        int safeSize = size <= 0 ? 20 : size;
        int safePage = Math.max(0, page);
        int totalPages = safeSize > 0 ? (int) Math.ceil((double) totalElements / safeSize) : 0;
        boolean first = safePage <= 0;
        boolean last = safePage >= totalPages - 1 || totalPages == 0;
        return new PageResponse<>(items != null ? items : Collections.emptyList(), safePage, safeSize, totalElements, totalPages, first, last);
    }
}
