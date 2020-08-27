package com.shopmall.page.client;

import com.shopmall.item.api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("item-service")
public interface BrandClient extends BrandApi {
}
