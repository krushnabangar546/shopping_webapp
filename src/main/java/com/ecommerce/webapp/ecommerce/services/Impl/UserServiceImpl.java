package com.ecommerce.webapp.ecommerce.services.Impl;

import com.ecommerce.webapp.ecommerce.model.User;
import com.ecommerce.webapp.ecommerce.repositories.UserRepository;
import com.ecommerce.webapp.ecommerce.services.UserService;
import com.ecommerce.webapp.ecommerce.util.AppConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Override
    public User saveUser(User user) {
        user.setRole("ROLE_USER");
        user.setEnable(true);
        user.setAccountNonLocked(true);
        user.setFailedAttempts(0);

        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
        User saveUser = userRepository.save(user);
        return saveUser;
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> getUsers(String role) {
        return userRepository.findByRole(role);
    }

    @Override
    public Boolean updateAccountStatus(Integer id, Boolean status) {
        Optional<User> findByUser = userRepository.findById(id);
        if (findByUser.isPresent()){
            User user = findByUser.get();
            user.setEnable(status);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public void increaseFailedAttempt(User user) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);
        userRepository.save(user);
    }

    @Override
    public void userAccountLocked(User user) {
        user.setAccountNonLocked(false);
        user.setLockDate(new Date());
        userRepository.save(user);
    }

    @Override
    public boolean unlockAccountTimeExpired(User user) {
        long lockTime = user.getLockDate().getTime();
        long unlockTime = lockTime = AppConstant.UNLOCK_DURATION_TIME;
        long currentTime = System.currentTimeMillis();

        if (unlockTime < currentTime) {
            user.setAccountNonLocked(true);
            user.setFailedAttempts(0);
            user.setLockDate(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public void resetAttempt(int userId) {

    }

    @Override
    public void updateUserResetToken(String email, String resetToken) {
        User user = userRepository.findByEmail(email);
        user.setResetToken(resetToken);
        userRepository.save(user);
    }

    @Override
    public User getUserByToken(String token) {
        return userRepository.findByResetToken(token);
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User updateUserProfile(User user, MultipartFile img) {
        User user1 = userRepository.findById(user.getId()).get();
        if (!img.isEmpty()){
            user1.setProfileImage(img.getOriginalFilename());
        }

        if (!ObjectUtils.isEmpty(user1)){
            user1.setName(user.getName());
            user1.setMobileNumber(user.getMobileNumber());
            user1.setAddress(user.getAddress());
            user1.setCity(user.getCity());
            user1.setState(user.getState());
            user1.setPinCode(user.getPinCode());

            user1 = userRepository.save(user1);
        }
        try {
            if (!img.isEmpty()){
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator + img.getOriginalFilename());

                Files.copy(img.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user1;
    }

    @Override
    public User saveAdmin(User user) {
        user.setRole("ROLE_ADMIN");
        user.setEnable(true);
        user.setAccountNonLocked(true);
        user.setFailedAttempts(0);

        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
        User saveUser = userRepository.save(user);
        return saveUser;
    }

    @Override
    public Boolean existsEmail(String email) {
        return existsEmail(email);
    }
}
