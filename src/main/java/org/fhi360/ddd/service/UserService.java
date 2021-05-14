package org.fhi360.ddd.service;


import org.fhi360.ddd.domain.User;

public interface UserService {
    void save(User user);

    User findByUsername(String username);
}
