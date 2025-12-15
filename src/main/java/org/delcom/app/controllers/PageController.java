package org.delcom.app.controllers;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.Food;
import org.delcom.app.entities.User;
import org.delcom.app.services.FoodService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.UUID; // Tambahkan import UUID

@Controller 
public class PageController {

    private final FoodService foodService;
    private final AuthContext authContext;
    
    public PageController(FoodService foodService, AuthContext authContext) {
        this.foodService = foodService;
        this.authContext = authContext;
    }

    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home(Model model) {
        
        if (!authContext.isAuthenticated()) {
            return "redirect:/auth/login"; 
        }

        User user = authContext.getAuthUser();
        
        // Cek defensif: jika user null atau ID-nya null (meskipun authenticated), log out
        UUID userId = (user != null) ? user.getId() : null;
        if (userId == null) {
            return "redirect:/auth/logout"; 
        }
        
        model.addAttribute("auth", user); 
        
        // Memanggil service dengan ID yang sudah dicek
        List<Food> foods = foodService.getAllFoods(userId, null);
        model.addAttribute("foods", foods);
        
        Map<String, Object> stats = foodService.getNutritionStatistics(userId);
        model.addAttribute("statistics", stats);

        return "pages/home";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "pages/login"; 
    }
    
    @GetMapping("/register")
    public String registerPage() {
        return "pages/register";
    }
}