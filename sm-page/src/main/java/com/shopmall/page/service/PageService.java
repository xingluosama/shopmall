package com.shopmall.page.service;

import com.shopmall.item.pojo.*;
import com.shopmall.page.client.BrandClient;
import com.shopmall.page.client.CategoryClient;
import com.shopmall.page.client.GoodsClient;
import com.shopmall.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PageService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specClient;

    @Autowired
    private TemplateEngine templateEngine;

    public Map<String, Object> loadModel(Long spuId) {
        Map<String, Object> model = new HashMap<>();

        // 查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        // 查询skus
        List<Sku> skus = spu.getSkus();
        // 查询详情
        SpuDetail detail = spu.getSpuDetail();
        // 查询brand
        Brand brand = brandClient.queryById(spu.getBrandId());
        // 查询商品分类
        List<Category> categories = categoryClient.queryCategoryByIds(
                Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        // 查询规格参数
        List<SpecGroup> specs = specClient.queryGroupByCid(spu.getCid3());
        // 查询特殊的规格参数
        List<SpecParam> params = specClient.queryParamByList(null, spu.getCid3(), null);
        Map<Long, String> paramMap = new HashMap<>();
        params.forEach(param -> {
            paramMap.put(param.getId(), param.getName());
        });

        model.put("spu", spu);
        model.put("skus", skus);
        model.put("detail", detail);
        model.put("brand", brand);
        model.put("categories", categories);
        model.put("specs", specs);
        model.put("paramMap", paramMap);

        return model;
    }

    public void createHtml(Long spuId) {

        // 上下文
        Context context = new Context();
        context.setVariables(loadModel(spuId));

        // 输出流
        File dest = new File("D:\\IdeaProjects\\upload", spuId + ".html");

        if (dest.exists()) {
            dest.delete();
        }

        try (PrintWriter writer = new PrintWriter(dest, "UTF-8");){
            // 生成html
            templateEngine.process("item", context, writer);
        } catch (Exception e) {
            log.error("[静态页服务] 生成静态页异常", e);
        }
    }

    public void deleteHtml(Long spuId) {
        File dest = new File("D:\\IdeaProjects\\upload", spuId + ".html");
        if (dest.exists()) {
            dest.delete();
        }
    }
}
