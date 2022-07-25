package org.fhi360.ddd;

import org.fhi360.ddd.utils.DataLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class DddApplication implements CommandLineRunner {
    @Autowired
    DataLoader dataLoader;

    @Override
    public void run(String... args) throws Exception {
        dataLoader.saveRegimen();
    }

    public static void main(String[] args) {
        SpringApplication.run(DddApplication.class, args);

    }


}




