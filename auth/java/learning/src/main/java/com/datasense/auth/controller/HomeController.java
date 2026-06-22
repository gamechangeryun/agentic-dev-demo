package com.datasense.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 루트(`/`)·`/signup`을 회원가입 OTP 정적 화면으로 포워딩한다. */
@Controller
public class HomeController {

    /** `/` 와 `/signup` → static/signup.html. REST `/auth/**` 계약에는 영향 없음. */
    @GetMapping({"/", "/signup"})
    public String signupScreen() {
        return "forward:/signup.html";
    }
}
