/*
 * Copyright (c) 2024. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */
package com.malyskok.productsservice.query;

import com.malyskok.productsservice.core.data.ProductEntity;
import com.malyskok.productsservice.core.data.ProductsRepository;
import com.malyskok.productsservice.core.event.ProductCreatedEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("product-group")
public class ProductEventsHandler {

    private final ProductsRepository productsRepository;

    @Autowired
    public ProductEventsHandler(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @ExceptionHandler(resultType = Exception.class)
    public void handleException(Exception ex) throws Exception {
        //rethrow to ListenerInvocationErrorHandler
        throw ex;
    }

    @ExceptionHandler(resultType = IllegalStateException.class)
    public void handleIllegalStateException(IllegalStateException ex){
        //rethrow to ListenerInvocationErrorHandler
        throw ex;
    }

    @EventHandler
    public void on(ProductCreatedEvent event) throws Exception {
        ProductEntity productEntity = new ProductEntity();
        BeanUtils.copyProperties(event, productEntity);
        productsRepository.save(productEntity);

//        if(true) throw new Exception("Some exception occurred during @CommandHandler");
    }
}