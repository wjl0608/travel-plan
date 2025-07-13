package com.fawnyr.travelplanbackend.service.impl;

import com.fawnyr.travelplanbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Test
    void userRegister() {
        userService.userRegister("Fawnyr","12345678","12345678");
    }
}