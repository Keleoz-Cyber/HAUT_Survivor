package cn.haut.survivor.service.impl;

import cn.haut.survivor.domain.entity.User;
import cn.haut.survivor.domain.enums.UserRole;
import cn.haut.survivor.mapper.UserMapper;
import cn.haut.survivor.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public User register(String username, String password, String nickname) {
        String normalizedUsername = requireText(username, "用户名不能为空");
        String normalizedPassword = requireText(password, "密码不能为空");
        if (normalizedPassword.length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于 6 位");
        }
        if (findByUsername(normalizedUsername) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setPassword(hashPassword(normalizedPassword));
        user.setNickname(StringUtils.hasText(nickname) ? nickname.trim() : normalizedUsername);
        user.setRole(UserRole.USER.name());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        return user;
    }

    @Override
    public User login(String username, String password) {
        String normalizedUsername = requireText(username, "用户名不能为空");
        String normalizedPassword = requireText(password, "密码不能为空");
        User user = findByUsername(normalizedUsername);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (!hashPassword(normalizedPassword).equals(user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        return user;
    }

    @Override
    public User findById(Long id) {
        if (id == null) {
            return null;
        }
        return userMapper.selectById(id);
    }

    @Override
    public boolean isAdmin(Long userId) {
        User user = findById(userId);
        return user != null && UserRole.ADMIN.name().equals(user.getRole());
    }

    private User findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .last("LIMIT 1"));
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }
}
