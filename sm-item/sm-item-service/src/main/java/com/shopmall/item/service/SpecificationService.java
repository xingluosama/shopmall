package com.shopmall.item.service;

import com.shopmall.common.enums.ExceptionEnum;
import com.shopmall.common.exception.SmException;
import com.shopmall.item.mapper.SpecGroupMapper;
import com.shopmall.item.mapper.SpecParamMapper;
import com.shopmall.item.pojo.SpecGroup;
import com.shopmall.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationService {

    @Autowired
    private SpecGroupMapper groupMapper;

    @Autowired
    private SpecParamMapper paramMapper;

    public List<SpecGroup> queryGroupByCid(Long cid) {
        SpecGroup group = new SpecGroup();
        group.setCid(cid);
        List<SpecGroup> list = groupMapper.select(group);
        if (CollectionUtils.isEmpty(list)) {
            // 没查到
            throw new SmException(ExceptionEnum.SPEC_GROUP_NOT_FOND);
        }
        return list;
    }

    public List<SpecParam> queryParamList(Long gid, Long cid, Boolean searching) {
        SpecParam param = new SpecParam();
        param.setGroupId(gid);
        param.setCid(cid);
        param.setSearching(searching);

        List<SpecParam> list = paramMapper.select(param);
        if (CollectionUtils.isEmpty(list)) {
            // 没查到
            throw new SmException(ExceptionEnum.SPEC_PARAM_NOT_FOND);
        }
        return list;
    }

    public List<SpecGroup> queryListByCid(Long cid) {

        // 查询规格组
        List<SpecGroup> specGroups = queryGroupByCid(cid);

        // 查询分类下的参数
        List<SpecParam> specParams = queryParamList(null, cid, null);

        // 将规格参数变成map，map的key是规格组id，map的值是组下的所有参数
        Map<Long, List<SpecParam>> map = new HashMap<>();
        for (SpecParam param : specParams) {
            Long groupId = param.getGroupId();
            if (!map.containsKey(groupId)) {
                // 当前组id在map中不存在，新增一个list
                map.put(groupId, new ArrayList<>());
            }
            map.get(groupId).add(param);
        }

        // 填充param到group
        for (SpecGroup specGroup : specGroups) {
            specGroup.setParams(map.get(specGroup.getId()));
        }

        return specGroups;
    }
}
