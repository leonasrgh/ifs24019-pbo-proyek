package org.delcom.app.utils;

public class ConstUtil {
    // Key untuk penyimpanan token di Cookie / Session
    public static final String KEY_AUTH_TOKEN = "AUTH_TOKEN";
    public static final String KEY_USER_ID = "USER_ID";

    // Lokasi file HTML (Thymeleaf Templates)
    
    
    public static final String TEMPLATE_PAGES_AUTH_LOGIN = "pages/auth/login";
    public static final String TEMPLATE_PAGES_AUTH_REGISTER = "pages/auth/register";
    
    public static final String TEMPLATE_PAGES_HOME = "pages/recipes/home"; // Halaman utama resep
    public static final String TEMPLATE_PAGES_RECIPES_DETAIL = "pages/recipes/detail"; // Detail resep
    public static final String TEMPLATE_PAGES_RECIPES_ADD = "models/recipes/add"; // Modal tambah
    public static final String TEMPLATE_PAGES_RECIPES_EDIT = "models/recipes/edit"; // Modal edit
}