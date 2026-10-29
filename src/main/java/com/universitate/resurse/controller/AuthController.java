
package com.universitate.resurse.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/auth")
    public String authPage() {
        return "redirect:/login";
    }
}
