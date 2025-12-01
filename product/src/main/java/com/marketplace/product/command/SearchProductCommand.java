package com.marketplace.product.command;

import com.marketplace.product.entity.Product;
import com.marketplace.product.repository.ProductRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class SearchProductCommand implements Command<Page<Product>, SearchProductCommand.Request> {

  @Autowired
  private ProductRepository productRepository;

  @Override
  public Page<Product> execute(Request request) {
    if (request.getQuery() == null || request.getQuery().isEmpty()) {
      return productRepository.findAll(request.getPageable());
    }
    return productRepository.findByNameContainingIgnoreCase(request.getQuery(),
        request.getPageable());
  }

  @Data
  public static class Request {
    private String query;
    private Pageable pageable;

    public Request(String query, Pageable pageable) {
      this.query = query;
      this.pageable = pageable;
    }
  }
}
