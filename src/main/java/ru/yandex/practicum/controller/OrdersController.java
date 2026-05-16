package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.api.dto.OrderDto;
import ru.yandex.practicum.service.OrderService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrdersController {

    private final OrderService orderService;

    @GetMapping("/orders")
    public String getOrders(Model model) {
        List<OrderDto> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String getOrder(@PathVariable Long id,
                           @RequestParam(required = false) Boolean newOrder,
                           Model model) {
        OrderDto order = orderService.getOrderById(id);
        model.addAttribute("newOrder", newOrder);
        model.addAttribute("order", order);
        return "order";
    }

    @PostMapping("/buy")
    public String buyCart() {
        try {
            OrderDto orderDto = orderService.createOrderFromCart();
            return "redirect:/orders/" + orderDto.id() + "?newOrder=true";
        } catch (Exception e) {
            return "redirect:/orders";
        }
    }
}