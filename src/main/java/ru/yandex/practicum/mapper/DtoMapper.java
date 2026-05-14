package ru.yandex.practicum.mapper;

import ru.yandex.practicum.dto.ItemDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.model.Cart;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.model.Order;

import java.util.List;
import java.util.stream.Collectors;

public class DtoMapper {

    public static ItemDto toItemDto(Item item, int count) {
        return new ItemDto(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getPrice(),
                item.getImagePath(),
                count
        );
    }

    public static OrderDto toOrderDto(Order order) {
        List<ItemDto> itemDtos = order.getOrderItems().stream()
                .map(orderItem -> toItemDto(orderItem.getItem(), orderItem.getCount()))
                .collect(Collectors.toList());
        return new OrderDto(
                order.getId(),
                itemDtos,
                order.getTotalSum(),
                order.isNewOrder()
        );
    }

    public static ItemDto toItemWitCartCaountDto(Cart cart, Item item) {
        int count = (cart != null) ? cart.getCount() : 0;
        return new ItemDto(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getPrice(),
                item.getImagePath(),
                count
        );
    }
}