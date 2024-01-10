package com.malyskok.productsservice.command;

import java.math.BigDecimal;

import com.malyskok.estore.core.commands.ReserveProductCommand;
import com.malyskok.estore.core.events.ProductReservedEvent;
import com.malyskok.productsservice.core.event.ProductCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

import static org.apache.commons.lang.StringUtils.isBlank;

@Slf4j
@Aggregate
public class ProductAggregate {

    @AggregateIdentifier
    private String productId;
    private String title;
    private BigDecimal price;
    private Integer quantity;

    public ProductAggregate() {
    }

    @CommandHandler
    public ProductAggregate(CreateProductCommand createProductCommand) {
        // Validate Create Product Command

        if (createProductCommand.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Price cannot be less or equal than 0");
        }

        if (isBlank(createProductCommand.getTitle())) {
            throw new IllegalStateException("Title cannot be blank");
        }

        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent();
        BeanUtils.copyProperties(createProductCommand, productCreatedEvent);
        AggregateLifecycle.apply(productCreatedEvent);

//        if(true) throw new Exception("Some exception occurred during @CommandHandler");
    }

    @CommandHandler
    public void handleReserveProduct(ReserveProductCommand command){
        if(quantity < command.getQuantity()){
            throw new IllegalArgumentException(
                    String.format("Insufficient number of item in stock (%d), ordered: %d",
                            quantity, command.getQuantity()));
        }

        ProductReservedEvent reservedEvent = ProductReservedEvent.builder()
                .productId(command.getProductId())
                .quantity(command.getQuantity())
                .orderId(command.getOrderId())
                .userId(command.getUserId())
                .build();

        AggregateLifecycle.apply(reservedEvent);
    }

    @EventSourcingHandler
    public void on(ProductCreatedEvent event) {
        log.info("""
                EventSourcingHandler - Handle ProductReservedEvent
                productId: %s
                """, event.getProductId());

        this.productId = event.getProductId();
        this.price = event.getPrice();
        this.quantity = event.getQuantity();
        this.title = event.getTitle();
    }

    @EventSourcingHandler
    public void on(ProductReservedEvent event){
        log.info(String.format("""
                EventSourcingHandler - Handle ProductReservedEvent
                orderId: %s
                productId: %s
                """, event.getOrderId(), event.getProductId()));

        this.quantity -= event.getQuantity();
    }
}