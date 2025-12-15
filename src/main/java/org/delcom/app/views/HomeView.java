package org.delcom.app.views;

import org.delcom.app.dto.FoodForm;
import org.delcom.app.entities.User;
import org.delcom.app.services.FoodService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class HomeView {

    private final FoodService foodService;

    public HomeView(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping
    public String home(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/auth/login";
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/login";
        }

        User authUser = (User) principal;
        model.addAttribute("auth", authUser);

        // Foods - semua makanan user
        var foods = foodService.getAllFoods(authUser.getId(), "");
        model.addAttribute("foods", foods);

        // Statistics untuk dashboard chart - dengan default empty map
        Map<String, Object> statistics;
        try {
            statistics = foodService.getNutritionStatistics(authUser.getId());
            if (statistics == null) {
                statistics = createEmptyStatistics();
            }
        } catch (Exception e) {
            statistics = createEmptyStatistics();
        }
        model.addAttribute("statistics", statistics);

        // Food Form untuk modal add
        model.addAttribute("foodForm", new FoodForm());

        return ConstUtil.TEMPLATE_PAGES_HOME;
    }

    // Helper method untuk create empty statistics
    private Map<String, Object> createEmptyStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("caloriesByCategory", new HashMap<String, Double>());
        stats.put("averageNutrition", new HashMap<String, Double>());
        stats.put("countByCategory", new HashMap<String, Long>());
        return stats;
    }
}