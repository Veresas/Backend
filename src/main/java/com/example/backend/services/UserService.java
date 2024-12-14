package com.example.backend.services;

import com.example.backend.DAO.UserRep;
import com.example.backend.models.Users;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.mongodb.core.query.Query;
import java.util.List;

@Service
public class UserService {

    private UserRep userRep;
    private ReactiveMongoTemplate mongoTemplate;

    @Autowired
    public UserService(UserRep userRep, ReactiveMongoTemplate reactiveMongoTemplate){
        this.userRep = userRep;
        this.mongoTemplate = reactiveMongoTemplate;
    }

    public Mono<Users> findByName(String name){
        return userRep.findByName(name);
    }

    public Mono<Users> findById(String id){
        ObjectId mongoId = new ObjectId(id);
        return userRep.findById(mongoId);
    }

    public Mono<Users> save(Users users){
        return userRep.save(users);
    }

    public Mono<Void> findByIdAndReplace(Users users){
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(users.getId()));
        return mongoTemplate.replace(query,users).then();
    }

    public Mono<Users> update(Users users) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(users.getId()));
        return mongoTemplate.findOne(query,Users.class)
                .flatMap(existingUsers -> {
                    existingUsers.setId(users.getId());
                    existingUsers.setEmail(users.getEmail());
                    existingUsers.setName(users.getName());
                    existingUsers.setFilms(users.getFilms());
                    existingUsers.setPassword(users.getPassword());
                    existingUsers.setRole(users.getRole());

                    return mongoTemplate.save(existingUsers);
                });
    }

    public Mono<Void> deleteAll(){
        return userRep.deleteAll();
    }
    public Flux<Users> findAll(){
        return userRep.findAll();
    }

    public Mono<Void> addFilmToUsers(String idUser, String idFilm){
        Query query = Query.query(Criteria.where("_id").is(idUser));
        Update update = new Update().push("films", idFilm);
        return mongoTemplate.updateFirst(query,update,Users.class)
                .flatMap(res -> {
                    if(res.getModifiedCount() == 0){
                        return Mono.error(new IllegalArgumentException("User not found"));
                    }
                    return Mono.empty();
                });
    }
}
