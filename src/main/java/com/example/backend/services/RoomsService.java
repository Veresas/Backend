package com.example.backend.services;

import com.example.backend.DAO.RoomsRep;
import com.example.backend.models.Movies;
import com.example.backend.models.Rooms;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class RoomsService {

    private RoomsRep roomsRep;
    private ReactiveMongoTemplate mongoTemplate;

    @Autowired
    public RoomsService(RoomsRep roomsRep, ReactiveMongoTemplate mongoTemplate) {
        this.roomsRep = roomsRep;
        this.mongoTemplate = mongoTemplate;
    }

    public Flux<Rooms> findAllRooms() { return roomsRep.findAll(); }

    public Flux<Rooms> findAllRoomsById(List<String> ids){
        Query query = new Query();
        query.addCriteria(Criteria.where("id").in(ids));
        return mongoTemplate.find(query, Rooms.class);
    }

    public Mono<Rooms> createRoom(Rooms rooms){
        return  roomsRep.save(rooms);
    }

    public Mono<Void> deleteAllRooms() {
        return roomsRep.deleteAll();
    }

    public Mono<Void> deleteRoomsById(List<String> ids) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").in(ids)); // Указываем, что удаляем документы, где _id находится в списке ids
        return mongoTemplate.remove(query, Rooms.class).then();
    }

    public Mono<Void> addUsersInRooms(String roomId, List<String> userIds) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("userIdsAdd", userIds);
        return updateRoomById(roomId, updateData);
    }

    // Удаление пользователей из комнаты
    public Mono<Void> removeUsersFromRooms(String roomId, List<String> userIds) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("userIdsRemove", userIds);
        return updateRoomById(roomId, updateData);
    }

    // Обновление фильма в комнате
    public Mono<Void> updateMovieInRoom(String roomId, String movieId) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("movieId", movieId);
        return updateRoomById(roomId, updateData);
    }

    // Обновление владельца комнаты
    public Mono<Void> updateOwnerInRoom(String roomId, String ownerId) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("ownerId", ownerId);
        return updateRoomById(roomId, updateData);
    }

    public Mono<Void> updateRoomById(String id, Map<String, Object> updateData){
        return mongoTemplate.findById(id, Rooms.class)
                .flatMap(room -> {
                    if (updateData.containsKey("userIdsAdd")){
                        List<String> newUserIds = (List<String>) updateData.get("userIdsAdd");
                        Set<String> existingUserIds = room.getUserIds();

                        existingUserIds.addAll(newUserIds); // Добавляем новых пользователей
                        room.setUserIds(existingUserIds);
                    }

                    if (updateData.containsKey("userIdsRemove")){
                        List<String> newUserIds = (List<String>) updateData.get("userIdsRemove");
                        Set<String> existingUserIds = room.getUserIds();

                        existingUserIds.removeAll(newUserIds); // Добавляем новых пользователей
                        room.setUserIds(existingUserIds);
                    }

                    if (updateData.containsKey("movieId")){
                        room.setMovieId((String) updateData.get("movieId"));
                    }

                    if (updateData.containsKey("ownerId")){
                        room.setCreatedBy((String) updateData.get("ownerId"));
                    }

                    return mongoTemplate.save(room).then();
                });
    }
}
