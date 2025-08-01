package com.itheima.mp.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.itheima.mp.domain.po.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class WrapperTest {
    @Autowired
    private UserMapper userMapper;

    /**
     * 查询username包含o且balance>=1000的用户
     */
    @Test
    public void testQueryWrapper() {
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.select("id","username","info","balance")
                .like("username","o")
                .ge("balance",1000);
        List<User> userList = userMapper.selectList(userQueryWrapper);
        userList.forEach(System.out::println);
    }
    /**
     * 更新用户名jack的余额为2000
     */
    @Test
    public void testQueryWrapper2() {
        User user = new User();
        user.setBalance(2000);

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("username","jack");
        userMapper.update(user,userQueryWrapper);

    }

    /**
     * 更新id为1，2，4的用户余额，-200
     */
    @Test
    public void testUpdateWrapper() {
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id",1,2,4);
        updateWrapper.setSql("balance = balance - 200");
        userMapper.update(null,updateWrapper);
    }

    @Test
    public void testLambdaUpdateWrapper() {
        LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.setSql("balance = balance - 200");
        lambdaUpdateWrapper.in(User::getId,1,2,4);
        userMapper.update(null,lambdaUpdateWrapper);
    }

    @Test
    public void testCustomSqlSeqment() {
        LambdaUpdateWrapper<User> userLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userLambdaUpdateWrapper.in(User::getId,List.of(1L,2L,4L));

        userMapper.updateBalanceByWrapper(200,userLambdaUpdateWrapper);
    }
}
