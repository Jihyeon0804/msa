package com.order.order.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.order.product.dto.ProductUpdateStockDTO;
import com.order.order.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockKafkaListener {

    private final ProductService productService;

    @KafkaListener(topics = "stock-update-topic", containerFactory = "kafkaListener")
    public void stockConsumer(String message) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ProductUpdateStockDTO productUpdateStockDTO = objectMapper.readValue(message, ProductUpdateStockDTO.class);
            productService.updateStock(productUpdateStockDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
