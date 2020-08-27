package com.shopmall.item.api;

import com.shopmall.common.dto.CartDTO;
import com.shopmall.common.vo.PageResult;
import com.shopmall.item.pojo.Sku;
import com.shopmall.item.pojo.Spu;
import com.shopmall.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GoodsApi {

    /**
     * 根据spu的id查询详情detail
     * @param spuId
     * @return
     */
    @GetMapping("/spu/detail/{id}")
    SpuDetail queryDetailById(@PathVariable("id") Long spuId);

    /**
     * 根据spu查询下面的所有sku
     * @param spuId
     * @return
     */
    @GetMapping("/sku/list")
    List<Sku> querySkuBySpuId(@RequestParam("id") Long spuId);

    /**
     * 分页查询spu
     * @param page
     * @param rows
     * @param saleable
     * @param key
     * @return
     */
    @GetMapping("/spu/page")
    PageResult<Spu> queryByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "key", required = false) String key
    );

    /**
     * 根据spu的id查询spu
     * @param id
     * @return
     */
    @GetMapping("/spu/{id}")
    Spu querySpuById(@PathVariable("id") Long id);

    /**
     * 根据spu的id查询sku
     * @param id
     * @return
     */
    @GetMapping("sku/{id}")
    Sku querySkuById(@PathVariable("id") Long id);

    /**
     * 根据spu的id集合查询所有sku
     * @param ids
     * @return
     */
    @GetMapping("/sku/list/ids")
    List<Sku> querySkuByIds(@RequestParam("ids") List<Long> ids);

    /**
     * 减少库存
     * @param carts
     * @return
     */
    @PostMapping("stock/decrease")
    Void decreaseStock(@RequestBody List<CartDTO> carts);
}
