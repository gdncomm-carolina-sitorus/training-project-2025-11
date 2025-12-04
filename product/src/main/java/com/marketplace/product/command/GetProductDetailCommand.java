package com.marketplace.product.command;

import com.marketplace.product.entity.Product;
import com.marketplace.product.exception.ProductNotFoundException;
import com.marketplace.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GetProductDetailCommand implements Command<Product, String> {

  @Autowired
  private ProductRepository productRepository;

  @Override
  public Product execute(String id) {
    return productRepository.findById(id)
        .orElseThrow(() -> new ProductNotFoundException(id));
  }
}
