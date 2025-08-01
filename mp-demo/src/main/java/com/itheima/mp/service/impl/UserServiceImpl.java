package com.itheima.mp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.itheima.mp.domain.po.Address;
import com.itheima.mp.domain.po.User;
import com.itheima.mp.domain.vo.AddressVO;
import com.itheima.mp.domain.vo.UserVO;
import com.itheima.mp.enums.UserStatus;
import com.itheima.mp.mapper.UserMapper;
import com.itheima.mp.service.IUserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService  {
    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void deductBalanceById(Long id, Integer amount) {
        // 1.查询用户
        User user = getById(id);
        if(user != null && user.getStatus()== UserStatus.FREEZE) {
            throw new RuntimeException("非合法用户");
        }
        // 2.判断金额
        if (user.getBalance() < amount) {
            throw new RuntimeException("余额不足");
        }
        // 3.扣减
        int remainBalance = user.getBalance() - amount;
        lambdaUpdate()
                .set(User::getBalance, remainBalance)
                .set(remainBalance==0,User::getStatus,2)
                .eq(User::getId, id)
                .update();
//        userMapper.deductBalanceById(id,amount);
    }

    @Override
    public UserVO queryUserAndAddressById(Long id) {
        User user = getById(id);
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);

        List<Address> addressList = Db.lambdaQuery(Address.class)
                .eq(Address::getUserId, id)
                .list();
        List<AddressVO> addressVOList = BeanUtil.copyToList(addressList, AddressVO.class);
        userVO.setAddresses(addressVOList);
        return userVO;
    }

    @Override
    public List<UserVO> queryUserAndAddressByIds(List<Long> ids) {
        List<User> userList = lambdaQuery().in(User::getId, ids).list();
        List<UserVO> userVOList = BeanUtil.copyToList(userList, UserVO.class);

//        for(UserVO userVO : userVOList) {
//            List<Address> addressList = Db.lambdaQuery(Address.class).eq(Address::getUserId, userVO.getId()).list();
//            List<AddressVO> addressVOList = BeanUtil.copyToList(addressList, AddressVO.class);
//            userVO.setAddresses(addressVOList);
//        }
        List<Address> addressList = Db.lambdaQuery(Address.class)
                .in(Address::getUserId, ids)
                .list();
        List<AddressVO> addressVOList = BeanUtil.copyToList(addressList, AddressVO.class);
        Map<Long,List<AddressVO>> userAddressMap = addressVOList.stream().collect(Collectors.groupingBy(AddressVO::getUserId));
        for(UserVO userVO:userVOList){
            userVO.setAddresses(userAddressMap.get(userVO.getId()));
        }
        return userVOList;
    }
}
