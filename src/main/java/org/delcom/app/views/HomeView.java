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
            return "redirect:/auth/logout";
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/logout";
        }

        User authUser = (User) principal;
        model.addAttribute("auth", authUser);

        // Foods - semua makanan user
        var foods = foodService.getAllFoods(authUser.getId(), "");
        model.addAttribute("foods", foods);

        // Statistics untuk dashboard chart
        var statistics = foodService.getNutritionStatistics(authUser.getId());
        model.addAttribute("statistics", statistics);

        // Food Form untuk modal add
        model.addAttribute("foodForm", new FoodForm());

        return ConstUtil.TEMPLATE_PAGES_HOME;
    }
}

