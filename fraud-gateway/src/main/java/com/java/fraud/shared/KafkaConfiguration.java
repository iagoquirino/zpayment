package com.java.fraud.shared;

import com.java.payment.events.PaymentEvent;
import com.java.payment.events.PaymentKey;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

@EnableKafka
@Configuration
public class KafkaConfiguration {

    @Bean
    public ProducerFactory<SpecificRecord, SpecificRecord> producerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties());
    }

    @Bean
    public KafkaTemplate<SpecificRecord, SpecificRecord> kafkaTemplate(ProducerFactory<SpecificRecord, SpecificRecord> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConsumerFactory<PaymentKey, PaymentEvent> consumerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<PaymentKey, PaymentEvent> kafkaListenerContainerFactory(ConsumerFactory<PaymentKey, PaymentEvent> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<PaymentKey, PaymentEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
