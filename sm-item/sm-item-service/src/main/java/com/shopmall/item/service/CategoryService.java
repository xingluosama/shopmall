package com.shopmall.item.service;

import com.shopmall.common.enums.ExceptionEnum;
import com.shopmall.common.exception.SmException;
import com.shopmall.item.mapper.CategoryMapper;
import com.shopmall.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    public List<Category> queryCategoryListByPid(Long pid) {
        // 查询条件，mapper会把对象中的非空属性作为查询条件
        Category t = new Category();
        t.setParentId(pid);
        List<Category> list = categoryMapper.select(t);
        // 判断结果
        if (CollectionUtils.isEmpty(list)) {
            throw new SmException(ExceptionEnum.CATEGORY_NOT_FOND);
        }
        return list;
    }

    public List<Category> queryByIds(List<Long> ids) {
        List<Category> list = categoryMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new SmException(ExceptionEnum.CATEGORY_NOT_FOND);
        }
        return list;
    }
}
