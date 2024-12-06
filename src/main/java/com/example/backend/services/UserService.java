package com.example.backend.services;

import com.example.backend.DAO.UserRep;
import com.example.backend.models.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRep userRep;

    public Mono<Users> findByName(String name){
        return userRep.findByName(name);
    }

    public Mono<Users> save(Users users){
        return userRep.save(users);
    }
}
