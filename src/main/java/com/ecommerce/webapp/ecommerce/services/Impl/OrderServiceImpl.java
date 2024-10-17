package com.ecommerce.webapp.ecommerce.services.Impl;

import com.ecommerce.webapp.ecommerce.model.Cart;
import com.ecommerce.webapp.ecommerce.model.OrderAddress;
import com.ecommerce.webapp.ecommerce.model.OrderRequest;
import com.ecommerce.webapp.ecommerce.model.ProductOrder;
import com.ecommerce.webapp.ecommerce.repositories.CartRepository;
import com.ecommerce.webapp.ecommerce.repositories.ProductOrderRepository;
import com.ecommerce.webapp.ecommerce.services.OrderService;
import com.ecommerce.webapp.ecommerce.util.CommonUtil;
import com.ecommerce.webapp.ecommerce.util.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ProductOrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CommonUtil commonUtil;
    @Override
    public void saveOrder(Integer userid, OrderRequest orderRequest) throws Exception {
        List<Cart> carts = cartRepository.findByUserId(userid);

        for (Cart cart : carts) {
            ProductOrder order = new ProductOrder();

            order.setOrderId(UUID.randomUUID().toString());
            order.setOrderDate(LocalDate.now());
            order.setProduct(cart.getProduct());
            order.setPrice(cart.getProduct().getDiscountPrice());
            order.setQuantity(cart.getQuantity());
            order.setUser(cart.getUser());
            order.setStatus(OrderStatus.IN_PROGRESS.getName());
            order.setPaymentType(orderRequest.getPaymentType());


            OrderAddress address = new OrderAddress();

            address.setFirstName(orderRequest.getFirstName());
            address.setLastName(orderRequest.getLastName());
            address.setEmail(orderRequest.getEmail());
            address.setMobileNumber(orderRequest.getMobileNo());
            address.setAddress(orderRequest.getAddress());
            address.setCity(orderRequest.getCity());
            address.setState(orderRequest.getState());
            address.setPinCode(orderRequest.getPinCode());

            order.setOrderAddress(address);

            ProductOrder saveOrder = orderRepository.save(order);

            commonUtil.sendMailForProductOrder(saveOrder, "Success");
        }
    }

    @Override
    public List<ProductOrder> getOrdersByUser(Integer userId) {
        List<ProductOrder> orders = orderRepository.findByUserId(userId);
        return orders;
    }

    @Override
    public ProductOrder updateOrderStatus(Integer id, String status) {

        Optional<ProductOrder> findById = orderRepository.findById(id);

        if (findById.isPresent()){
            ProductOrder productOrder = findById.get();
            productOrder.setStatus(status);

            ProductOrder updateOrder = orderRepository.save(productOrder);
            return updateOrder;
        }
        return null;
    }

    @Override
    public List<ProductOrder> getAllOrders() {
        return  orderRepository.findAll();
    }

    @Override
    public ProductOrder getOrdersByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId);
    }

    @Override
    public Page<ProductOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo,pageSize);
        return orderRepository.findAll(pageable);
    }
}
