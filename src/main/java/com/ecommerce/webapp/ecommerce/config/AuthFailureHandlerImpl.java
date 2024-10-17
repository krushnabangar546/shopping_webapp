package com.ecommerce.webapp.ecommerce.config;

import com.ecommerce.webapp.ecommerce.model.User;
import com.ecommerce.webapp.ecommerce.repositories.UserRepository;
import com.ecommerce.webapp.ecommerce.services.UserService;
import com.ecommerce.webapp.ecommerce.util.AppConstant;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationFailure (@NotNull HttpServletRequest request,
                                         HttpServletResponse response,
                                         AuthenticationException exception) throws IOException, ServletException {

        String email =  request.getParameter("username");
        User user = userRepository.findByEmail(email);

        if ( user != null ){
            if (user.isEnable()){
                if (user.isAccountNonLocked()){
                    if (user.getFailedAttempts() < AppConstant.ATTEMPT_TIME){
                        userService.increaseFailedAttempt(user);
                    } else {
                        userService.userAccountLocked(user);
                        exception = new LockedException("Your Account is Locked !! failed attempt 3.");
                    }
                } else {
                    if (userService.unlockAccountTimeExpired(user)){
                        exception = new LockedException("Your Account is UnLocked !! please try to login.");
                    } else {
                        exception = new LockedException("Your Account is Locked !! please try after sometime.");
                    }
                }
            } else {
                exception = new LockedException("Your Account is inActive");
            }
        } else {
            exception = new LockedException("email & password inValid");
        }

        super.setDefaultFailureUrl("/signin?error");
        super.onAuthenticationFailure(request, response, exception);
    }

}
