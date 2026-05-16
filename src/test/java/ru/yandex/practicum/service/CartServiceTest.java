package ru.yandex.practicum.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.api.dto.ItemDto;
import ru.yandex.practicum.model.Cart;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ItemRepository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CartServiceTest extends BaseServiceTest {

    @MockitoBean
    private CartRepository cartRepository;

    @MockitoBean
    private ItemRepository itemRepository;

    @Autowired
    private CartService cartService;

    @Test
    void getCartItems_whenCartHasItems_returnsListOfItemDto() {
        // Arrange
        Item item = new Item(1L, "Test Item", "Description", new BigDecimal("100.0"), "image.jpg");
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setItemId(1L);
        cart.setItem(item);
        cart.setCount(3);
        when(cartRepository.findAll()).thenReturn(List.of(cart));

        // Act
        List<ItemDto> result = cartService.getCartItems();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        ItemDto dto = result.get(0);
        assertEquals(1L, dto.id());
        assertEquals("Test Item", dto.title());
        assertEquals(3, dto.count());
        assertEquals(new BigDecimal("100.0"), dto.price());
        verify(cartRepository).findAll();
    }

    @Test
    void getCartItems_whenCartIsEmpty_returnsEmptyList() {
        // Arrange
        when(cartRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<ItemDto> result = cartService.getCartItems();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cartRepository).findAll();
    }

    @Test
    void getCartItems_whenCartItemHasNullItem_filtersItOut() {
        // Arrange
        Cart cartWithNullItem = new Cart();
        cartWithNullItem.setId(1L);
        cartWithNullItem.setItemId(1L);
        cartWithNullItem.setItem(null);
        cartWithNullItem.setCount(2);

        Item item = new Item(2L, "Valid Item", "Description", new BigDecimal("50.0"), "image.jpg");
        Cart validCart = new Cart();
        validCart.setId(2L);
        validCart.setItemId(2L);
        validCart.setItem(item);
        validCart.setCount(1);

        when(cartRepository.findAll()).thenReturn(List.of(cartWithNullItem, validCart));

        // Act
        List<ItemDto> result = cartService.getCartItems();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).id());
    }

    @Test
    void addToCart_whenItemNotInCart_createsNewCartEntry() {
        // Arrange
        Long itemId = 1L;
        Item item = new Item(itemId, "Test Item", "Description", new BigDecimal("100.0"), "image.jpg");
        when(cartRepository.findByItemId(itemId)).thenReturn(null);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // Act
        cartService.addToCart(itemId);

        // Assert
        verify(cartRepository).findByItemId(itemId);
        verify(itemRepository).findById(itemId);
        verify(cartRepository).save(argThat(cart ->
                cart.getItemId().equals(itemId) &&
                cart.getCount() == 1 &&
                cart.getItem().equals(item)
        ));
    }

    @Test
    void addToCart_whenItemAlreadyInCart_incrementsCount() {
        // Arrange
        Long itemId = 1L;
        Item item = new Item(itemId, "Test Item", "Description", new BigDecimal("100.0"), "image.jpg");
        Cart existingCart = new Cart();
        existingCart.setId(1L);
        existingCart.setItemId(itemId);
        existingCart.setItem(item);
        existingCart.setCount(2);
        when(cartRepository.findByItemId(itemId)).thenReturn(existingCart);

        // Act
        cartService.addToCart(itemId);

        // Assert
        verify(cartRepository).findByItemId(itemId);
        verify(itemRepository, never()).findById(anyLong());
        verify(cartRepository).save(argThat(cart -> cart.getCount() == 3));
    }

    @Test
    void addToCart_whenItemDoesNotExist_throwsException() {
        // Arrange
        Long itemId = 999L;
        when(cartRepository.findByItemId(itemId)).thenReturn(null);
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> cartService.addToCart(itemId));
        verify(cartRepository).findByItemId(itemId);
        verify(itemRepository).findById(itemId);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void removeFromCart_whenCountGreaterThanOne_decrementsCount() {
        // Arrange
        Long itemId = 1L;
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setItemId(itemId);
        cart.setCount(3);
        when(cartRepository.findByItemId(itemId)).thenReturn(cart);

        // Act
        cartService.removeFromCart(itemId);

        // Assert
        verify(cartRepository).findByItemId(itemId);
        verify(cartRepository, never()).delete(any());
        verify(cartRepository).save(argThat(c -> c.getCount() == 2));
    }

    @Test
    void removeFromCart_whenCountEqualsOne_deletesCartEntry() {
        // Arrange
        Long itemId = 1L;
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setItemId(itemId);
        cart.setCount(1);
        when(cartRepository.findByItemId(itemId)).thenReturn(cart);

        // Act
        cartService.removeFromCart(itemId);

        // Assert
        verify(cartRepository).findByItemId(itemId);
        verify(cartRepository).delete(cart);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void removeFromCart_whenCartEntryDoesNotExist_doesNothing() {
        // Arrange
        Long itemId = 1L;
        when(cartRepository.findByItemId(itemId)).thenReturn(null);

        // Act
        cartService.removeFromCart(itemId);

        // Assert
        verify(cartRepository).findByItemId(itemId);
        verify(cartRepository, never()).delete(any());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void removeItemFromCart_whenCartEntryExists_deletesIt() {
        // Arrange
        Long itemId = 1L;
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setItemId(itemId);
        cart.setCount(2);
        when(cartRepository.findByItemId(itemId)).thenReturn(cart);

        // Act
        cartService.removeItemFromCart(itemId);

        // Assert
        verify(cartRepository).findByItemId(itemId);
        verify(cartRepository).delete(cart);
    }

    @Test
    void removeItemFromCart_whenCartEntryDoesNotExist_doesNothing() {
        // Arrange
        Long itemId = 1L;
        when(cartRepository.findByItemId(itemId)).thenReturn(null);

        // Act
        cartService.removeItemFromCart(itemId);

        // Assert
        verify(cartRepository).findByItemId(itemId);
        verify(cartRepository, never()).delete(any());
    }

    @Test
    void calculateTotalPrice_withMultipleItems_calculatesCorrectly() {
        // Arrange
        List<ItemDto> items = List.of(
                new ItemDto(1L, "Item1", "Desc1", new BigDecimal("10.0"), "img1.jpg", 2),
                new ItemDto(2L, "Item2", "Desc2", new BigDecimal("20.0"), "img2.jpg", 3)
        );

        // Act
        BigDecimal result = cartService.calculateTotalPrice(items);

        // Assert
        // 10*2 + 20*3 = 20 + 60 = 80
        assertEquals(new BigDecimal("80.0"), result);
    }

    @Test
    void calculateTotalPrice_withEmptyList_returnsZero() {
        // Arrange
        List<ItemDto> items = Collections.emptyList();

        // Act
        BigDecimal result = cartService.calculateTotalPrice(items);

        // Assert
        assertEquals(BigDecimal.ZERO, result);
    }
}
