package com.capgemini.test.code.model.repository;

import com.capgemini.test.code.model.entity.Rooms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Rooms, Long> {

}
