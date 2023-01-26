package org.fhi360.ddd;

import org.fhi360.ddd.utils.DataLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class DddApplication implements CommandLineRunner {
   // @Autowired
    //DataLoader dataLoader;

    @Override
    public void run(String... args) throws Exception {
      //  dataLoader.saveRegimen();
    }




    public static void main(String[] args) {
        SpringApplication.run(DddApplication.class, args);

    }



//    public static boolean isPalladrome(String val) {
//        String resvers = "";
//        boolean result = false;
//        for (int i = val.length() - 1; i >= 0; i--) {
//            resvers = resvers + val.charAt(i);
//        }
//        if (val.equals(resvers)) {
//            result = true;
//        }
//        return result;
//    }
//
//    public static void main(String[] args) {
//
//        String str = "madam";
//
//        str = str.toLowerCase();
//        boolean A = isPalindrome(str);
//        System.out.println(A);
//    }

//
//    public static void main(String args[]) {
//        int i, m = 0, result = 0;
//        int n = 3;
//        m = n / 2;
//        if (n == 0 || n == 1) {
//            System.out.println(n + " is not prime number");
//        } else {
//            for (i = 2; i <= m; i++) {
//                if (n % i == 0) {
//                    System.out.println(n + " is not prime number");
//                    result = 1;
//                    break;
//                }
//            }
//            if (result == 0) {
//                System.out.println(n + " is prime number");
//            }
//        }
//    }

}




