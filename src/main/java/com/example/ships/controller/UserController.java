package com.example.ships.controller;

import com.example.ships.model.Role;
import com.example.ships.service.UserService;
import com.example.ships.model.User;
import com.example.ships.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Controller
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }


    @PostMapping("/register")
    public String registerUser(User user, Model model) {
        if (userService.isUserEmailUnique(user.getEmail())) {
            Set<Role> roles = new HashSet<>();
            roles.add(Role.USER);
            if (userService.isAdmin(user.getEmail())) {
                roles.add(Role.ADMIN);
            }
            user.setId(null);
            userService.saveUser(user, roles);
            model.addAttribute("message", "Registration successful!");
            return "redirect:/login";
        } else {
            model.addAttribute("error", "User with this email already exists. Please use a different email.");
            return "register";
        }
    }

    @PostMapping("/login")
    public String loginUser(User user, Model model, HttpServletRequest request) {
        if (userService.isValidUser(user)) {
            User loggedInUser = userService.getUserByEmail(user.getEmail());
            if (loggedInUser != null) {
                String token = jwtUtil.generateToken(loggedInUser.getEmail());

                Boolean isAdmin = userService.isAdminRole(loggedInUser.getRoles());
                System.out.println(isAdmin);
                HttpSession session = request.getSession();
                session.setAttribute("username", loggedInUser.getUsername());
                session.setAttribute("token", token);
                session.setAttribute("user_id", loggedInUser.getId());

                if (isAdmin) {
                    session.setAttribute("isAdmin", true);
                    return "redirect:/admin";
                } else {
                    return "redirect:/";
                }
            }
        }
        model.addAttribute("error", "Invalid credentials. Please try again.");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

}
