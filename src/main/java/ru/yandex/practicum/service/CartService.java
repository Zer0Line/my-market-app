package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.ItemDto;
import ru.yandex.practicum.mapper.DtoMapper;
import ru.yandex.practicum.model.Cart;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ItemRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CartService {

    private final CartRepository cartRepository;

    private final ItemRepository itemRepository;

    public List<ItemDto> getCartItems() {
        List<Cart> carts = cartRepository.findAll();
        return carts.stream()
                .map(cart -> {
                    Item item = cart.getItem();
                    if (item == null) {
                        return null;
                    }
                    return DtoMapper.toItemDto(item, cart.getCount());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addToCart(Long itemId) {
        Cart cart = cartRepository.findByItemId(itemId);
        if (cart == null) {
            cart = new Cart();
            cart.setItemId(itemId);
            cart.setCount(1);
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

    @Transactional
    public void removeItemFromCart(Long itemId) {
        Cart cart = cartRepository.findByItemId(itemId);
        if (cart != null) {
            cartRepository.delete(cart);
        }
    }

    public BigDecimal calculateTotalPrice(List<ItemDto> cartItems) {
        return cartItems.stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.count())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
