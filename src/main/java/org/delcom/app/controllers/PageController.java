package org.delcom.app.controllers;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.dto.FoodForm;
import org.delcom.app.entities.Food;
import org.delcom.app.entities.User;
import org.delcom.app.services.FoodService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    public String home(Model model, HttpServletRequest request) {
        
        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return "redirect:/auth/login"; 
        }

        User user = authContext.getAuthUser();
        
        // Cek defensif: jika user null atau ID-nya null
        if (user == null || user.getId() == null) {
            return "redirect:/auth/logout"; 
        }
        
        UUID userId = user.getId();
        model.addAttribute("auth", user);
        
        // PENTING: Kirim currentPath untuk navbar active state
        model.addAttribute("currentPath", request.getRequestURI());
        
        // Ambil semua foods
        List<Food> foods = foodService.getAllFoods(userId, "");
        model.addAttribute("foods", foods);
        
        // Force empty statistics untuk test
        Map<String, Object> statistics = createEmptyStatistics();
        model.addAttribute("statistics", statistics);
        
        // Food Form untuk modal add
        model.addAttribute("foodForm", new FoodForm());

        // PENTING: Gunakan konstanta dari ConstUtil
        return ConstUtil.TEMPLATE_PAGES_HOME;  // "pages/home"
    }

    // Helper method untuk create empty statistics
    private Map<String, Object> createEmptyStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("caloriesByCategory", new HashMap<String, Double>());
        stats.put("averageNutrition", new HashMap<String, Double>());
        stats.put("countByCategory", new HashMap<String, Long>());
        return stats;
    }

    @GetMapping("/login")
    public String loginPage() {
        return ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN; 
    }
    
    @GetMapping("/register")
    public String registerPage() {
        return ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER;
    }
}