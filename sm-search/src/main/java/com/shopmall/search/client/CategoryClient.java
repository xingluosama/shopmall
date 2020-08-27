package com.shopmall.search.client;

import com.shopmall.item.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("item-service")
public interface CategoryClient extends CategoryApi {
}