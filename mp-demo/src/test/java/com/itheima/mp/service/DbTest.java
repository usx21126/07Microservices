package com.itheima.mp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.itheima.mp.domain.po.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class DbTest {
    // 1.根据id查用户
    @Test
    public void testQueryById() {
        User user = Db.getById(1L,User.class);
        System.out.println(user);
    }
    // 2.查询名字包含o且余额大于1000
    @Test
    public void testQueryByNameAndBalance() {
//        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>(User.class);
//        wrapper.like(User::getUsername,"o")
//                .ge(User::getBalance,1000);
//        List<User> userList = Db.list(wrapper);
        List<User> userList = Db.lambdaQuery(User.class)
                .like(User::getUsername,"o")
                .ge(User::getBalance,1000)
                .list();

        for (User user : userList) {
            System.out.println(user);
        }
    }
    // 3.更新Rose用户余额为2000
    @Test
    public void testUpdate() {
//        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>(User.class);
//        wrapper.eq(User::getUsername,"Rose");
//        User user = new User();
//        user.setBalance(2000);
//        Db.update(user,wrapper);
        Db.lambdaUpdate(User.class)
                .set(User::getBalance,2000)
                .set(User::getUsername,"Rose")
                .update();
    }
}
