package com.guilhermebraga.agendamento_api.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginWebController {

    @GetMapping("/web/login")
    public String loginPage() {
        return "web/login";
    }
}
