package ru.yandex.practicum.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.api.dto.ItemDto;
import ru.yandex.practicum.service.CartService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Test
    void getCartItems_whenCartHasItems_returnsCartView() throws Exception {
        // Arrange
        ItemDto item = new ItemDto(1L, "Test Item", "Description", new BigDecimal("100.0"), "image.jpg", 2);
        when(cartService.getCartItems()).thenReturn(List.of(item));
        when(cartService.calculateTotalPrice(anyList())).thenReturn(new BigDecimal("200.0"));

        // Act & Assert
        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attribute("total", new BigDecimal("200.0")));

        verify(cartService).getCartItems();
        verify(cartService).calculateTotalPrice(anyList());
    }

    @Test
    void getCartItems_whenCartIsEmpty_returnsCartViewWithEmptyList() throws Exception {
        // Arrange
        when(cartService.getCartItems()).thenReturn(List.of());
        when(cartService.calculateTotalPrice(anyList())).thenReturn(BigDecimal.ZERO);

        // Act & Assert
        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", List.of()))
                .andExpect(model().attribute("total", BigDecimal.ZERO));
    }

    @Test
    void updateCartItem_withPlusAction_addsToCartAndRedirects() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "PLUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().stringValues(HttpHeaders.LOCATION, "/cart/items"));

        verify(cartService).addToCart(1L);
        verify(cartService, never()).removeFromCart(anyLong());
        verify(cartService, never()).removeItemFromCart(anyLong());
    }

    @Test
    void updateCartItem_withMinusAction_removesFromCartAndRedirects() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "MINUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().stringValues(HttpHeaders.LOCATION, "/cart/items"));

        verify(cartService).removeFromCart(1L);
        verify(cartService, never()).addToCart(anyLong());
        verify(cartService, never()).removeItemFromCart(anyLong());
    }

    @Test
    void updateCartItem_withDeleteAction_deletesFromCartAndRedirects() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "DELETE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().stringValues(HttpHeaders.LOCATION, "/cart/items"));

        verify(cartService).removeItemFromCart(1L);
        verify(cartService, never()).addToCart(anyLong());
        verify(cartService, never()).removeFromCart(anyLong());
    }
}
