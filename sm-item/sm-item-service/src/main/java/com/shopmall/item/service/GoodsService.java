package com.shopmall.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.shopmall.common.dto.CartDTO;
import com.shopmall.common.enums.ExceptionEnum;
import com.shopmall.common.exception.SmException;
import com.shopmall.common.vo.PageResult;
import com.shopmall.item.mapper.SkuMapper;
import com.shopmall.item.mapper.SpuDetailMapper;
import com.shopmall.item.mapper.SpuMapper;
import com.shopmall.item.mapper.StockMapper;
import com.shopmall.item.pojo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper detailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<Spu> queryByPage(Integer page, Integer rows, Boolean saleable, String key) {

        // 分页
        PageHelper.startPage(page, rows);
        // 过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        // 搜索字段过滤
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        // 上下架过滤
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        // 默认排序
        example.setOrderByClause("last_update_time DESC");

        // 查询
        List<Spu> spus = spuMapper.selectByExample(example);
        // 判断
        if (CollectionUtils.isEmpty(spus)) {
            throw new SmException(ExceptionEnum.GOODS_NOT_FOND);
        }

        // 解析分类和品牌的名称
        loadCategoryAndBrandName(spus);

        // 解析分页结果
        PageInfo<Spu> info = new PageInfo<>(spus);
        return new PageResult<>(info.getTotal(), spus);
    }

    private void loadCategoryAndBrandName(List<Spu> spus) {
        for (Spu spu : spus) {
            // 处理分类名称
            List<String> names = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names, "/"));
            // 处理品牌名称
            spu.setBname(brandService.queryById(spu.getBrandId()).getName());
        }
    }

    @Transactional
    public void saveGoods(Spu spu) {
        // 新增spu
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(false);

        int count = spuMapper.insert(spu);
        if (count != 1) {
            throw new SmException(ExceptionEnum.GOODS_SAVE_ERROR);
        }

        // 新增 detail
        SpuDetail detail = spu.getSpuDetail();
        detail.setSpuId(spu.getId());

        count = detailMapper.insert(detail);
        if (count != 1) {
            throw new SmException(ExceptionEnum.GOODS_SAVE_ERROR);
        }

        // 新增sku和库存
        saveSkuAndStock(spu);

        // 发送mq消息
        sendMessage(spu.getId(), "insert");
    }

    private void sendMessage(Long id, String type) {
        // 发送mq消息
        try {
            this.amqpTemplate.convertAndSend("item." + type, id);
        } catch (AmqpException e) {
            log.error("{}商品消息发送异常，商品id：{}", type, id, e);
        }
    }

    private void saveSkuAndStock(Spu spu) {
        int count;
        // 定义库存的集合
        List<Stock> stockList = new ArrayList<>();

        // 新增sku
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());

            count = skuMapper.insert(sku);
            if (count != 1) {
                throw new SmException(ExceptionEnum.GOODS_SAVE_ERROR);
            }

            // 新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());

            stockList.add(stock);
        }

        // 批量新增库存
        count = stockMapper.insertList(stockList);
        if (count != stockList.size()) {
            throw new SmException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
    }

    public SpuDetail queryDetailById(Long spuId) {
        SpuDetail detail = detailMapper.selectByPrimaryKey(spuId);
        if (detail == null) {
            throw new SmException(ExceptionEnum.GOODS_DETAIL_NOT_FOND);
        }
        return detail;
    }

    public List<Sku> querySkuBySpuId(Long spuId) {
        // 查询sku
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skuList = skuMapper.select(sku);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new SmException(ExceptionEnum.GOODS_SKU_NOT_FOND);
        }

        // 查询库存并封装到skuList中
        loadStockInSku(skuList);
        return skuList;
    }

    private void loadStockInSku(List<Sku> skuList) {
        //查询库存
        List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
        List<Stock> stockList = stockMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(stockList)) {
            throw new SmException(ExceptionEnum.GOODS_STOCK_NOT_FOND);
        }

        // 将stockList变为map，key为skuId，值为库存值
        Map<Long, Integer> stockMap = stockList.stream()
                .collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));

        skuList.forEach(s -> s.setStock(stockMap.get(s.getId())));
    }

    @Transactional
    public void updateGoods(Spu spu) {
        if (spu.getId() == null) {
            throw new SmException(ExceptionEnum.GOODS_ID_CANNOT_BE_NULL);
        }
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());

        // 查询sku
        List<Sku> skuList = skuMapper.select(sku);
        if (!CollectionUtils.isEmpty(skuList)) {
            // 删除sku
            skuMapper.delete(sku);

            // 删除stock
            List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(ids);
        }

        // 修改spu
        spu.setValid(null);
        spu.setSaleable(null);
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);

        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1) {
            throw new SmException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }

        // 修改detail
        count = detailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if (count != 1) {
            throw new SmException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }

        // 新增sku和stock
        saveSkuAndStock(spu);

        // 发送mq消息
        sendMessage(spu.getId(), "update");
    }

    public Spu querySpuById(Long id) {
        // 查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new SmException(ExceptionEnum.GOODS_NOT_FOND);
        }

        // 查询sku
        spu.setSkus(querySkuBySpuId(id));

        // 查询detail
        spu.setSpuDetail(queryDetailById(id));

        return spu;
    }

    public List<Sku> querySkuByIds(List<Long> ids) {
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skus)) {
            throw new SmException(ExceptionEnum.GOODS_SKU_NOT_FOND);
        }

        // 查询库存并封装到skuList中
        loadStockInSku(skus);
        return skus;
    }

    public Sku querySkuById(Long id) {
        Sku sku = skuMapper.selectByPrimaryKey(id);
        if (sku == null) {
            throw new SmException(ExceptionEnum.GOODS_SKU_NOT_FOND);
        }
        return sku;
    }

    @Transactional
    public void decreaseStock(List<CartDTO> carts) {
        for (CartDTO cart : carts) {
            // 减库存
            int count = stockMapper.decreaseStock(cart.getSkuId(), cart.getNum());
            if (count != 1) {
                throw new SmException(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
        }
    }
}
