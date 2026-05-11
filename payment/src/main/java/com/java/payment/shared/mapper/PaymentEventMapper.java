package com.java.payment.shared.mapper;


import com.java.payment.entity.Payments;
import com.java.payment.entity.enums.Currency;
import com.java.payment.events.PaymentEvent;
import com.java.payment.events.PaymentKey;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PaymentEventMapper {

    @Mapping(source = "id", target = "paymentId")
    PaymentKey toEventKey(Payments payments);

    @Mapping(source = "id", target = "paymentId")
    @Mapping(source = "amount", target = "amount.value")
    @Mapping(source = "currency", target = "amount.currency", qualifiedByName = "toCurrency")
    PaymentEvent toEvent(Payments payments);


    @Named("toCurrency")
    default String toCurrency(Currency currency) {
        return currency.name();
    }

}
