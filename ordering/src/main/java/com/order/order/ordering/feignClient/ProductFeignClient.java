package com.order.order.ordering.feignClient;

import com.order.order.common.dto.CommonDTO;
import com.order.order.ordering.dto.OrderCreateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

// product-service로 보낼 요청 정의
// name 속성은 eureka에 등록된 application.name을 의미
@FeignClient(name = "product-service")
public interface ProductFeignClient {

    @GetMapping("/product/detail/{productId}")
    CommonDTO getProductById(@PathVariable Long productId);

    @PutMapping("/product/updateStock")
    void updateProductStockQuantity(@RequestBody OrderCreateDTO orderCreateDTO);
}
