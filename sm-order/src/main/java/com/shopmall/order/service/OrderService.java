package com.shopmall.order.service;

import com.shopmall.auth.entity.UserInfo;
import com.shopmall.common.dto.CartDTO;
import com.shopmall.common.enums.ExceptionEnum;
import com.shopmall.common.exception.SmException;
import com.shopmall.common.utils.IdWorker;
import com.shopmall.item.pojo.Sku;
import com.shopmall.order.client.AddressClient;
import com.shopmall.order.client.GoodsClient;
import com.shopmall.order.dto.AddressDTO;
import com.shopmall.order.dto.OrderDTO;
import com.shopmall.order.enums.OrderStatusEnum;
import com.shopmall.order.enums.PayState;
import com.shopmall.order.interceptor.UserInterceptor;
import com.shopmall.order.mapper.OrderDetailMapper;
import com.shopmall.order.mapper.OrderMapper;
import com.shopmall.order.mapper.OrderStatusMapper;
import com.shopmall.order.pojo.Order;
import com.shopmall.order.pojo.OrderDetail;
import com.shopmall.order.pojo.OrderStatus;
import com.shopmall.order.utils.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper detailMapper;

    @Autowired
    private OrderStatusMapper statusMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private PayHelper payHelper;

    @Transactional
    public Long createOrder(OrderDTO orderDTO) {

        // 新增订单
        Order order = new Order();
        // 订单编号，基本信息
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setPaymentType(orderDTO.getPaymentType());
        // 用户信息
        UserInfo user = UserInterceptor.getUser();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);
        // 收货人地址
        // 获取收货人信息
        AddressDTO addr = AddressClient.findById(orderDTO.getAddressId());
        order.setReceiver(addr.getName());
        order.setReceiverAddress(addr.getAddress());
        order.setReceiverCity(addr.getCity());
        order.setReceiverDistrict(addr.getDistrict());
        order.setReceiverMobile(addr.getPhone());
        order.setReceiverState(addr.getState());
        order.setReceiverZip(addr.getZipCode());

        // 金额
        // 将CartDTO转为一个map，key是sku的id，值是num
        Map<Long, Integer> numMap = orderDTO.getOrderDetails().stream()
                .collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        // 获取所有sku的id
        Set<Long> ids = numMap.keySet();
        // 根据id查询sku
        List<Sku> skus = goodsClient.querySkuByIds(new ArrayList<>(ids));

        // 准备orderDetail集合
        List<OrderDetail> details = new ArrayList<>();

        long totalPay = 0L;
        for (Sku sku : skus) {
            // 计算商品总价
            totalPay += sku.getPrice() * numMap.get(sku.getId());

            // 封装orderDetail
            OrderDetail detail = new OrderDetail();
            detail.setImage(StringUtils.substringBefore(sku.getImages(), ","));
            detail.setNum(numMap.get(sku.getId()));
            detail.setOrderId(orderId);
            detail.setOwnSpec(sku.getOwnSpec());
            detail.setPrice(sku.getPrice());
            detail.setSkuId(sku.getId());
            detail.setTitle(sku.getTitle());

            details.add(detail);
        }

        order.setTotalPay(totalPay);
        // 实付金额：总金额 + 邮费 - 优惠金额
        order.setActualPay(totalPay + order.getPostFee() - 0);
        // 将order写入数据库
        int count = orderMapper.insertSelective(order);
        if (count != 1) {
            log.error("[创建订单] 创建订单失败，orderId:{}", orderId);
            throw new SmException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        // 新增订单详情
        count = detailMapper.insertList(details);
        if (count != details.size()) {
            log.error("[创建订单] 创建订单失败，orderId:{}", orderId);
            throw new SmException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        // 新增订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.UN_PAY.value());
        count = statusMapper.insertSelective(orderStatus);
        if (count != 1) {
            log.error("[创建订单] 创建订单失败，orderId:{}", orderId);
            throw new SmException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        // 减库存
        List<CartDTO> cartDTOS = orderDTO.getOrderDetails();
        goodsClient.decreaseStock(cartDTOS);

        return orderId;
    }

    public Order queryOrderById(Long id) {
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            // 不存在
            throw new SmException(ExceptionEnum.ORDER_NOT_FOND);
        }
        // 查询订单详情
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(id);
        List<OrderDetail> details = detailMapper.select(detail);
        if (CollectionUtils.isEmpty(details)) {
            // 不存在
            throw new SmException(ExceptionEnum.ORDER_DETAIL_NOT_FOND);
        }
        order.setOrderDetails(details);
        // 查询订单状态
        OrderStatus orderStatus = statusMapper.selectByPrimaryKey(id);
        if (orderStatus == null) {
            // 不存在
            throw new SmException(ExceptionEnum.ORDER_STATUS_NOT_FOND);
        }
        order.setOrderStatus(orderStatus);

        return order;
    }

    public String createPayUrl(Long orderId) {
        // 查询订单
        Order order = queryOrderById(orderId);
        // 判断订单状态
        Integer status = order.getOrderStatus().getStatus();
        if (status != OrderStatusEnum.UN_PAY.value()) {
            // 订单状态异常
            throw new SmException(ExceptionEnum.ORDER_STATUS_ERROR);
        }
        // 支付金额
        Long actualPay = order.getActualPay();
        // 商品描述
        OrderDetail detail = order.getOrderDetails().get(0);
        String desc = detail.getTitle();

        return payHelper.createOrder(orderId, actualPay, desc);
    }

    @Transactional
    public void handleNotify(Map<String, String> result) {
        // 数据校验
        payHelper.isSuccess(result);

        // 校验签名
        payHelper.isValidSign(result);

        // 校验金额
        String totalFeeStr = result.get("total_fee");
        String tradeNo = result.get("out_trade_no");
        if (StringUtils.isEmpty(totalFeeStr) || StringUtils.isEmpty(tradeNo)) {
            throw new SmException(ExceptionEnum.INVALID_ORDER_PARAM);
        }
        // 获取结果中的金额
        Long totalFee = Long.valueOf(totalFeeStr);
        // 获取订单id
        Long orderId = Long.valueOf(tradeNo);
        // 获取订单金额
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (totalFee != order.getActualPay()) {
            // 金额不符
            throw new SmException(ExceptionEnum.INVALID_ORDER_PARAM);
        }

        // 修改订单状态
        OrderStatus status = new OrderStatus();
        status.setStatus(OrderStatusEnum.PAYED.value());
        status.setOrderId(orderId);
        status.setPaymentTime(new Date());

        int count = statusMapper.updateByPrimaryKeySelective(status);
        if (count != 1) {
            throw new SmException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
        }

        log.info("[订单回调] 订单支付成功！ 订单编号:{}", orderId);
    }

    public PayState queryOrderState(Long orderId) {
        OrderStatus orderStatus = statusMapper.selectByPrimaryKey(orderId);
        Integer status = orderStatus.getStatus();
        if (status != OrderStatusEnum.UN_PAY.value()) {
            // 已支付
            return PayState.SUCCESS;
        }

        // 未支付，去微信查找订单状态
        return payHelper.queryPayState(orderId);
    }
}
