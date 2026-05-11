package ru.yandex.practicum.dto;

import ru.yandex.practicum.model.Item;

/**
 * DTO for {@link Item}
 */
public record ItemDto(long id,
                      String title,
                      String description,
                      long price,
                      String imgPath,
                      Integer count) {
}