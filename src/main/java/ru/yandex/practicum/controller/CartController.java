package ru.yandex.practicum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.api.ItemAction;
import ru.yandex.practicum.dto.ItemDto;
import ru.yandex.practicum.service.ItemsService;

import java.util.List;

@Controller
public class CartController {

    private final ItemsService itemsService;

    public CartController(ItemsService itemsService) {
        this.itemsService = itemsService;
    }

    @GetMapping("/cart/items")
    public String getCartItems(Model model) {
        List<ItemDto> cartItems = itemsService.getCartItems();
        model.addAttribute("items", cartItems);
        return "cart";
    }

    @PostMapping("/cart/items")
    public String updateCartItem(
            @RequestParam Long id,
            @RequestParam ItemAction action,
            Model model) {
        
        if (action == ItemAction.PLUS) {
            itemsService.addToCart(id);
        } else if (action == ItemAction.MINUS) {
            itemsService.removeFromCart(id);
        }

        return "redirect:/cart/items";
    }
}
