package com.shopmall.search.repository;

import com.shopmall.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface GoodsRepository extends ElasticsearchRepository<Goods, Long> {
}
