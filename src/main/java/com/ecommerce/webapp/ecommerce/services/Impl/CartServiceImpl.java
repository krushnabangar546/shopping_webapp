package com.ecommerce.webapp.ecommerce.services.Impl;

import com.ecommerce.webapp.ecommerce.model.Cart;
import com.ecommerce.webapp.ecommerce.model.Product;
import com.ecommerce.webapp.ecommerce.model.User;
import com.ecommerce.webapp.ecommerce.repositories.CartRepository;
import com.ecommerce.webapp.ecommerce.repositories.ProductRepository;
import com.ecommerce.webapp.ecommerce.repositories.UserRepository;
import com.ecommerce.webapp.ecommerce.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Cart saveCart(Integer productId, Integer userId) {

        User user = userRepository.findById(userId).get();
        Product product = productRepository.findById(productId).get();
        Cart cartStatus = cartRepository.findByProductIdAndUserId(productId, userId);

        Cart cart = null;

        if (ObjectUtils.isEmpty(cartStatus)){

            cart = new Cart();
            cart.setProduct(product);
            cart.setUser(user);
            cart.setQuantity(1);
            cart.setTotalPrice(1 * product.getDiscountPrice());
        } else {
            cart = cartStatus;
            cart.setQuantity(cart.getQuantity() + 1);
            cart.setTotalPrice(cart.getQuantity() * cart.getProduct().getDiscountPrice());
        }

        Cart saveCart = cartRepository.save(cart);

        return saveCart;
    }

    @Override
    public List<Cart> getCartsByUser(Integer userId) {
        List<Cart> carts = cartRepository.findByUserId(userId);

        Double totalOrderPrice = 0.0;
        List<Cart> updateCarts = new ArrayList<>();
        for (Cart c : carts){
            Double totalPrice = (c.getProduct().getDiscountPrice() * c.getQuantity());
            c.setTotalPrice(totalPrice);

            totalOrderPrice += totalPrice;
            c.setTotalOrderPrice(totalOrderPrice);

            updateCarts.add(c);
        }
        return updateCarts;
    }

    @Override
    public Integer getCountCart(Integer userId) {

        Integer countByUserId = cartRepository.countByUserId(userId);
        return countByUserId;
    }

    @Override
    public void updateQuantity(String sy, Integer cid) {
        Cart cart = cartRepository.findById(cid).get();

        int updateQueantity;
        if (sy.equalsIgnoreCase("de")){
            updateQueantity = cart.getQuantity() - 1;

            if (updateQueantity <= 0) {
                cartRepository.delete(cart);
            } else {
                cart.setQuantity(updateQueantity);
                cartRepository.save(cart);
            }
        } else {
            updateQueantity = cart.getQuantity() + 1;
            cart.setQuantity(updateQueantity);
            cartRepository.save(cart);
        }
    }
}
