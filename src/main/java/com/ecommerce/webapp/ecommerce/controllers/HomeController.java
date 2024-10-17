package com.ecommerce.webapp.ecommerce.controllers;

import ch.qos.logback.core.util.StringUtil;
import com.ecommerce.webapp.ecommerce.model.Category;
import com.ecommerce.webapp.ecommerce.model.Product;
import com.ecommerce.webapp.ecommerce.model.User;
import com.ecommerce.webapp.ecommerce.services.CartService;
import com.ecommerce.webapp.ecommerce.services.CategoryService;
import com.ecommerce.webapp.ecommerce.services.ProductService;
import com.ecommerce.webapp.ecommerce.services.UserService;
import com.ecommerce.webapp.ecommerce.util.CommonUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class HomeController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @ModelAttribute
    public void getUserDetails (Principal p, Model m){
        if (p != null){
            String mail = p.getName();
            User user = userService.getUserByEmail(mail);
            m.addAttribute("user", user);
            Integer countCart = cartService.getCountCart(user.getId());
            m.addAttribute("countCart", countCart);
        }

        List<Category> categories = categoryService.getAllActiveCategory();
        m.addAttribute("categories", categories);
    }

    @GetMapping("/")
    public String index (Model m){
        List<Category> allCategories = categoryService.getAllActiveCategory().stream().sorted((c1, c2) -> c2.getId().compareTo(c1.getId())).limit(6).toList();
        List<Product> allProduct = productService.getAllActiveProducts("").stream().sorted((p1, p2) -> p2.getId().compareTo(p1.getId())).limit(8).toList();

        m.addAttribute("category", allCategories);
        m.addAttribute("products", allProduct);
        return "index";
    }

    @GetMapping("/signin")
    public String login (){
        return "login";
    }

    @GetMapping("/register")
    public String register () {
        return "register";
    }

    @GetMapping("/products")
    public String products (Model m, @RequestParam (value = "category", defaultValue = "") String category,
                          @RequestParam  (name = "pageNo", defaultValue = "0") Integer pageNo,
                          @RequestParam (name = "pageSize", defaultValue = "12") Integer pageSize,
                          @RequestParam (defaultValue = "") String ch){

        List<Category> categories = categoryService.getAllActiveCategory();
        m.addAttribute("paramValue", category);
        m.addAttribute("categories", categories);

        Page<Product> page = null;

        if (StringUtils.isEmpty(ch)){
            page = productService.getAllActiveProductPagination(pageNo, pageSize, category);
        }else {
            page = productService.searchActiveProductPagination(pageNo, pageSize, category, ch);
        }

        List<Product> products = page.getContent();
        m.addAttribute("products", products);
        m.addAttribute("productSize", products.size());
        m.addAttribute("pageNo", page.getNumber());
        m.addAttribute("pageSize", pageSize);
        m.addAttribute("totalElements", page.getTotalElements());
        m.addAttribute("totalPages", page.getTotalPages());
        m.addAttribute("isFirst", page.isFirst());
        m.addAttribute("isLast", page.isLast());

        return "product";
    }

    @GetMapping("/product/{id}")
    public String product (@PathVariable int id, Model m) {
        Product productById = productService.getProductById(id);
        m.addAttribute("product", productById);

        return "view_product";
    }

    @PostMapping("/saveUser")
    public String  saveUser (@ModelAttribute User user,
                          @RequestParam("img")MultipartFile file,
                          HttpSession session) throws IOException {
        Boolean emailExists = userService.existsEmail(user.getEmail());

        if (emailExists){
            session.setAttribute("errMsg", "Email already Exists");
        }else {
            String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
            user.setProfileImage(imageName);

            User saveUser = userService.saveUser(user);

            if (!ObjectUtils.isEmpty(saveUser)){
                if (!file.isEmpty()){
                    File saveFile = new ClassPathResource("static/img").getFile();

                    Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator + file.getOriginalFilename());
                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                }
                session.setAttribute("succMsg", "Register Successfully");
            } else {
                session.setAttribute("errMsg", "Something went wrong");
            }
        }
        return "redirect:/register";
    }

    @GetMapping("/forget-password")
    public String showForgetPassword (){
        return "forget_password.html";
    }

    @PostMapping("/forget-password")
    public String processForgetPassword (@RequestParam String email,
                                       HttpSession session,
                                       HttpServletRequest request) throws UnsupportedEncodingException, MessagingException {
        User userByMail = userService.getUserByEmail(email);

        if (ObjectUtils.isEmpty(userByMail)){
            session.setAttribute("errMsg", "Invalid Email");
        } else {
            String resetToken = UUID.randomUUID().toString();
            userService.updateUserResetToken(email, resetToken);

            String url = CommonUtil.generateURL(request) + "/reset-password?token=" + resetToken;

            Boolean sendEmail = commonUtil.sendMail(url, email);

            if (sendEmail){
                session.setAttribute("succMsg", "Please check your email.. Password reset link sent");
            }else {
                session.setAttribute("errMsg", "Something went wrong ! Sending email failed");
            }
        }
        return "redirect:/forget-password";
    }

    @GetMapping("/reset-password")
    public String ShowResetPassword (@RequestParam String token, HttpSession session, Model m) {
        User userByToken = userService.getUserByToken(token);

        if (userByToken == null){
            m.addAttribute("msg", "your Link is Invalid or Expired !!");
            return "message";
        }
        m.addAttribute("token", token);
        return "/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword (@RequestParam String token, @RequestParam String password, HttpSession session , Model m) {
        User userByToken = userService.getUserByToken(token);

        if (userByToken == null){
            session.setAttribute("errMsg", "Your Link is Invalid or Expired !!");
            return "message";
        }else{
            userByToken.setPassword(bCryptPasswordEncoder.encode(password));
            userByToken.setResetToken(null);
            userService.updateUser(userByToken);

            m.addAttribute("msg", "Reset Password Successfully");

            return "message";
        }
    }

    @GetMapping("/search")
    public String  searchProduct (@RequestParam String ch, Model m){
        List<Product> products = productService.searchProduct(ch);
        m.addAttribute("products", products);

        List<Category> categories = categoryService.getAllActiveCategory();
        m.addAttribute("categories", categories);

        return "product";
    }
}
