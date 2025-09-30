package com.hmall.api.client;


import com.hmall.api.config.DefaultFeignConfig;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.api.fallback.ItemClientFallback;
import com.hmall.common.utils.BeanUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@FeignClient(value = "item-service" ,fallbackFactory = ItemClientFallback.class)
public interface ItemClient {
    @GetMapping("/items")
    public List<ItemDTO> queryItemByIds(@RequestParam Collection<Long> ids);

    @PutMapping("/items/stock/deduct")
    void deductStock(@RequestBody List<OrderDetailDTO> items);

    @GetMapping("/items/{id}")
    public ItemDTO queryItemById(@PathVariable("id") Long id) ;

}
