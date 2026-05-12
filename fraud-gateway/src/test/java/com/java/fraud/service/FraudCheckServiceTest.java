package com.java.fraud.service;

import com.java.fraud.client.FraudCheckerClient;
import com.java.fraud.client.dto.FraudCheckerResponse;
import com.java.fraud.shared.FraudCheckerResult;
import com.java.payment.events.PaymentEvent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.java.fraud.TestUtils.givenFraudCheckerResponse;
import static com.java.fraud.TestUtils.givenPaymentEvent;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class FraudCheckServiceTest {

    private FraudCheckService testObject;

    @Mock
    private FraudCheckerClient fraudCheckerClient;

    @Mock
    private FraudCheckerProducer fraudCheckerProducer;

    @Captor
    private ArgumentCaptor<FraudChecker> captorFraudChecker;


    @BeforeEach
    void setUp() {
        testObject = new FraudCheckService(fraudCheckerClient, fraudCheckerProducer);
    }

    @Test
    void checkFraud() {
        // given
        PaymentEvent event = givenPaymentEvent();
        FraudCheckerResponse fraudCheckerResponse = givenFraudCheckerResponse();
        given(fraudCheckerClient.checkFraud(event)).willReturn(fraudCheckerResponse);

        // when
        testObject.checkFraud(event);

        // then
        then(fraudCheckerClient).should().checkFraud(event);

        then(fraudCheckerProducer).should().publish(captorFraudChecker.capture());

        FraudChecker fraudChecker = captorFraudChecker.getValue();

        Assertions.assertThat(fraudChecker.checkId()).isEqualTo(fraudCheckerResponse.checkId());
        Assertions.assertThat(fraudChecker.paymentId()).isEqualTo(event.getPaymentId());
        Assertions.assertThat(fraudChecker.result()).isEqualTo(FraudCheckerResult.PASS);
        Assertions.assertThat(fraudChecker.id()).isNotNull();
    }


}