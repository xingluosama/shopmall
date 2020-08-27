package com.shopmall.order.web;

import com.shopmall.order.dto.OrderDTO;
import com.shopmall.order.pojo.Order;
import com.shopmall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderDTO orderDTO) {
        // 创建订单
        return ResponseEntity.ok(orderService.createOrder(orderDTO));
    }

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Order> queryOrderById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(orderService.queryOrderById(id));
    }

    /**
     * 创建支付链接
     * @param orderId
     * @return
     */
    @GetMapping("/url/{orderId}")
    public ResponseEntity<String> createPayUrl(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(orderService.createPayUrl(orderId));
    }

    /**
     * 查询订单状态
     * @param orderId
     * @return
     */
    @GetMapping("/state/{orderId}")
    public ResponseEntity<Integer> queryOrderState(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(orderService.queryOrderState(orderId).getValue());
    }
}
