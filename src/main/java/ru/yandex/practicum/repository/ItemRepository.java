package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.model.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findByTitleContainingIgnoreCase(String search, Pageable pageable);
}
