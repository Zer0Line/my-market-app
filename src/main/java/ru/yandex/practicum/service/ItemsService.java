package ru.yandex.practicum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.api.PageWrapper;
import ru.yandex.practicum.api.SortType;
import ru.yandex.practicum.api.dto.ItemDto;
import ru.yandex.practicum.mapper.DtoMapper;
import ru.yandex.practicum.model.Cart;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ItemRepository;

@Service
public class ItemsService {

    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;

    public ItemsService(ItemRepository itemRepository, CartRepository cartRepository) {
        this.itemRepository = itemRepository;
        this.cartRepository = cartRepository;
    }

    public PageWrapper<ItemDto> getItems(String search, SortType sort, int pageSize, int pageNumber) {
        Sort.Direction direction = Sort.Direction.ASC;

        Sort sortType = switch (sort) {
            case PRICE -> Sort.by(direction, "price");
            case ALPHA -> Sort.by(direction, "title");
            case null, default -> Sort.by(direction, "id");
        };

        Page<Item> itemsPage;
        if (search != null && !search.isEmpty()) {
            itemsPage = itemRepository.findByTitleContainingIgnoreCase(search, PageRequest.of(pageNumber, pageSize, sortType));
        } else {
            itemsPage = itemRepository.findAll(PageRequest.of(pageNumber, pageSize, sortType));
        }

        return new PageWrapper<>(itemsPage.map(item -> {
            Cart cart = cartRepository.findByItemId(item.getId());
            return DtoMapper.toItemWitCartCountDto(cart, item);
        }));
    }

    public ItemDto getItem(Long id) {
         return itemRepository.findById(id)
                 .map(item -> {
                     Cart cart = cartRepository.findByItemId(item.getId());
                     return DtoMapper.toItemWitCartCountDto(cart, item);
                 })
                 .orElse(null);
    }
}
