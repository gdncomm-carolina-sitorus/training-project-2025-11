package com.marketplace.product.command;

import com.marketplace.product.entity.Product;
import com.marketplace.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SearchProductCommandTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SearchProductCommand searchProductCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void execute_shouldCallFindByNameContainingIgnoreCase_whenNoWildcard() {
        String query = "product";
        Pageable pageable = PageRequest.of(0, 10);
        SearchProductCommand.Request request = new SearchProductCommand.Request(query, pageable);
        when(productRepository.findByNameContainingIgnoreCase(any(), any())).thenReturn(Page.empty());
        searchProductCommand.execute(request);
        verify(productRepository).findByNameContainingIgnoreCase(eq("product"), eq(pageable));
    }

    @Test
    void execute_shouldCallFindByNameRegex_whenWildcardPresent() {
        String query = "pro*";
        Pageable pageable = PageRequest.of(0, 10);
        SearchProductCommand.Request request = new SearchProductCommand.Request(query, pageable);

        when(productRepository.findByNameRegex(any(), any())).thenReturn(Page.empty());
        searchProductCommand.execute(request);
        verify(productRepository).findByNameRegex(eq("^pro.*$"), eq(pageable));
    }
}
