package com.marketplace.product.repository;

import com.marketplace.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
  Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

  Page<Product> findByNameRegex(String regex, Pageable pageable);
}
