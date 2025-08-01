package com.itheima.mp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mp.domain.po.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class UserServiceTest {
    @Autowired
    private IUserService userService;
    //测试分页
    @Test
    public void testPage() {
        // 1.创建分页对象
        Page<User> page = new Page<User>(2, 2);
        // 2.分页查询
        Page<User> userPage = userService.page(page);
        System.out.println("总页数："+userPage.getPages());
        System.out.println("总记录数"+userPage.getTotal());
        List<User> userList = userPage.getRecords();
        for(User user : userPage.getRecords()) {
            System.out.println(user);
        }
    }
}
