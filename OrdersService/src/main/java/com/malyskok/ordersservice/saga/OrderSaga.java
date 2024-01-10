/*
 * Copyright (c) 2024. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */
package com.malyskok.ordersservice.saga;

import com.malyskok.estore.core.commands.ReserveProductCommand;
import com.malyskok.estore.core.events.ProductReservedEvent;
import com.malyskok.estore.core.user.FetchUserPaymentDetailsQuery;
import com.malyskok.estore.core.user.UserDetails;
import com.malyskok.ordersservice.core.event.OrderCreateEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Saga
public class OrderSaga {

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient QueryGateway queryGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCreateEvent event) {
        log.info(String.format("""
                SagaEventHandler - handle OrderCreateEvent
                orderId: %s
                productId: %s
                """, event.getOrderId(), event.getProductId()));

        ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
                .productId(event.getProductId())
                .quantity(event.getQuantity())
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .build();

        commandGateway.send(reserveProductCommand, (commandMessage, commandResultMessage) -> {
            if (commandResultMessage.isExceptional()) {
                //start compensating transaction
            }

        });
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservedEvent event) {
        log.info(String.format("""
                SagaEventHandler - handle ProductReservedEvent
                orderId: %s
                productId: %s
                """, event.getOrderId(), event.getProductId()));

        //process payment
        FetchUserPaymentDetailsQuery userPaymentDetailsQuery = new FetchUserPaymentDetailsQuery();
        userPaymentDetailsQuery.setUserId(event.getUserId());

        UserDetails userDetails = null;
        try {
            userDetails = queryGateway.query(userPaymentDetailsQuery,
                    ResponseTypes.instanceOf(UserDetails.class)).join();
        } catch (Exception e){
            log.error(e.getMessage(), e);
            //start compensating transaction
            return;
        }

        log.info(String.format("""
                SagaEventHandler - handle ProductReservedEvent
                orderId: %s
                productId: %s
                User payment details fetched successfully
                userId: %s
                """, event.getOrderId(), event.getProductId(), userDetails.getUserId()));

    }
}