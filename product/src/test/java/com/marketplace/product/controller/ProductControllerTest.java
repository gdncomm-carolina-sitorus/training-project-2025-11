package com.marketplace.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.product.command.GetProductDetailCommand;
import com.marketplace.product.command.SearchProductCommand;
import com.marketplace.product.entity.Product;
import com.marketplace.product.exception.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private SearchProductCommand searchProductCommand;

  @MockitoBean
  private GetProductDetailCommand getProductDetailCommand;

  @Test
  void testSearchProducts_Success() throws Exception {
    Product product1 = new Product();
    product1.setId("p1");
    product1.setName("Product One");

    Product product2 = new Product();
    product2.setId("p2");
    product2.setName("Product Two");

    Page<Product> page = new PageImpl<>(List.of(product1, product2), PageRequest.of(0, 10), 2);

    when(searchProductCommand.execute(any(SearchProductCommand.Request.class))).thenReturn(page);

    mockMvc.perform(get("/api/products").param("query", "test")
            .param("page", "0")
            .param("size", "10")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Products retrieved successfully"))
        .andExpect(jsonPath("$.data.content[0].id").value("p1"))
        .andExpect(jsonPath("$.data.content[0].name").value("Product One"))
        .andExpect(jsonPath("$.data.content[1].id").value("p2"))
        .andExpect(jsonPath("$.data.content[1].name").value("Product Two"));
  }

  @Test
  void testGetProduct_Success() throws Exception {
    Product product = new Product();
    product.setId("p1");
    product.setName("Product One");

    when(getProductDetailCommand.execute("p1")).thenReturn(product);

    mockMvc.perform(get("/api/products/p1").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Product retrieved successfully"))
        .andExpect(jsonPath("$.data.id").value("p1"))
        .andExpect(jsonPath("$.data.name").value("Product One"));
  }

  @Test
  void testGetProduct_NotFound() throws Exception {
    when(getProductDetailCommand.execute("p999")).thenThrow(new ProductNotFoundException("p999"));
    mockMvc.perform(get("/api/products/p999").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Product not found with id: p999"))
        .andExpect(jsonPath("$.data").doesNotExist());
  }
}
