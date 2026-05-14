package ru.yandex.practicum.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for {@link Order}
 */
public record OrderDto(Long id,
                       List<ItemDto> items,
                       BigDecimal totalSum,
                       boolean newOrder) {
}