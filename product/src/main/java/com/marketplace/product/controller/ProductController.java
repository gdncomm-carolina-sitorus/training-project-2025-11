package com.marketplace.product.controller;

import com.marketplace.product.command.GetProductDetailCommand;
import com.marketplace.product.command.SearchProductCommand;
import com.marketplace.product.entity.Product;
import com.marketplace.product.model.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
  @Autowired
  private SearchProductCommand searchProductCommand;

  @Autowired
  private GetProductDetailCommand getProductDetailCommand;

  @GetMapping
  public ResponseEntity<ApiResponse<Page<Product>>> searchProducts(@RequestParam(required = false) String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Product> products =
        searchProductCommand.execute(new SearchProductCommand.Request(query, pageable));
    return ResponseEntity.ok(ApiResponse.<Page<Product>>builder()
        .success(true)
        .message("Products retrieved successfully")
        .data(products)
        .build());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<Product>> getProduct(@PathVariable String id) {
    Product product = getProductDetailCommand.execute(id);
    return ResponseEntity.ok(ApiResponse.<Product>builder()
        .success(true)
        .message("Product retrieved successfully")
        .data(product)
        .build());
  }
}
