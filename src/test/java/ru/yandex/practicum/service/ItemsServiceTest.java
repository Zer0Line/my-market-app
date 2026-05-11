package ru.yandex.practicum.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.repository.ItemRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ItemsServiceTest {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void testDataLoaded() {
        Boolean tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'ITEMS'",
                Boolean.class);
        System.out.println("Table exists: " + (tableExists != null && tableExists));

        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM items", Long.class);
        System.out.println("Count via JDBC: " + count);

        List<Item> items = itemRepository.findAll();
        System.out.println("Number of items found: " + items.size());
        items.forEach(item -> System.out.println(item.getTitle()));
        assertThat(items).hasSize(10);
    }

}