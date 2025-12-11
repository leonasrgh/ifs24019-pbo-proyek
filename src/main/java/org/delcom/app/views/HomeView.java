package org.delcom.app.views;

import org.delcom.app.dto.RecipeForm;
import org.delcom.app.entities.User;
import org.delcom.app.services.RecipeService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeView {

    private final RecipeService recipeService;

    public HomeView(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping
    public String home(Model model, @RequestParam(required = false) String search) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Cek Login
        if (authentication instanceof AnonymousAuthenticationToken || authentication == null) {
            return "redirect:/auth/login";
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/logout";
        }

        User authUser = (User) principal;
        model.addAttribute("auth", authUser);

        // 1. Ambil Daftar Resep
        var recipes = recipeService.getAllRecipes(authUser.getId(), search);
        model.addAttribute("recipes", recipes);

        // 2. Ambil Statistik untuk CHART (Poin Penilaian No. 8)
        var stats = recipeService.getStats(authUser.getId());
        model.addAttribute("stats", stats);

        // 3. Form untuk Modal Tambah Resep
        model.addAttribute("recipeForm", new RecipeForm());

        return ConstUtil.TEMPLATE_PAGES_HOME;
    }
}