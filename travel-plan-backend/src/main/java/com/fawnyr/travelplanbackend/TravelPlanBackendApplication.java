package com.fawnyr.travelplanbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@MapperScan("com.fawnyr.travelplanbackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication(exclude = PgVectorStoreAutoConfiguration.class)
public class TravelPlanBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelPlanBackendApplication.class, args);
    }

}
