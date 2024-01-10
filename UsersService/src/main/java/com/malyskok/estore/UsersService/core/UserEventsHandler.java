/*
 * Copyright (c) 2024. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */
package com.malyskok.estore.UsersService.core;

import com.malyskok.estore.core.user.FetchUserPaymentDetailsQuery;
import com.malyskok.estore.core.user.PaymentDetails;
import com.malyskok.estore.core.user.UserDetails;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class UserEventsHandler {

    @QueryHandler
    public UserDetails findUserPaymentDetails(FetchUserPaymentDetailsQuery query){
        PaymentDetails paymentDetails = PaymentDetails.builder()
                .cardNumber("123Card")
                .cvv("123")
                .name("Jan Kowalski")
                .validUntilMonth(12)
                .validUntilYear(2030)
                .build();

        UserDetails userRest = UserDetails.builder()
                .firstName("Jan")
                .lastName("Kowalski")
                .userId(query.getUserId())
                .paymentDetails(paymentDetails)
                .build();

        return userRest;
    }
}