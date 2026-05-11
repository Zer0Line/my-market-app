package ru.yandex.practicum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.dto.ItemDto;
import ru.yandex.practicum.service.ItemsService;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/")
public class ItemsController {

    private final ItemsService itemsService;

    public ItemsController(ItemsService itemsService) {
        this.itemsService = itemsService;
    }

    @GetMapping()
    public String getItemsDefault(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") String sort,
            @RequestParam(defaultValue = "2") int pageSize,
            @RequestParam(defaultValue = "0") int pageNumber,
            Model model) {
        var itemsPage = itemsService.getItems(search, sort, pageSize, pageNumber);

        // Преобразуем список элементов в список списков по 3 элемента для корректного отображения в шаблоне
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
        model.addAttribute("sort", sort);
        return "items";
    }

    @GetMapping("items")
    public String getItems(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") String sort,
            @RequestParam(defaultValue = "2") int pageSize,
            @RequestParam(defaultValue = "0") int pageNumber,
            Model model) {
        var itemsPage = itemsService.getItems(search, sort, pageSize, pageNumber);

        // Преобразуем список элементов в список списков по 3 элемента для корректного отображения в шаблоне
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
        model.addAttribute("sort", sort);
        return "items";
    }

    @GetMapping("/items/{id}")
    public String getItem(@PathVariable("id") Long id){
        var item = itemsService.getItem(id);
    }
}