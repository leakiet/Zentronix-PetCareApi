package com.greenkitchen.portal.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptPasswordGen {
  public static void main(String[] args) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    String encodedPassword = encoder.encode("123456");
    System.out.println(encodedPassword);
  }
}
