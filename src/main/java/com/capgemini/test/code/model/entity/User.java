package com.capgemini.test.code.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true)
    private String email;
    @Column(unique = true)
    private String dni;
    @Column(unique = true)
    private String phone;
    private String role;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Rooms room;
}
