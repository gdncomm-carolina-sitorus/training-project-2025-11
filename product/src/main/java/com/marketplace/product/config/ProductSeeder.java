package com.marketplace.product.config;

import com.marketplace.product.entity.Product;
import com.marketplace.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProductSeeder implements CommandLineRunner {

  @Autowired
  private ProductRepository productRepository;

  @Override
  public void run(String... args) throws Exception {
    if (productRepository.count() == 0) {
      System.out.println("Seeding products...");
      List<Product> products = new ArrayList<>();
      for (int i = 1; i <= 50000; i++) {
        Product product = new Product();
        product.setName("Product " + i);
        product.setDescription("Description for product " + i);
        product.setPrice(new BigDecimal(10 + (i % 100)));
        product.setCategory("Category " + (i % 10));
        products.add(product);

        if (i % 1000 == 0) {
          productRepository.saveAll(products);
          products.clear();
          System.out.println("Seeded " + i + " products");
        }
      }
      if (!products.isEmpty()) {
        productRepository.saveAll(products);
      }
      System.out.println("Product seeding complete.");
    }
  }
}
