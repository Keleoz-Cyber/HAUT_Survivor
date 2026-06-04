package cn.haut.survivor.controller;

import cn.haut.survivor.config.LoginInterceptor;
import cn.haut.survivor.domain.entity.User;
import cn.haut.survivor.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model
    ) {
        try {
            User user = userService.login(username, password);
            session.setAttribute(LoginInterceptor.LOGIN_USER_ID, user.getId());
            session.setAttribute(LoginInterceptor.LOGIN_USER_ROLE, user.getRole());
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String nickname,
            HttpSession session,
            Model model
    ) {
        try {
            User user = userService.register(username, password, nickname);
            session.setAttribute(LoginInterceptor.LOGIN_USER_ID, user.getId());
            session.setAttribute(LoginInterceptor.LOGIN_USER_ROLE, user.getRole());
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("nickname", nickname);
            return "auth/register";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
