package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.ItemDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.mapper.DtoMapper;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.model.OrderItem;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;

    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(DtoMapper::toOrderDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));
        return DtoMapper.toOrderDto(order);
    }

    @Transactional
    public OrderDto createOrderFromCart() {
        // Get cart items
        List<ItemDto> cartItems = cartService.getCartItems();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }
        BigDecimal totalSum = cartService.calculateTotalPrice(cartItems);
        Order order = new Order();
        order.setTotalSum(totalSum);
        order.setNewOrder(true);
        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItemDto -> {
                    Item item = itemRepository.findById(cartItemDto.id())
                            .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + cartItemDto.id()));
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setItem(item);
                    orderItem.setCount(cartItemDto.count());
                    return orderItem;
                })
                .collect(Collectors.toList());

        order.setOrderItems(orderItems);
        Order savedOrder = orderRepository.save(order);
        cartRepository.deleteAll();
        return DtoMapper.toOrderDto(savedOrder);
    }
}