package com.graddu.rilaserver.controller;

import com.graddu.rilaserver.dto.UserDto;
import com.graddu.rilaserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class LoginPageController {
    
    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    // 登录页面映射已移至AuthController，避免冲突
    // @GetMapping("/login")
    // public String loginPage() {
    //     return "login";
    // }

    @GetMapping("/index")
    public String indexPage() {
        return "index";
    }

    @GetMapping("/users")
    public String usersPage(Model model) {
        List<UserDto> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "users";
    }
} 