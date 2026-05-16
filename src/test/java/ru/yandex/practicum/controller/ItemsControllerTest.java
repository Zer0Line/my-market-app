package ru.yandex.practicum.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.api.PageWrapper;
import ru.yandex.practicum.api.SortType;
import ru.yandex.practicum.api.dto.ItemDto;
import ru.yandex.practicum.service.CartService;
import ru.yandex.practicum.service.ItemsService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemsController.class)
class ItemsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemsService itemsService;

    @MockitoBean
    private CartService cartService;

    @Test
    void getItemsDefault_whenCalled_returnsItemsView() throws Exception {
        // Arrange
        ItemDto item = new ItemDto(1L, "Test Item", "Description", new BigDecimal("100.0"), "image.jpg", 0);
        PageWrapper<ItemDto> page = new PageWrapper<>(new PageImpl<>(List.of(item)));
        when(itemsService.getItems(eq(""), eq(SortType.NO), eq(2), eq(0))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("paging"))
                .andExpect(model().attribute("sort", "NO"));
    }

    @Test
    void getItemsDefault_withSearchParams_passesParamsToService() throws Exception {
        // Arrange
        PageWrapper<ItemDto> page = new PageWrapper<>(new PageImpl<>(List.of()));
        when(itemsService.getItems(eq("laptop"), eq(SortType.PRICE), eq(5), eq(1))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/")
                        .param("search", "laptop")
                        .param("sort", "PRICE")
                        .param("pageSize", "5")
                        .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attribute("search", "laptop"))
                .andExpect(model().attribute("sort", "PRICE"));

        verify(itemsService).getItems("laptop", SortType.PRICE, 5, 1);
    }

    @Test
    void getItems_whenCalled_returnsItemsView() throws Exception {
        // Arrange
        ItemDto item = new ItemDto(1L, "Test Item", "Description", new BigDecimal("100.0"), "image.jpg", 0);
        PageWrapper<ItemDto> page = new PageWrapper<>(new PageImpl<>(List.of(item)));
        when(itemsService.getItems(eq(""), eq(SortType.NO), eq(2), eq(0))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("paging"));
    }

    @Test
    void getItems_withPaginationParams_groupsItemsCorrectly() throws Exception {
        // Arrange
        ItemDto item1 = new ItemDto(1L, "Item1", "Desc1", new BigDecimal("10.0"), "img1.jpg", 0);
        ItemDto item2 = new ItemDto(2L, "Item2", "Desc2", new BigDecimal("20.0"), "img2.jpg", 0);
        ItemDto item3 = new ItemDto(3L, "Item3", "Desc3", new BigDecimal("30.0"), "img3.jpg", 0);
        PageWrapper<ItemDto> page = new PageWrapper<>(new PageImpl<>(List.of(item1, item2, item3)));
        when(itemsService.getItems(eq(""), eq(SortType.ALPHA), eq(3), eq(0))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/items")
                        .param("sort", "ALPHA")
                        .param("pageSize", "3")
                        .param("pageNumber", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attribute("itemCount", 3));
    }

    @Test
    void getItem_whenItemExists_returnsItemView() throws Exception {
        // Arrange
        ItemDto item = new ItemDto(1L, "Test Item", "Description", new BigDecimal("100.0"), "image.jpg", 0);
        when(itemsService.getItem(1L)).thenReturn(item);

        // Act & Assert
        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("/item"))
                .andExpect(model().attributeExists("item"))
                .andExpect(model().attribute("item", item));

        verify(itemsService).getItem(1L);
    }

    @Test
    void handleItemAction_withPlusAction_addsToCartAndRedirects() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/items")
                        .param("id", "1")
                        .param("search", "test")
                        .param("sort", "NO")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .param("action", "PLUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?search=test&sort=NO&pageNumber=0&pageSize=10"));

        verify(cartService).addToCart(1L);
        verify(cartService, never()).removeFromCart(anyLong());
    }

    @Test
    void handleItemAction_withMinusAction_removesFromCartAndRedirects() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/items")
                        .param("id", "2")
                        .param("search", "laptop")
                        .param("sort", "PRICE")
                        .param("pageNumber", "1")
                        .param("pageSize", "5")
                        .param("action", "MINUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?search=laptop&sort=PRICE&pageNumber=1&pageSize=5"));

        verify(cartService).removeFromCart(2L);
        verify(cartService, never()).addToCart(anyLong());
    }

    @Test
    void handleItemAction_withDeleteAction_doesNothingAndRedirects() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/items")
                        .param("id", "1")
                        .param("search", "")
                        .param("sort", "NO")
                        .param("pageNumber", "0")
                        .param("pageSize", "2")
                        .param("action", "DELETE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?search=&sort=NO&pageNumber=0&pageSize=2"));

        verify(cartService, never()).addToCart(anyLong());
        verify(cartService, never()).removeFromCart(anyLong());
    }
}
