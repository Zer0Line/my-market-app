package ru.yandex.practicum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.api.ItemAction;
import ru.yandex.practicum.dto.ItemDto;
import ru.yandex.practicum.service.CartService;

import java.util.List;

@Controller
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cart/items")
    public String getCartItems(Model model) {
        List<ItemDto> cartItems = cartService.getCartItems();
        model.addAttribute("items", cartItems);
        model.addAttribute("total", cartService.calculateTotalPrice(cartItems));
        return "cart";
    }

    @PostMapping("/cart/items")
    public String updateCartItem(
            @RequestParam Long id,
            @RequestParam ItemAction action) {

        switch (action) {
            case PLUS -> cartService.addToCart(id);
            case MINUS -> cartService.removeFromCart(id);
            case DELETE -> cartService.removeItemFromCart(id);
            case null, default -> {
            }
        }

        return "redirect:/cart/items";
    }
}
