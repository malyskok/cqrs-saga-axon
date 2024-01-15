/*
 * Copyright (c) 2024. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */
package com.malyskok.productsservice.query;

import com.malyskok.estore.core.events.ProductReservationCancelledEvent;
import com.malyskok.estore.core.events.ProductReservedEvent;
import com.malyskok.productsservice.core.data.ProductEntity;
import com.malyskok.productsservice.core.data.ProductsRepository;
import com.malyskok.productsservice.core.event.ProductCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
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
        log.info(String.format("""
                EventHandler - Handle ProductCreatedEvent
                productId: %s
                """, event.getProductId()));

        ProductEntity productEntity = new ProductEntity();
        BeanUtils.copyProperties(event, productEntity);
        productsRepository.save(productEntity);

//        if(true) throw new Exception("Some exception occurred during @CommandHandler");
    }

    @EventHandler
    public void on(ProductReservedEvent event){
        log.info(String.format("""
                EventHandler - Handle ProductReservedEvent
                orderId: %s
                productId: %s
                """, event.getOrderId(), event.getProductId()));

        ProductEntity product = productsRepository.findByProductId(event.getProductId());
        product.setQuantity(product.getQuantity() - event.getQuantity());
        productsRepository.save(product);
    }

    @EventHandler
    public void on(ProductReservationCancelledEvent event){
        ProductEntity product = productsRepository.findByProductId(event.getProductId());
        int fixedQuantity = product.getQuantity() + event.getQuantity();
        product.setQuantity(fixedQuantity);
        productsRepository.save(product);
    }

    @ResetHandler
    public void reset(){
        productsRepository.deleteAll();
    }
}