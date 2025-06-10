package com.capgemini.test.code.service.impl;

import com.capgemini.test.code.clients.CheckDniRequest;
import com.capgemini.test.code.clients.CheckDniResponse;
import com.capgemini.test.code.clients.DniClient;
import com.capgemini.test.code.clients.NotificationClient;
import com.capgemini.test.code.model.dto.*;
import com.capgemini.test.code.model.entity.Rooms;
import com.capgemini.test.code.model.entity.User;
import com.capgemini.test.code.model.repository.RoomRepository;
import com.capgemini.test.code.model.repository.UserRepository;
import com.capgemini.test.code.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private DniClient dniClient;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationClient notificationClient;

    @Override
    public UserResponse createUser(UserRequest request) {

        // 1. Busco el email, si lo encuentro lanzo excepcion por duplicado
        if (userRepository.findByEmail(request.getEmail()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error validation email");

        // 2. Valido DNI llamando a mock-server, si es el malo lanzara excepcion Feign
        ResponseEntity<CheckDniResponse> dniResponse = dniClient.check(new CheckDniRequest(request.getDni()));

        // 3. Busco la sala, si no la encuentra lanzo excepcion
        Optional<Rooms> optionalRoom = roomRepository.findById(1L);
        if (optionalRoom.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala no encontrada");
        Rooms room = optionalRoom.get();

        //Lanzo excepciones por duplicidad de telefono y dni para capturarlos en el globalexception
        if (userRepository.findByDni(request.getDni()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error duplicate dni");
        if (userRepository.findByPhone(request.getPhone()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error duplicate phone");

        //4. Mapeo el dto a entidad
        User user = userMapper.toEntity(request);

        //Seteo la sala -> siempre la 1 segun el enunciado
        user.setRoom(room);

        //5 . Guardo el usuario
        User savedUser = userRepository.save(user);

        // 6 . Notificacion segun el rol del usuario
        String mensaje = "usuario guardado";
        if ("admin".equals(user.getRole()))
            notificationClient.sendEmail(new EmailNotificationRequest(user.getEmail(), mensaje));
        if ("superadmin".equals(user.getRole()))
            notificationClient.sendSms(new SmsNotificationRequest(user.getPhone(), mensaje));

        // 7. Devuelvo el ID
        return userMapper.toUserResponse(savedUser);

    }

    @Override
    public UserDetailResponse getUserById(Long id) {

        // Busco el id, sino está lanzo excepcion
        User user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un usuario con ese id"));
        //Si lo encuentro devuelvo todos los detalles del user
        return userMapper.toUserDetail(user);
    }
}
