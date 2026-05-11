package com.java.payment.shared.mapper;

import com.java.payment.api.model.PaymentRequest;
import com.java.payment.api.model.PaymentResponse;
import com.java.payment.entity.Payments;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentsMapper {

    Payments toEntity(PaymentRequest paymentRequest);

    PaymentResponse toResponse(Payments payments);

}
