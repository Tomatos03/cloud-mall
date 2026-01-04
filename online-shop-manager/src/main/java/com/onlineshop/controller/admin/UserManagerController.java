package com.onlineshop.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.onlineshop.framework.models.user.User;
import com.onlineshop.framework.models.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 *
 * @author : Tomatos
 * @date : 2025/12/20
 */
@RestController
@RequestMapping("/manager/admin/user")
public class UserManagerController {
    @Autowired
    private IUserService userService;

    @PostMapping
    public boolean createUser(@RequestBody User user) {
        return userService.save(user);
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.list();
    }

    @PutMapping
    public boolean updateUser(@RequestBody User user) {
        return userService.updateById(user);
    }

    @DeleteMapping("/{id}")
    public boolean deleteUser(@PathVariable Long id) {
        return userService.removeById(id);
    }

    @GetMapping("/page")
    public IPage<User> getUsersPage(
            @RequestParam(defaultValue = "1", name = "page") int current,
            @RequestParam(defaultValue = "10", name = "pageSize") int size
    ) {
        return userService.getUsersPage(current, size);
    }
}
