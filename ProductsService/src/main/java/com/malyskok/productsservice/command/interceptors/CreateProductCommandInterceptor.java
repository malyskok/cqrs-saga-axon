/*
 * Copyright (c) 2024. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */
package com.malyskok.productsservice.command.interceptors;

import com.malyskok.productsservice.command.CreateProductCommand;
import com.malyskok.productsservice.core.data.ProductLookupEntity;
import com.malyskok.productsservice.core.data.ProductLookupRepository;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BiFunction;

@Component
public class CreateProductCommandInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateProductCommandInterceptor.class);

    private final ProductLookupRepository productLookupRepository;

    @Autowired
    public CreateProductCommandInterceptor(ProductLookupRepository productLookupRepository) {
        this.productLookupRepository = productLookupRepository;
    }

    @Nonnull
    @Override
    public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(@Nonnull List<? extends CommandMessage<?>> messages) {
        return (index, command) -> {

            LOGGER.info("Intercepted command message: " + command.getPayloadType());

            if (CreateProductCommand.class.equals(command.getPayloadType())) {

                CreateProductCommand createCommand = (CreateProductCommand) command.getPayload();

                ProductLookupEntity lookupEntity = productLookupRepository.findByProductIdOrTitle(
                        createCommand.getProductId(), createCommand.getTitle());

                if (lookupEntity != null) {
                    throw new IllegalStateException(
                            String.format("Product with %s productid or %s title already exists",
                                    lookupEntity.getProductId(), lookupEntity.getTitle()));
                }
            }
            return command;
        };
    }
}