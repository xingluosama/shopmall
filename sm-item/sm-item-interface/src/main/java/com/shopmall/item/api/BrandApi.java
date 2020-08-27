package com.shopmall.item.api;

import com.shopmall.item.pojo.Brand;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface BrandApi {

    @GetMapping("brand/{id}")
    Brand queryById(@PathVariable("id") Long id);

    @GetMapping("brand/list")
    List<Brand> queryBrandByIds(@RequestParam("ids") List<Long> ids);
}
