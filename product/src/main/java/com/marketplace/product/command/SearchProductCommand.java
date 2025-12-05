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

    String query = request.getQuery();
    if (query.contains("*") || query.contains("?")) {
      String regex = queryToRegex(query);
      return productRepository.findByNameRegex(regex, request.getPageable());
    }

    return productRepository.findByNameContainingIgnoreCase(query, request.getPageable());
  }

  private String queryToRegex(String query) {
    // Escape special regex characters except * and ?
    String escaped = query.replaceAll("[.\\\\+^\\[\\](){}|$]", "\\\\$0");
    // Convert * to .* (any sequence of characters)
    // Convert ? to . (any single character)
    return "^" + escaped.replace("*", ".*").replace("?", ".") + "$";
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
