package ru.yandex.practicum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.ItemDto;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.repository.ItemRepository;

@Service
public class ItemsService {

    private final ItemRepository itemRepository;

    public ItemsService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public PageWrapper<ItemDto> getItems(String search, String sort, int pageSize, int pageNumber) {
        Sort.Direction direction = Sort.Direction.ASC;
        Sort sortObj = Sort.by(direction, "title");

        if ("PRICE".equals(sort)) {
            sortObj = Sort.by(direction, "price");
        } else if ("ALPHA".equals(sort)) {
            sortObj = Sort.by(direction, "title");
        }

        Page<Item> itemsPage;
        if (search != null && !search.isEmpty()) {
            itemsPage = itemRepository.findByTitleContainingIgnoreCase(search, PageRequest.of(pageNumber, pageSize, sortObj));
        } else {
            itemsPage = itemRepository.findAll(PageRequest.of(pageNumber, pageSize, sortObj));
        }

        return new PageWrapper<>(itemsPage.map(this::toDto));
    }

    private ItemDto toDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getPrice(),
                item.getImagePath(),
                0
        );
    }

    public Object getItem(Long id) {
        return itemRepository.findById(id);
    }
}
