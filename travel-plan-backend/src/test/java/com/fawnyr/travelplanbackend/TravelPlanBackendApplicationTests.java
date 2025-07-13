package com.fawnyr.travelplanbackend;

import com.fawnyr.travelplanbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class TravelPlanBackendApplicationTests {

    @Autowired
    private UserService userService;

    @Test
    public void findTotal(){
        Long total = userService.count();
        System.out.println(total);
    }

}
