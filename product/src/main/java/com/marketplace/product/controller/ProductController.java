package com.marketplace.product.controller;

import com.marketplace.product.command.GetProductDetailCommand;
import com.marketplace.product.command.SearchProductCommand;
import com.marketplace.product.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
  @Autowired
  private SearchProductCommand searchProductCommand;

  @Autowired
  private GetProductDetailCommand getProductDetailCommand;

  @GetMapping
  public Page<Product> searchProducts(@RequestParam(required = false) String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return searchProductCommand.execute(new SearchProductCommand.Request(query, pageable));
  }

  @GetMapping("/{id}")
  public Product getProduct(@PathVariable String id) {
    return getProductDetailCommand.execute(id);
  }
}
