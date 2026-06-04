package cn.haut.survivor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("cn.haut.survivor.mapper")
@SpringBootApplication
public class HautSurvivorApplication {

    public static void main(String[] args) {
        SpringApplication.run(HautSurvivorApplication.class, args);
    }
}
