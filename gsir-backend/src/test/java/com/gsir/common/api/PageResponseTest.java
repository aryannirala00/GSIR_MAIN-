package com.gsir.common.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testPageResponseCalculations() {
        List<String> items = List.of("item1", "item2", "item3", "item4", "item5");

        // Page 0, size 2 -> items 1, 2
        PageResponse<String> page0 = PageResponse.of(items, 0, 2);
        assertEquals(2, page0.items().size());
        assertEquals("item1", page0.items().get(0));
        assertEquals("item2", page0.items().get(1));
        assertEquals(0, page0.page());
        assertEquals(2, page0.size());
        assertEquals(5, page0.totalElements());
        assertEquals(3, page0.totalPages());
        assertTrue(page0.first());
        assertFalse(page0.last());

        // Page 2, size 2 -> item 5
        PageResponse<String> page2 = PageResponse.of(items, 2, 2);
        assertEquals(1, page2.items().size());
        assertEquals("item5", page2.items().get(0));
        assertEquals(2, page2.page());
        assertFalse(page2.first());
        assertTrue(page2.last());
    }

    @Test
    void testEmptyListHandling() {
        PageResponse<String> empty = PageResponse.of(List.of(), 0, 10);
        assertTrue(empty.items().isEmpty());
        assertEquals(0, empty.totalElements());
        assertEquals(0, empty.totalPages());
        assertTrue(empty.first());
        assertTrue(empty.last());
    }

    @Test
    void testSerializationContainsItemsAndContent() throws Exception {
        PageResponse<String> response = PageResponse.of(List.of("alpha", "beta"), 0, 10);
        String json = objectMapper.writeValueAsString(response);

        assertTrue(json.contains("\"items\":[\"alpha\",\"beta\"]"));
        assertTrue(json.contains("\"content\":[\"alpha\",\"beta\"]"));
        assertTrue(json.contains("\"page\":0"));
        assertTrue(json.contains("\"size\":10"));
        assertTrue(json.contains("\"totalElements\":2"));
        assertTrue(json.contains("\"totalPages\":1"));
        assertTrue(json.contains("\"first\":true"));
        assertTrue(json.contains("\"last\":true"));
    }

    @Test
    void testPaginationParamsNormalizationAndSorting() {
        PaginationParams params = new PaginationParams(null, null, "  name ", "desc");

        assertEquals(0, params.getNormalizedPage());
        assertEquals(20, params.getNormalizedSize());
        assertEquals("DESC", params.getNormalizedSortDirection());
        assertEquals("name", params.getNormalizedSortBy("default"));
        assertFalse(params.isAscending());

        List<String> fruits = List.of("banana", "apple", "cherry");
        List<String> sortedDesc = params.applySort(fruits, s -> s);
        assertEquals(List.of("cherry", "banana", "apple"), sortedDesc);

        PaginationParams ascParams = new PaginationParams(2, 50, null, "asc");
        assertEquals(2, ascParams.getNormalizedPage());
        assertEquals(50, ascParams.getNormalizedSize());
        assertTrue(ascParams.isAscending());
        List<String> sortedAsc = ascParams.applySort(fruits, s -> s);
        assertEquals(List.of("apple", "banana", "cherry"), sortedAsc);
    }
}
