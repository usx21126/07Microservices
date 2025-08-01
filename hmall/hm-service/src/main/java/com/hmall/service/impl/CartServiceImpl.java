package com.hmall.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.BeanUtils;
import com.hmall.common.utils.CollUtils;
import com.hmall.common.utils.UserContext;
import com.hmall.domain.dto.CartFormDTO;
import com.hmall.domain.dto.ItemDTO;
import com.hmall.domain.po.Cart;
import com.hmall.domain.po.Item;
import com.hmall.domain.vo.CartVO;
import com.hmall.mapper.CartMapper;
import com.hmall.service.ICartService;
import com.hmall.service.IItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

    private final IItemService itemService;

    @Override
    public void addItem2Cart(CartFormDTO cartFormDTO) {
        
        Cart cart = lambdaQuery()
                .eq(Cart::getItemId, cartFormDTO.getItemId())
                .eq(Cart::getUserId, UserContext.getUser())
                .one();
        if (cart == null) {
            Long count = lambdaQuery().eq(Cart::getUserId, UserContext.getUser()).count();
            if (count >= 10) {
                throw new BizIllegalException("购物车商品数量超过限制！");
            } else {
                Cart newCart = BeanUtil.copyProperties(cartFormDTO, Cart.class);
                newCart.setUserId(UserContext.getUser());
                newCart.setNum(1);
                newCart.setCreateTime(LocalDateTime.now());
                newCart.setUpdateTime(LocalDateTime.now());
                this.save(newCart);
            }
        } else {
            lambdaUpdate().eq(Cart::getId,cart.getId()).setSql("num = num + 1").update();
        }
    }

    @Override
    public List<CartVO> queryMyCarts() {
        List<Cart> cartList = lambdaQuery().eq(Cart::getUserId, UserContext.getUser()).list();
        if(!CollUtils.isEmpty(cartList)){
            List<CartVO> cartVOList = BeanUtils.copyToList(cartList, CartVO.class);

            //2、设置商品的最新价格、状态、库存等信息
            //2.1、收集商品id集合
            List<Long> itemIdList = cartVOList.stream().map(CartVO::getItemId).collect(Collectors.toList());

            //2.2、根据商品id集合批量查询商品

            Map<Long, Item> itemMap = itemService.listByIds(itemIdList).stream().collect(Collectors.toMap(Item::getId, Function.identity()));

            //2.3、遍历每个购物车商品，设置商品属性
            cartVOList.forEach(cartVO -> {
                Item item = itemMap.get(cartVO.getItemId());
                cartVO.setPrice(item.getPrice());
                cartVO.setStatus(item.getStatus());
                cartVO.setStock(item.getStock());
            });

            return cartVOList;
        }

        return CollUtils.emptyList();
    }

    @Override
    public void removeByItemIds(Collection<Long> itemIds) {
        // 1.构建删除条件，userId和itemId
        QueryWrapper<Cart> queryWrapper = new QueryWrapper<Cart>();
        queryWrapper.lambda()
                .eq(Cart::getUserId, UserContext.getUser())
                .in(Cart::getItemId, itemIds);
        // 2.删除
        remove(queryWrapper);
    }
}
