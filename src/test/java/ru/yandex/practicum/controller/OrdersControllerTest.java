package ru.yandex.practicum.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.api.dto.ItemDto;
import ru.yandex.practicum.api.dto.OrderDto;
import ru.yandex.practicum.service.OrderService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrdersController.class)
class OrdersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void getOrders_whenOrdersExist_returnsOrdersView() throws Exception {
        // Arrange
        ItemDto item = new ItemDto(1L, "Test Item", "Description", new BigDecimal("100.0"), "image.jpg", 2);
        OrderDto order = new OrderDto(1L, List.of(item), new BigDecimal("200.0"), true);
        when(orderService.getAllOrders()).thenReturn(List.of(order));

        // Act & Assert
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attribute("orders", List.of(order)));

        verify(orderService).getAllOrders();
    }

    @Test
    void getOrders_whenNoOrders_returnsOrdersViewWithEmptyList() throws Exception {
        // Arrange
        when(orderService.getAllOrders()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attribute("orders", List.of()));
    }

    @Test
    void getOrder_whenOrderExists_returnsOrderView() throws Exception {
        // Arrange
        ItemDto item = new ItemDto(1L, "Test Item", "Description", new BigDecimal("100.0"), "image.jpg", 2);
        OrderDto order = new OrderDto(1L, List.of(item), new BigDecimal("200.0"), true);
        when(orderService.getOrderById(1L)).thenReturn(order);

        // Act & Assert
        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attribute("order", order))
                .andExpect(model().attribute("newOrder", (Boolean) null));

        verify(orderService).getOrderById(1L);
    }

    @Test
    void getOrder_withNewOrderParam_passesNewOrderToModel() throws Exception {
        // Arrange
        ItemDto item = new ItemDto(1L, "Test Item", "Description", new BigDecimal("100.0"), "image.jpg", 2);
        OrderDto order = new OrderDto(1L, List.of(item), new BigDecimal("200.0"), true);
        when(orderService.getOrderById(1L)).thenReturn(order);

        // Act & Assert
        mockMvc.perform(get("/orders/1")
                        .param("newOrder", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("newOrder", true));
    }


    @Test
    void buyCart_whenCartHasItems_createsOrderAndRedirectsToOrderPage() throws Exception {
        // Arrange
        ItemDto item = new ItemDto(1L, "Test Item", "Description", new BigDecimal("100.0"), "image.jpg", 2);
        OrderDto order = new OrderDto(5L, List.of(item), new BigDecimal("200.0"), true);
        when(orderService.createOrderFromCart()).thenReturn(order);

        // Act & Assert
        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().stringValues(HttpHeaders.LOCATION, "/orders/5?newOrder=true"));

        verify(orderService).createOrderFromCart();
    }

    @Test
    void buyCart_whenCartIsEmpty_redirectsToOrdersPage() throws Exception {
        // Arrange
        when(orderService.createOrderFromCart()).thenThrow(new IllegalStateException("Cart is empty"));

        // Act & Assert
        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().stringValues(HttpHeaders.LOCATION, "/orders"));

        verify(orderService).createOrderFromCart();
    }

    @Test
    void buyCart_whenExceptionOccurs_redirectsToOrdersPage() throws Exception {
        // Arrange
        when(orderService.createOrderFromCart()).thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().stringValues(HttpHeaders.LOCATION, "/orders"));
    }
}
