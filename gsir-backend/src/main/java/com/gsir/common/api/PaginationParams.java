package com.gsir.common.api;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public record PaginationParams(
        Integer page,
        Integer size,
        String sortBy,
        String sortDirection
) {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;
    public static final String DEFAULT_SORT_DIRECTION = "ASC";

    public int getNormalizedPage() {
        return (page == null || page < 0) ? DEFAULT_PAGE : page;
    }

    public int getNormalizedSize() {
        if (size == null || size <= 0) return DEFAULT_SIZE;
        return Math.min(size, MAX_SIZE);
    }

    public String getNormalizedSortDirection() {
        if (sortDirection == null || (!sortDirection.equalsIgnoreCase("ASC") && !sortDirection.equalsIgnoreCase("DESC"))) {
            return DEFAULT_SORT_DIRECTION;
        }
        return sortDirection.toUpperCase();
    }

    public String getNormalizedSortBy(String defaultSortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return defaultSortBy;
        }
        return sortBy.trim();
    }

    public boolean isAscending() {
        return "ASC".equalsIgnoreCase(getNormalizedSortDirection());
    }

    public <T, U extends Comparable<? super U>> List<T> applySort(List<T> items, Function<T, U> keyExtractor) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        Comparator<T> comparator = Comparator.comparing(keyExtractor);
        if (!isAscending()) {
            comparator = comparator.reversed();
        }
        return items.stream().sorted(comparator).toList();
    }
}
