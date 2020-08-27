package com.shopmall.cart.client;

import com.shopmall.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {
}
