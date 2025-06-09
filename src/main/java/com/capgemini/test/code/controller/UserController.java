package com.capgemini.test.code.controller;

import com.capgemini.test.code.model.dto.UserDetailResponse;
import com.capgemini.test.code.model.dto.UserRequest;
import com.capgemini.test.code.model.dto.UserResponse;
import com.capgemini.test.code.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    //Post para CREAR usuario
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest request) {

        //si tod0 ok me lo crea
        UserResponse userResponse = userService.createUser(request);
        //Devuelvo un http 201(creado) y devuelvo en el cuerpo el dto que devuelve solo el id
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    //Get para OBTENER usuario
    @GetMapping("/{id}")
    public ResponseEntity<UserDetailResponse> getUserById(@PathVariable Long id) {
        //El servicio valida el id y me devuelve el usuario por id
        UserDetailResponse userDetailResponse = userService.getUserById(id);
        //Devuelvo un http 200 con el usuario entero
        return ResponseEntity.ok(userDetailResponse);
    }
}
