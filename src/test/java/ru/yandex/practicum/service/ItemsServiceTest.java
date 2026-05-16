package ru.yandex.practicum.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.api.PageWrapper;
import ru.yandex.practicum.api.SortType;
import ru.yandex.practicum.api.dto.ItemDto;
import ru.yandex.practicum.model.Cart;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ItemRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItemsServiceTest extends BaseServiceTest {

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private CartRepository cartRepository;

    @Autowired
    private ItemsService itemsService;

    @Test
    void getItems_whenCalled_returnsPageOfItemDtos() {
        // Arrange
        Item item = new Item(1L, "Test Item", "Description", new BigDecimal("100.0"), "image.jpg");
        Cart cart = new Cart();
        cart.setItemId(1L);
        cart.setItem(item);
        cart.setCount(2);
        Page<Item> itemPage = new PageImpl<>(List.of(item));
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(itemPage);
        when(cartRepository.findByItemId(1L)).thenReturn(cart);

        // Act
        PageWrapper<ItemDto> result = itemsService.getItems(null, SortType.NO, 10, 0);

        // Assert
        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        ItemDto dto = result.getContent().get(0);
        assertEquals(1L, dto.id());
        assertEquals("Test Item", dto.title());
        assertEquals(2, dto.count());
        verify(itemRepository).findAll(any(Pageable.class));
        verify(cartRepository).findByItemId(1L);
    }

    @Test
    void getItems_whenSearchProvided_returnsFilteredItems() {
        // Arrange
        Item item = new Item(1L, "Test Item", "Description", new BigDecimal("100.0"), "image.jpg");
        Page<Item> itemPage = new PageImpl<>(List.of(item));
        when(itemRepository.findByTitleContainingIgnoreCase(eq("test"), any(Pageable.class))).thenReturn(itemPage);
        Cart cart = new Cart();
        cart.setItemId(1L);
        cart.setItem(item);
        cart.setCount(1);
        when(cartRepository.findByItemId(1L)).thenReturn(cart);

        // Act
        PageWrapper<ItemDto> result = itemsService.getItems("test", SortType.NO, 10, 0);

        // Assert
        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        ItemDto dto = result.getContent().get(0);
        assertEquals("Test Item", dto.title());
        // Fix: Verify with specific argument for search term, any() for Pageable
        verify(itemRepository).findByTitleContainingIgnoreCase(eq("test"), any(Pageable.class));
        verify(cartRepository).findByItemId(1L);
    }

    @Test
    void getItem_whenItemExists_returnsItemDto() {
        // Arrange
        Item item = new Item(1L, "Test Item", "Description", new BigDecimal("100.0"), "image.jpg");
        Cart cart = new Cart();
        cart.setItemId(1L);
        cart.setItem(item);
        cart.setCount(3);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(cartRepository.findByItemId(1L)).thenReturn(cart);

        // Act
        ItemDto result = itemsService.getItem(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Test Item", result.title());
        assertEquals(3, result.count());
        verify(itemRepository).findById(1L);
        verify(cartRepository).findByItemId(1L);
    }

    @Test
    void getItem_whenItemDoesNotExist_returnsNull() {
        // Arrange
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        ItemDto result = itemsService.getItem(1L);

        // Assert
        assertNull(result);
        verify(itemRepository).findById(1L);
        verify(cartRepository, never()).findByItemId(anyLong());
    }

    @Test
    void getItems_sortByPrice_appliesCorrectSort() {
        // Arrange
        Item item = new Item(1L, "Test Item", "Description", new BigDecimal("100.0"), "image.jpg");
        Page<Item> itemPage = new PageImpl<>(List.of(item));
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(itemPage);
        Cart cart = new Cart();
        cart.setItemId(1L);
        cart.setItem(item);
        cart.setCount(1);
        when(cartRepository.findByItemId(1L)).thenReturn(cart);

        // Act
        itemsService.getItems(null, SortType.PRICE, 10, 0);

        // Assert
        verify(itemRepository).findAll(argThat((Pageable pageable) -> {
            PageRequest pr = (PageRequest) pageable;
            Sort sort = pr.getSort();
            return sort != null && sort.isSorted() && sort.stream().findFirst()
                    .map(order -> order.getProperty().equals("price") && order.getDirection().isAscending())
                    .orElse(false);
        }));
    }

    @Test
    void getItems_sortByAlpha_appliesCorrectSort() {
        // Arrange
        Item item = new Item(1L, "Test Item", "Description", new BigDecimal("100.0"), "image.jpg");
        Page<Item> itemPage = new PageImpl<>(List.of(item));
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(itemPage);
        Cart cart = new Cart();
        cart.setItemId(1L);
        cart.setItem(item);
        cart.setCount(1);
        when(cartRepository.findByItemId(1L)).thenReturn(cart);

        // Act
        itemsService.getItems(null, SortType.ALPHA, 10, 0);

        // Assert
        verify(itemRepository).findAll(argThat((Pageable pageable) -> {
            PageRequest pr = (PageRequest) pageable;
            Sort sort = pr.getSort();
            return sort != null && sort.isSorted() && sort.stream().findFirst()
                    .map(order -> order.getProperty().equals("title") && order.getDirection().isAscending())
                    .orElse(false);
        }));
    }

}