package com.work.api.controller;


import com.google.common.base.Strings;
import com.work.api.domain.User;
import com.work.api.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Date;

@RestController
@ResponseBody
public class LoginController {
    private static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    UserRepository userRepository;

    @GetMapping(value = "/api/login")
    public ResponseEntity<?> login(@Valid User user) {
        if (Strings.isNullOrEmpty(user.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("name is empty");
        }
        if (Strings.isNullOrEmpty(user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("password is empty");
        }

        User loginUser = userRepository.findByName(user.getName());
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("name is not exist");
        }

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        if (!bCryptPasswordEncoder.matches(user.getPassword(),loginUser.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("password is not match");
        }

        long nowMillis = System.currentTimeMillis();

        String jwtString = Jwts.builder()
                .setId(String.valueOf(loginUser.getId()))
                .setIssuer(loginUser.getName())
                .setIssuedAt(new Date(nowMillis))
                .setExpiration(new Date(nowMillis + (1000 * 60 * 5)))
                .setSubject("accepted")
                .signWith(SignatureAlgorithm.HS256, "psalm0105")
                .compact();

        logger.info(jwtString);
        loginUser.setToken(jwtString);

        return ResponseEntity.ok(loginUser);
    }

}
