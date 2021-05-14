package org.fhi360.ddd.service;

public interface SecurityService {
    String findLoggedInUsername();

    void autoLogin(String username, String password);
}
