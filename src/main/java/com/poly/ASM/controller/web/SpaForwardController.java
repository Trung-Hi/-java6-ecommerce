package com.poly.ASM.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({
            "/",
            "/home/**",
            "/product/**",
            "/cart/**",
            "/order/**",
            "/account/**",
            "/auth/**",
            "/admin/**"
    })
    public String forwardToVueApp() {
        return "forward:/app/index.html";
    }
}
