package com.atguigu.gulimall.search.service;

import com.atguigu.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author tangyao
 * @version 1.0.0
 * @Description TODO
 * @createTime 2020年10月17日 14:27:00
 */
public interface ProductSaveService {

    Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;

}
