package com.shopmall.order.dto;

import com.shopmall.common.dto.CartDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    @NonNull
    private Long addressId;         // 收货人地址id
    @NonNull
    private Integer paymentType;    // 付款类型
    @NonNull
    private List<CartDTO> orderDetails;    // 订单详情
}
