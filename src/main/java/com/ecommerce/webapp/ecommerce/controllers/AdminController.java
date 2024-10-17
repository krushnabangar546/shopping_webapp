package com.ecommerce.webapp.ecommerce.controllers;

import com.ecommerce.webapp.ecommerce.model.Category;
import com.ecommerce.webapp.ecommerce.model.User;
import com.ecommerce.webapp.ecommerce.services.*;
import com.ecommerce.webapp.ecommerce.util.CommonUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @ModelAttribute
    public void getUserDetails(Principal p, Model m){
        if (p != null){
            String email = p.getName();
            User user = userService.getUserByEmail(email);
            m.addAttribute("user", user);

            Integer countCart =  cartService.getCountCart(user.getId());
            m.addAttribute("countCart", countCart);
        }
        List<Category> categoryList = categoryService.getAllActiveCategory();
        m.addAttribute("category", categoryList);
    }

    @GetMapping("/")
    public String index(){
        return "admin/index";
    }

    @GetMapping("/loadAddProduct")
    public String loadAllProduct(Model m){
        List<Category> categories = categoryService.getAllCategory();
        m.addAttribute("categories", categories);
        return "admin/add_product";
    }

    @GetMapping("/category")
    public String category(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize){
        Page<Category> categoryPage = categoryService.getAllCategorPagination(pageNo, pageSize);
        List<Category> categories = categoryPage.getContent();

        m.addAttribute("pageNo", categoryPage.getNumber());
        m.addAttribute("pageSize", pageSize);
        m.addAttribute("totalElements", categoryPage.getTotalElements());
        m.addAttribute("totalPages", categoryPage.getTotalPages());
        m.addAttribute("isFirst", categoryPage.isFirst());
        m.addAttribute("isLast", categoryPage.isLast());

        return "admin/category";
    }

    @PostMapping("/saveCategory")
    public String saveCategory (@ModelAttribute Category category, @RequestParam("file")MultipartFile file, HttpSession session) throws IOException {

        String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
        category.setImageName(imageName);

        Boolean existsCategory = categoryService.existCategory(category.getName());

        if (existsCategory){
            session.setAttribute("errMsg", "Category Name already exists");
        } else {
            Category saveCategory = categoryService.saveCategory(category);
            if (ObjectUtils.isEmpty(saveCategory)){
                session.setAttribute("errMsg", "Not saved ! internal server error");
            } else {
                File saveFile =  new ClassPathResource("static/img").getFile();

                Path path  = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator + file.getOriginalFilename());

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                session.setAttribute("succMsg", "Saved Successfully");
            }
        }
        return "redirect:/admin/category";
    }

    @GetMapping("/deleteCategory/{id}")
    public String deleteCategory(@PathVariable int id, HttpSession session){
        Boolean deleteCategory = categoryService.deleteCategory(id);
        if (deleteCategory){
            session.setAttribute("succMsg", "category deleted Successfully");
        }else {
            session.setAttribute("errMsg", "Something wrong in server");
        }
        return "redirect:/admin/category";
    }

    @GetMapping("/loadEditCategory/{id}")
    public String loadEditCategory (@PathVariable int id, Model m) {
        m.addAttribute("category", categoryService.getCategoryById(id));
        return "admin/edit_category";
    }
}
