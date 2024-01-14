/*
 * Copyright (c) 2024. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */
package com.malyskok.ordersservice.saga;

import com.malyskok.estore.core.commands.CancelProductReservationCommand;
import com.malyskok.estore.core.commands.ProcessPaymentCommand;
import com.malyskok.estore.core.commands.ReserveProductCommand;
import com.malyskok.estore.core.events.PaymentProcessedEvent;
import com.malyskok.estore.core.events.ProductReservationCancelledEvent;
import com.malyskok.estore.core.events.ProductReservedEvent;
import com.malyskok.estore.core.user.FetchUserPaymentDetailsQuery;
import com.malyskok.estore.core.user.UserDetails;
import com.malyskok.ordersservice.command.commands.ApproveOrderCommand;
import com.malyskok.ordersservice.command.commands.RejectOrderCommand;
import com.malyskok.ordersservice.core.event.OrderApprovedEvent;
import com.malyskok.ordersservice.core.event.OrderCreatedEvent;
import com.malyskok.ordersservice.core.event.OrderRejectedEvent;
import com.malyskok.ordersservice.core.model.OrderSummary;
import com.malyskok.ordersservice.query.FindOrderQuery;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Saga
public class OrderSaga {

    private final String PAYMENT_PROCESSING_DEADLINE = "payment-processing-deadline";

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient QueryGateway queryGateway;

    @Autowired
    private transient DeadlineManager deadlineManager;

    @Autowired
    private transient QueryUpdateEmitter queryUpdateEmitter;

    private String scheduleId;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCreatedEvent event) {
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
                RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(
                        event.getOrderId(), commandResultMessage.exceptionResult().getMessage());
                commandGateway.send(rejectOrderCommand);
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
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            cancelProductReservation(event, e.getMessage());
            return;
        }
        if (userDetails == null) {
            cancelProductReservation(event, "Could not fetch user payment details");
            return;
        }

        log.info(String.format("""
                SagaEventHandler - handle ProductReservedEvent
                orderId: %s
                productId: %s
                User payment details fetched successfully
                userId: %s
                """, event.getOrderId(), event.getProductId(), userDetails.getUserId()));

        scheduleId = deadlineManager.schedule(Duration.of(120, ChronoUnit.SECONDS),
                PAYMENT_PROCESSING_DEADLINE,
                event);

        ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand.builder()
                .paymentId(UUID.randomUUID().toString())
                .orderId(event.getOrderId())
                .paymentDetails(userDetails.getPaymentDetails())
                .build();

        String result = null;
        try {
            result = commandGateway.sendAndWait(processPaymentCommand);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            cancelProductReservation(event, e.getMessage());
            return;
        }

        if (result == null) {
            log.info("ProcessPaymentCommand returned null. Starting to compensate transaction");
            cancelProductReservation(event, "Could not process user payment with provided details");
        }

    }

    private void cancelProductReservation(ProductReservedEvent event, String reason) {
        log.info("cancelProductReservation method called for orderId: ", event.getOrderId());
        cancelPaymentDeadline();
        CancelProductReservationCommand cancelCommand = CancelProductReservationCommand.builder()
                .productId(event.getProductId())
                .quantity(event.getQuantity())
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .reason(reason)
                .build();
        commandGateway.send(cancelCommand);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservationCancelledEvent productReservationCancelledEvent) {
        log.info(String.format("""
                        SagaEventHandler - handle ProductReservationCancelledEvent
                        orderId: %s
                        productId: %s
                        reason: %s
                        """, productReservationCancelledEvent.getOrderId(), productReservationCancelledEvent.getProductId(),
                productReservationCancelledEvent.getReason()));
        RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(
                productReservationCancelledEvent.getOrderId(), productReservationCancelledEvent.getReason());
        commandGateway.send(rejectOrderCommand);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentProcessedEvent event) {
        log.info(String.format("""
                SagaEventHandler - handle PaymentProcessedEvent
                orderId: %s
                paymentId: %s
                """, event.getOrderId(), event.getPaymentId()));

        cancelPaymentDeadline();
        log.info("cancel payment-processing-deadline");

        //approve order
        ApproveOrderCommand approveOrderCommand = new ApproveOrderCommand(event.getOrderId());
        commandGateway.send(approveOrderCommand);
    }

    private void cancelPaymentDeadline() {
        if (scheduleId != null) {
            deadlineManager.cancelSchedule(PAYMENT_PROCESSING_DEADLINE, scheduleId);
            scheduleId = null;
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderApprovedEvent event) {
        log.info(String.format("""
                SagaEventHandler - handle OrderApprovedEvent
                orderId: %s
                orderStatus: %s
                Saga is complete!
                """, event.getOrderId(), event.getOrderStatus()));
//        SagaLifecycle.end();

        queryUpdateEmitter.emit(FindOrderQuery.class, query -> true,
                new OrderSummary(event.getOrderId(), event.getOrderStatus(), ""));
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderRejectedEvent event) {
        log.info(String.format("""
                SagaEventHandler - handle OrderRejectedEvent
                orderId: %s
                orderStatus: %s
                Saga is complete!
                """, event.getOrderId(), event.getOrderStatus()));

        queryUpdateEmitter.emit(FindOrderQuery.class, query -> true,
                new OrderSummary(event.getOrderId(), event.getOrderStatus(),
                        event.getReason()));
    }

    @DeadlineHandler(deadlineName = PAYMENT_PROCESSING_DEADLINE)
    public void handlePaymentDeadline(ProductReservedEvent event) {
        log.info(String.format("""
                DeadlineHandler - handle PAYMENT_PROCESSING_DEADLINE
                orderId: %s
                productId: %s
                """, event.getOrderId(), event.getProductId()));
        cancelProductReservation(event, "Payment timeout");
    }
}