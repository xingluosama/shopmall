package com.shopmall.auth.client;

import com.shopmall.user.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("user-service")
public interface UserClient extends UserApi {
}
