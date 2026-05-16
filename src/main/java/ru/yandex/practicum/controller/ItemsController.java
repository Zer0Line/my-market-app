package ru.yandex.practicum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.api.ItemAction;
import ru.yandex.practicum.api.SortType;
import ru.yandex.practicum.api.dto.ItemDto;
import ru.yandex.practicum.service.CartService;
import ru.yandex.practicum.service.ItemsService;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/")
public class ItemsController {

    private final ItemsService itemsService;

    private final CartService cartService;

    public ItemsController(ItemsService itemsService,
                           CartService cartService) {
        this.itemsService = itemsService;
        this.cartService = cartService;
    }

    @GetMapping()
    public String getItemsDefault(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") SortType sort,
            @RequestParam(defaultValue = "2") int pageSize,
            @RequestParam(defaultValue = "0") int pageNumber,
            Model model) {

        var itemsPage = itemsService.getItems(search, sort, pageSize, pageNumber);

        List<ItemDto> itemsList = itemsPage.getContent();
        List<List<ItemDto>> groupedItems = new ArrayList<>();
        for (int i = 0; i < itemsList.size(); i += 3) {
            int end = Math.min(i + 3, itemsList.size());
            groupedItems.add(itemsList.subList(i, end));
        }

        model.addAttribute("items", groupedItems);
        model.addAttribute("itemCount", pageSize);
        model.addAttribute("paging", itemsPage);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort.name());
        return "items";
    }

    @GetMapping("items")
    public String getItems(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") SortType sort,
            @RequestParam(defaultValue = "2") int pageSize,
            @RequestParam(defaultValue = "0") int pageNumber,
            Model model) {
        var itemsPage = itemsService.getItems(search, sort, pageSize, pageNumber);

        List<ItemDto> itemsList = itemsPage.getContent();
        List<List<ItemDto>> groupedItems = new ArrayList<>();
        for (int i = 0; i < itemsList.size(); i += pageSize) {
            int end = Math.min(i + pageSize, itemsList.size());
            groupedItems.add(itemsList.subList(i, end));
        }

        model.addAttribute("items", groupedItems);
        model.addAttribute("itemCount", pageSize);
        model.addAttribute("paging", itemsPage);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort.name());
        return "items";
    }

    @GetMapping("/items/{id}")
    public String getItem(@PathVariable("id") Long id, Model model) {
        var item = itemsService.getItem(id);
        model.addAttribute("item", item);
        return "/item";
    }

    @PostMapping("/items")
    public String handleItemAction(
            @RequestParam Long id,
            @RequestParam String search,
            @RequestParam SortType sort,
            @RequestParam int pageNumber,
            @RequestParam int pageSize,
            @RequestParam ItemAction action) {

        if (action == ItemAction.PLUS) {
            cartService.addToCart(id);
        } else if (action == ItemAction.MINUS) {
            cartService.removeFromCart(id);
        }

        return "redirect:/items?search=" + search +
                "&sort=" + sort +
                "&pageNumber=" + pageNumber +
                "&pageSize=" + pageSize;
    }
}