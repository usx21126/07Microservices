package com.itheima.mp.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.itheima.mp.domain.dto.UserFormDTO;
import com.itheima.mp.domain.po.User;
import com.itheima.mp.domain.query.UserQuery;
import com.itheima.mp.domain.vo.UserVO;
import com.itheima.mp.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@Api(tags="用户接口管理")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    @ApiOperation("新增用户")
    @PostMapping
    public void addUser(@RequestBody UserFormDTO userFormDTO) {
        User user = BeanUtil.copyProperties(userFormDTO, User.class);
        userService.save(user);
    }

    @ApiOperation("删除用户")
    @DeleteMapping
    public void deleteUser(@PathVariable Long id) {
        userService.removeById(id);
    }

    @ApiOperation("根据id查询用户")
    @GetMapping("/{id}")
    public UserVO getUserById(@PathVariable Long id) {
//        return BeanUtil.copyProperties(userService.getById(id),UserVO.class);
        return userService.queryUserAndAddressById(id);
    }

    @GetMapping
    @ApiOperation("根据id批量查询用户")
    public List<UserVO> getUserByIds(@RequestParam("ids") List<Long> ids) {
//        return BeanUtil.copyToList(userService.listByIds(ids),UserVO.class);
        return userService.queryUserAndAddressByIds(ids);
    }

    @ApiOperation("根据id扣减余额")
    @PostMapping("/{id}/deduction/{amount}")
    public void updateBalanceById(@PathVariable Long id, @PathVariable Integer amount) {
        userService.deductBalanceById(id,amount);
    }

    @ApiOperation("根据条件查询用户列表")
    @PostMapping("/list")
    public List<UserVO> queryList(@RequestBody UserQuery userQuery) {
        String name = userQuery.getName();
        Integer status = userQuery.getStatus();
        Integer maxBalance = userQuery.getMaxBalance();
        Integer minBalance = userQuery.getMinBalance();
//        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.like(StrUtil.isNotBlank(name),User::getUsername,name)
//                .eq(status != null,User::getStatus,status)
//                .ge(minBalance != null,User::getBalance,minBalance)
//                .le(maxBalance != null,User::getBalance,maxBalance);
//
//        List<User> list = userService.list(queryWrapper);
        List<User> list = userService.lambdaQuery()
                .like(StrUtil.isNotBlank(name), User::getUsername, name)
                .eq(status != null, User::getStatus, status)
                .le(maxBalance != null, User::getBalance, maxBalance)
                .ge(minBalance != null, User::getBalance, minBalance).list();
        return BeanUtil.copyToList(list,UserVO.class);
    }

}
