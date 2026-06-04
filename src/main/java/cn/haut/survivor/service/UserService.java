package cn.haut.survivor.service;

import cn.haut.survivor.domain.entity.User;

public interface UserService {

    User register(String username, String password, String nickname);

    User login(String username, String password);

    User findById(Long id);

    boolean isAdmin(Long userId);
}
