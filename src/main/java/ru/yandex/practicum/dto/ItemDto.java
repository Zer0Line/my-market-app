package ru.yandex.practicum.dto;

import ru.yandex.practicum.model.Item;

import java.math.BigDecimal;

/**
 * DTO for {@link Item}
 */
public record ItemDto(long id,
                      String title,
                      String description,
                      BigDecimal price,
                      String imgPath,
                      Integer count) {
}