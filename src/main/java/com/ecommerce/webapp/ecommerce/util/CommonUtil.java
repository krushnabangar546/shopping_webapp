package com.ecommerce.webapp.ecommerce.util;

import com.ecommerce.webapp.ecommerce.model.ProductOrder;
import com.ecommerce.webapp.ecommerce.model.User;
import com.ecommerce.webapp.ecommerce.services.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.http.HttpRequest;
import java.security.Principal;

@Component
public class CommonUtil {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private UserService userService;

    public Boolean sendMail (String url, String reciepentEMail) throws UnsupportedEncodingException, MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

        helper.setFrom("bangark501@gmail.com", "Shopping Cart");
        helper.setTo(reciepentEMail);

        String content = "<p>Hello,</p>" + "<p>You have requested to reset your password.</p>"
                + "<p>Click the link below to change your password:</p>" + "<p><a href=\"" + url
                + "\">Change my password</a></p>";

        helper.setSubject("Password Reset");
        helper.setText(content);

        javaMailSender.send(mimeMessage);

        return true;
    }

    public static String generateURL (HttpServletRequest request) {
        String siteURL  = request.getRequestURL().toString();
        return siteURL.replace(request.getServletPath(), "");
    }

    String msg = null;

    public Boolean sendMailForProductOrder (ProductOrder order, String status) throws Exception {

        msg="<p>Hello [[name]],</p>"
                + "<p>Thank you order <b>[[orderStatus]]</b>.</p>"
                + "<p><b>Product Details:</b></p>"
                + "<p>Name : [[productName]]</p>"
                + "<p>Category : [[category]]</p>"
                + "<p>Quantity : [[quantity]]</p>"
                + "<p>Price : [[price]]</p>"
                + "<p>Payment Type : [[paymentType]]</p>";

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("bangark501@gmail.com", "Shopping Cart");
        helper.setTo(order.getOrderAddress().getEmail());

        msg = msg.replace("[[name]]", order.getOrderAddress().getFirstName());
        msg = msg.replace("[[orderStatus]]", status);
        msg = msg.replace("[[productName]]", order.getProduct().getTitle());
        msg = msg.replace("[[category]]", order.getProduct().getCategory());
        msg = msg.replace("[[quantity]]", order.getQuantity().toString());
        msg = msg.replace("[[price]]", order.getPrice().toString());
        msg = msg.replace("[[paymentType]]", order.getPaymentType());

        helper.setSubject("Product Order Status");
        helper.setText(msg,true);

        javaMailSender.send(message);
        return  true;
    }

    public User getLoggedInUserDetails (Principal p){
        String email = p.getName();
        User user = userService.getUserByEmail(email);

        return user;
    }
}
