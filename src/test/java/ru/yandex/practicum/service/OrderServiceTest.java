package ru.yandex.practicum.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.api.dto.ItemDto;
import ru.yandex.practicum.api.dto.OrderDto;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.model.OrderItem;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest extends BaseServiceTest {

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private CartRepository cartRepository;

    @MockitoBean
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Test
    void getAllOrders_whenOrdersExist_returnsListOfOrderDto() {
        // Arrange
        Order order = createSampleOrder(1L);
        when(orderRepository.findAll()).thenReturn(List.of(order));

        // Act
        List<OrderDto> result = orderService.getAllOrders();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        OrderDto dto = result.getFirst();
        assertEquals(1L, dto.id());
        assertEquals(new BigDecimal("150.0"), dto.totalSum());
        assertTrue(dto.newOrder());
        assertEquals(1, dto.items().size());
        verify(orderRepository).findAll();
    }

    @Test
    void getAllOrders_whenNoOrders_returnsEmptyList() {
        // Arrange
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<OrderDto> result = orderService.getAllOrders();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository).findAll();
    }

    @Test
    void getOrderById_whenOrderExists_returnsOrderDto() {
        // Arrange
        Long orderId = 1L;
        Order order = createSampleOrder(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        OrderDto result = orderService.getOrderById(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.id());
        assertEquals(new BigDecimal("150.0"), result.totalSum());
        assertTrue(result.newOrder());
        assertEquals(1, result.items().size());
        ItemDto itemDto = result.items().getFirst();
        assertEquals(10L, itemDto.id());
        assertEquals("Order Item", itemDto.title());
        assertEquals(2, itemDto.count());
        verify(orderRepository).findById(orderId);
    }

    @Test
    void getOrderById_whenOrderDoesNotExist_throwsException() {
        // Arrange
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> orderService.getOrderById(orderId));
        verify(orderRepository).findById(orderId);
    }

    @Test
    void createOrderFromCart_whenCartHasItems_createsOrderAndClearsCart() {
        // Arrange
        long itemId = 1L;
        ItemDto cartItem = new ItemDto(itemId, "Cart Item", "Description", new BigDecimal("50.0"), "img.jpg", 2);
        Item item = new Item(itemId, "Cart Item", "Description", new BigDecimal("50.0"), "img.jpg");

        when(cartService.getCartItems()).thenReturn(List.of(cartItem));
        when(cartService.calculateTotalPrice(List.of(cartItem))).thenReturn(new BigDecimal("100.0"));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1L);
            return savedOrder;
        });

        // Act
        OrderDto result = orderService.createOrderFromCart();

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(new BigDecimal("100.0"), result.totalSum());
        assertTrue(result.newOrder());
        assertEquals(1, result.items().size());
        verify(cartService).getCartItems();
        verify(cartService).calculateTotalPrice(List.of(cartItem));
        verify(itemRepository).findById(itemId);
        verify(orderRepository).save(any(Order.class));
        verify(cartRepository).deleteAll();
    }

    @Test
    void createOrderFromCart_whenCartIsEmpty_throwsException() {
        // Arrange
        when(cartService.getCartItems()).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> orderService.createOrderFromCart());
        verify(cartService).getCartItems();
        verify(orderRepository, never()).save(any());
        verify(cartRepository, never()).deleteAll();
    }

    @Test
    void createOrderFromCart_whenCartItemsIsNull_throwsException() {
        // Arrange
        when(cartService.getCartItems()).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> orderService.createOrderFromCart());
        verify(cartService).getCartItems();
        verify(orderRepository, never()).save(any());
        verify(cartRepository, never()).deleteAll();
    }

    @Test
    void createOrderFromCart_withMultipleCartItems_createsOrderWithAllItems() {
        // Arrange
        ItemDto item1 = new ItemDto(1L, "Item1", "Desc1", new BigDecimal("10.0"), "img1.jpg", 2);
        ItemDto item2 = new ItemDto(2L, "Item2", "Desc2", new BigDecimal("20.0"), "img2.jpg", 3);
        List<ItemDto> cartItems = List.of(item1, item2);

        Item entity1 = new Item(1L, "Item1", "Desc1", new BigDecimal("10.0"), "img1.jpg");
        Item entity2 = new Item(2L, "Item2", "Desc2", new BigDecimal("20.0"), "img2.jpg");

        when(cartService.getCartItems()).thenReturn(cartItems);
        when(cartService.calculateTotalPrice(cartItems)).thenReturn(new BigDecimal("80.0"));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(entity1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(entity2));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1L);
            return savedOrder;
        });

        // Act
        OrderDto result = orderService.createOrderFromCart();

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(new BigDecimal("80.0"), result.totalSum());
        assertEquals(2, result.items().size());
        verify(itemRepository).findById(1L);
        verify(itemRepository).findById(2L);
        verify(cartRepository).deleteAll();
    }

    private Order createSampleOrder(Long id) {
        Order order = new Order();
        order.setId(id);
        order.setTotalSum(new BigDecimal("150.0"));
        order.setNewOrder(true);

        Item item = new Item(10L, "Order Item", "Description", new BigDecimal("75.0"), "image.jpg");
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setOrder(order);
        orderItem.setItem(item);
        orderItem.setCount(2);

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);
        order.setOrderItems(orderItems);

        return order;
    }
}
