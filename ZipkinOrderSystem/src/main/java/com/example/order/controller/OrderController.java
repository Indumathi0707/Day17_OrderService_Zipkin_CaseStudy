package com.example.order.controller;

import com.example.order.model.OrderRequest;
import com.example.order.model.OrderResponse;
import com.example.order.model.PaymentRequest;
import com.example.order.model.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private RestTemplate restTemplate;

    private final String paymentServiceUrl = "http://localhost:8082/payments/process";

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        try {
            // Create payment request
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setOrderId(request.getOrderId());
            paymentRequest.setAmount(request.getAmount());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PaymentRequest> entity = new HttpEntity<>(paymentRequest, headers);

            // Call payment-service
            PaymentResponse paymentResponse =
                    restTemplate.postForObject(paymentServiceUrl, entity, PaymentResponse.class);

            // Simulate order processing delay
            Thread.sleep(150);

            // Prepare response
            OrderResponse response = new OrderResponse();
            response.setOrderId(request.getOrderId());
            response.setAmount(request.getAmount());
            response.setPaymentStatus(paymentResponse != null ? paymentResponse.getStatus() : "FAILED");
            response.setTransactionId(paymentResponse != null ? paymentResponse.getTransactionId() : null);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            OrderResponse response = new OrderResponse();
            response.setOrderId(request.getOrderId());
            response.setAmount(request.getAmount());
            response.setPaymentStatus("ERROR");
            return ResponseEntity.status(500).body(response);
        }
    }
}
