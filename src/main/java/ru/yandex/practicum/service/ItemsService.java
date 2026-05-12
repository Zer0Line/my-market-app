package ru.yandex.practicum.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.api.SortType;
import ru.yandex.practicum.dto.ItemDto;
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
        Sort sortObj = Sort.by(direction, "title");

        if (SortType.PRICE.equals(sort)) {
            sortObj = Sort.by(direction, "price");
        } else if (SortType.ALPHA.equals(sort)) {
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

    public ItemDto getItem(Long id) {
         return itemRepository.findById(id).map(this::toDto).orElse(null);
    }

    public List<ItemDto> getCartItems() {
        List<Cart> carts = cartRepository.findAll();
        return carts.stream()
                .map(cart -> {
                    Item item = cart.getItem();
                    if (item == null) {
                        // This shouldn't happen if foreign key constraints are enforced
                        return null;
                    }
                    return toDtoWithCount(item, cart.getCount());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private ItemDto toDtoWithCount(Item item, int count) {
        return new ItemDto(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getPrice(),
                item.getImagePath(),
                count
        );
    }

    @Transactional
    public void addToCart(Long itemId) {
        Cart cart = cartRepository.findByItemId(itemId);
        if (cart == null) {
            cart = new Cart();
            cart.setItemId(itemId);
            cart.setCount(1);
            // We need to fetch and set the item to avoid transient instance issues
            Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item not found"));
            cart.setItem(item);
        } else {
            cart.setCount(cart.getCount() + 1);
        }
        cartRepository.save(cart);
    }

    @Transactional
    public void removeFromCart(Long itemId) {
        Cart cart = cartRepository.findByItemId(itemId);
        if (cart != null) {
            int newCount = cart.getCount() - 1;
            if (newCount <= 0) {
                cartRepository.delete(cart);
            } else {
                cart.setCount(newCount);
                cartRepository.save(cart);
            }
        }
    }

    private ItemDto toDto(Item item) {
        Cart cart = cartRepository.findByItemId(item.getId());
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
