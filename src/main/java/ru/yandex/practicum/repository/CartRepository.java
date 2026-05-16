package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart findByItemId(long itemId);
}