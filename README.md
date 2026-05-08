# ZPayment

It's a multi-module project that will emulate a payment that will be passed throught a fraud check.

Each module a part from `avros`, will represent a different microservice.

## Payment Module

## Fraud Gateway Module

## Avros

For simplicity we will use common module to have avro schemas shared between microservices.

In real world would be retrieved the avros from schema registry.

e.g `fraud-gateway` would contain only [fraud_payment_result.avdl](avros/src/main/resources/avro/fraud/fraud_payment_result.avdl)
and the [payment_submitted.avdl](avros/src/main/resources/avro/payment/payment_submitted.avdl) would be pulled from SC.