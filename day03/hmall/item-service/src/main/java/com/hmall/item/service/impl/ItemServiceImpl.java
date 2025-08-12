package com.hmall.item.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.BeanUtils;
import com.hmall.item.domain.dto.ItemDTO;
import com.hmall.item.domain.dto.OrderDetailDTO;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.query.ItemPageQuery;
import com.hmall.item.mapper.ItemMapper;
import com.hmall.item.service.IItemService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author itheima
 */
@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements IItemService {

    @Override
    public void deductStock(List<OrderDetailDTO> items) {
        String sqlStatement = "com.hmall.item.mapper.ItemMapper.updateStock";
        boolean r = false;
        try {
            r = executeBatch(items, (sqlSession, entity) -> sqlSession.update(sqlStatement, entity));
            
        } catch (Exception e) {
            log.error("更新库存异常", e);
            throw new BizIllegalException("库存不足！");
        }
        if (!r) {
            throw new BizIllegalException("库存不足！");
        }
    }

    @Override
    public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
        return BeanUtils.copyList(listByIds(ids), ItemDTO.class);
    }
    @Override
    public PageDTO<ItemDTO> search(ItemPageQuery query) {
        LambdaQueryChainWrapper<Item> wrapper = lambdaQuery()
                .like(StrUtil.isNotBlank(query.getKey()), Item::getName, query.getKey())
                .eq(StrUtil.isNotBlank(query.getBrand()), Item::getBrand, query.getBrand())
                .eq(StrUtil.isNotBlank(query.getCategory()), Item::getCategory, query.getCategory())
                .ge(query.getMinPrice() != null, Item::getPrice, query.getMinPrice())
                .le(query.getMaxPrice() != null, Item::getPrice, query.getMaxPrice());
        if (query.getSortBy() !=null) {
            switch (query.getSortBy()){
                case "price":
                    wrapper.orderBy(true, query.getIsAsc(), Item::getPrice);
                    break;
                case "sold":
                    wrapper.orderBy(true, query.getIsAsc(), Item::getSold);
                    break;
                default:
                    wrapper.orderBy(true, query.getIsAsc(), Item::getUpdateTime);
                    break;
            }
        } else {
            wrapper.orderBy(true, query.getIsAsc(), Item::getUpdateTime);
        }
        Page<Item> itemPage = wrapper.page(new Page<>(query.getPageNo(), query.getPageSize()));

        return PageDTO.of(itemPage, ItemDTO.class);
    }

}
