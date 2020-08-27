package com.shopmall.cart.service;

import com.shopmall.auth.entity.UserInfo;
import com.shopmall.cart.client.GoodsClient;
import com.shopmall.cart.interceptor.UserInterceptor;
import com.shopmall.cart.pojo.Cart;
import com.shopmall.common.enums.ExceptionEnum;
import com.shopmall.common.exception.SmException;
import com.shopmall.common.utils.JsonUtils;
import com.shopmall.item.pojo.Sku;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate template;

    @Autowired
    private GoodsClient goodsClient;

    private static final String KEY_PREFIX = "sm:cart:uid:";

    public void addCart(Cart cart) {
        // 获取登录用户
        UserInfo user = UserInterceptor.getUser();
        // key
        String key = KEY_PREFIX + user.getId();
        // skuId
        Long skuId = cart.getSkuId();
        // hashKey
        String hashKey = skuId.toString();
        // 记录num
        Integer num = cart.getNum();

        BoundHashOperations<String, Object, Object> operation = template.boundHashOps(key);
        // 判断当前购物车商品是否存在
        if (operation.hasKey(hashKey)) {
            // 存在，修改数量
            String json = operation.get(hashKey).toString();
            cart = JsonUtils.toBean(json, Cart.class);
            cart.setNum(cart.getNum() + num);
        } else {
            // 不存在，新增购物车数据
            cart.setUserId(user.getId());
            // 其它商品信息， 需要查询商品服务
            Sku sku = this.goodsClient.querySkuById(skuId);
            cart.setImage(StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);
            cart.setPrice(sku.getPrice());
            cart.setTitle(sku.getTitle());
            cart.setOwnSpec(sku.getOwnSpec());
        }

        // 写回redis
        operation.put(hashKey, JsonUtils.toString(cart));
    }

    public List<Cart> queryCartList() {
        // 获取登录用户
        UserInfo user = UserInterceptor.getUser();
        // key
        String key = KEY_PREFIX + user.getId();
        if (!template.hasKey(key)) {
            // key不存在，返回404
            throw new SmException(ExceptionEnum.CART_NOT_FOND);
        }

        //获取登录用户的所有购物车
        BoundHashOperations<String, Object, Object> operation = template.boundHashOps(key);

        List<Cart> carts = operation.values().stream().map(o -> JsonUtils.toBean(o.toString(), Cart.class))
                .collect(Collectors.toList());

        return carts;
    }

    public void updateNum(Long skuId, Integer num) {
        // 获取登录用户
        UserInfo user = UserInterceptor.getUser();
        // key
        String key = KEY_PREFIX + user.getId();
        // hashKey
        String hashKey = skuId.toString();
        // 获取操作
        BoundHashOperations<String, Object, Object> operation = template.boundHashOps(key);

        // 判断是否存在
        if (!operation.hasKey(hashKey)) {
            throw new SmException(ExceptionEnum.CART_NOT_FOND);
        }

        // 查询购物车
        Cart cart = JsonUtils.toBean(operation.get(hashKey).toString(), Cart.class);
        cart.setNum(num);

        // 写回redis
        operation.put(hashKey, JsonUtils.toString(cart));
    }

    public void deleteCart(Long skuId) {
        // 获取登录用户
        UserInfo user = UserInterceptor.getUser();
        // key
        String key = KEY_PREFIX + user.getId();

        // 删除
        template.opsForHash().delete(key, skuId.toString());
    }
}
