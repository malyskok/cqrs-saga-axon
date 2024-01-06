/*
 * Copyright (c) 2024. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */
package com.malyskok.productsservice.query;

import com.malyskok.productsservice.ProductRestModel;
import com.malyskok.productsservice.core.data.ProductEntity;
import com.malyskok.productsservice.core.data.ProductsRepository;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductsQueryHandler {
    private final ProductsRepository productsRepository;

    @Autowired
    public ProductsQueryHandler(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @QueryHandler
    public List<ProductRestModel> findProducts(FindProductsQuery findProductsQuery) {
        List<ProductEntity> entities = productsRepository.findAll();
        return entities.stream()
                .map(entity -> new ProductRestModel(entity.getTitle(), entity.getPrice(), entity.getQuantity()))
                .toList();
    }
}