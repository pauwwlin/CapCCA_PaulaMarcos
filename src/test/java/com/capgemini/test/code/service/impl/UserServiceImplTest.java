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
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private DniClient dniClient;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private UserMapper userMapper;

    @Test
    void createUser_Created() {

        //estructura GIVEN-WHEN-THEN
        // GIVEN

        //Creo el DTO como lo recibiria el controlador
        UserRequest request = new UserRequest();
        request.setName("xxxx");
        request.setEmail("test@email.com");
        request.setDni("12345678A");
        request.setPhone("600000000");
        request.setRole("admin");

        //Simulo condiciones validas: que el email no existe, dni es valido, la sala 1 existe
        Mockito.when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        Mockito.when(dniClient.check(any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        Rooms room = new Rooms();
        room.setId(1L);
        Mockito.when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        //Mapeo de dto a entidad, simulo como convierte el userrequest en user
        User userEntity = new User();
        userEntity.setName("admin");
        userEntity.setEmail("test@email.com");
        userEntity.setDni("12345678A");
        userEntity.setPhone("600000000");
        userEntity.setRole("admin");
        userEntity.setRoom(room);
        Mockito.when(userMapper.toEntity(request)).thenReturn(userEntity);

        //Guardado en BD
        User savedUser = new User();
        savedUser.setId(99L); //seteo un id
        Mockito.when(userRepository.save(userEntity)).thenReturn(savedUser);

        //Mapeo de entidad a userResponse
        UserResponse userResponse = new UserResponse();
        userResponse.setId(99L);
        Mockito.when(userMapper.toUserResponse(savedUser)).thenReturn(userResponse);

        //WHEN
        UserResponse response = userService.createUser(request);

        //THEN
        assertNotNull(response); //Compruebo que no devuelva null, es decir se construyo y devuelve un userresponse
        assertEquals(99L, response.getId()); //Compruebo que el id devuelto es 99

        //Verifico notificacion por email porque es admin
        Mockito.verify(notificationClient).sendEmail(Mockito.any());

    }

    @Test
    void createUser_DuplicateEmail() {

        //Creo un dto y seteo un email
        UserRequest userRequest = new UserRequest();
        userRequest.setEmail("test@email.com");

        //Simulo que ya hay un usuario ya guardado en la base de datos con el mismo email
        User existingUser = new User();
        existingUser.setEmail("test@email.com");

        //Simulo que cuando el respositorio llame al metodo con ese mail, devuelva un Optional
        Mockito.when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(existingUser));

        // Verifico que al llamar a createuser se lanza una excpecion
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.createUser(userRequest));

        //Compruebo que a parte de que se ha lanzado la excepcion, el mensaje es el correcto
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        // Verifico que no se llegó a guardar nada, que nunca llamo al save()
        Mockito.verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_InvalidDni() {
        // GIVEN
        UserRequest request = new UserRequest();
        request.setEmail("nuevo@email.com");
        request.setDni("99999999W");
        request.setRole("admin");

        // Mockeo el el correo que devuelva vacio, esta ok
        Mockito.when(userRepository.findByEmail("nuevo@email.com")).thenReturn(Optional.empty());

        // Simulamos que el servicio de DNI devuelve un error 409
        Request feignRequest = Request.create(Request.HttpMethod.POST, "http://localhost:8080/api/dni/check", Map.of(), "DNI inválido".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

        Mockito.when(dniClient.check(any(CheckDniRequest.class))).thenThrow(new FeignException.Conflict("DNI inválido", feignRequest, null, null));

        // WHEN & THEN
        FeignException.Conflict exception = assertThrows(FeignException.Conflict.class, () -> userService.createUser(request));

        // Verificaciones adicionales
        assertEquals("DNI inválido", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toEntity(any());
    }

    @Test
    void createUser_RoomNoExist() {

        //Creo dto y simulo una peticion valida
        UserRequest request = new UserRequest();
        request.setEmail("nuevo@email.com");
        request.setDni("12345678A");
        request.setRole("admin");

        // El email no está duplicado y el DNI es valido
        Mockito.when(userRepository.findByEmail("nuevo@email.com")).thenReturn(Optional.empty());
        Mockito.when(dniClient.check(any(CheckDniRequest.class))).thenReturn(ResponseEntity.ok(new CheckDniResponse()));

        //Pero la sala no existe cuando la busca, devuelve un optional vacio
        Mockito.when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        // Verifico que se lanza la excepcion
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.createUser(request);
        });

        //Y el mensaje que tengo definido en el serviceimpl
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Sala no encontrada", exception.getReason());
    }

    @Test
    void createUser_adminSendEmail() {

        //Creo dto y simulo una peticion valida
        UserRequest request = new UserRequest();
        request.setEmail("admin@email.com");
        request.setDni("12345678A");
        request.setRole("admin");

        // El email no está duplicado y el DNI es valido
        Mockito.when(userRepository.findByEmail("admin@email.com")).thenReturn(Optional.empty());
        Mockito.when(dniClient.check(any(CheckDniRequest.class))).thenReturn(ResponseEntity.ok().build());

        //Seteo la room y existe
        Rooms room = new Rooms();
        room.setId(1L);
        Mockito.when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        //Creo una entidad con esos datos
        User user = new User();
        user.setEmail(request.getEmail());
        user.setDni(request.getDni());
        user.setRole(request.getRole());
        user.setRoom(room);

        //Mockeo que al pasarle el dto me devuelve esta entidad
        Mockito.when(userMapper.toEntity(request)).thenReturn(user);

        //Seteo el id y lo guardo en la bd, mockeo que me devuelva el usuario creado
        User savedUser = new User();
        savedUser.setId(99L);
        Mockito.when(userRepository.save(user)).thenReturn(savedUser);

        //Lo paso a user response que es lo que recibe
        UserResponse response = new UserResponse();
        response.setId(99L);
        Mockito.when(userMapper.toUserResponse(savedUser)).thenReturn(response);

        // Y lo intento crear
        userService.createUser(request);

        // Verifico que se llame a sendEmail que es lo que tocaria y que el mensaje es correcto
        verify(notificationClient, Mockito.times(1)).sendEmail(new EmailNotificationRequest("admin@email.com", "usuario guardado"));
    }

    @Test
    void createUser_superadminSendSms() {

        //Creo dto y simulo una peticion valida
        UserRequest request = new UserRequest();
        request.setEmail("superadmin@email.com");
        request.setDni("12345678A");
        request.setRole("superadmin");
        request.setPhone("666777888");

        // El email no está duplicado y el DNI es valido
        Mockito.when(userRepository.findByEmail("superadmin@email.com")).thenReturn(Optional.empty());
        Mockito.when(dniClient.check(any(CheckDniRequest.class))).thenReturn(ResponseEntity.ok().build());

        //Seteo la room y existe
        Rooms room = new Rooms();
        room.setId(1L);
        Mockito.when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        //Creo una entidad con esos datos
        User user = new User();
        user.setEmail(request.getEmail());
        user.setDni(request.getDni());
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        user.setRoom(room);

        //Mockeo que al pasarle el dto me devuelve esta entidad
        Mockito.when(userMapper.toEntity(request)).thenReturn(user);

        //Seteo el id y lo guardo en la bd, mockeo que me devuelva el usuario creado
        User savedUser = new User();
        savedUser.setId(99L);
        Mockito.when(userRepository.save(user)).thenReturn(savedUser);

        //Lo paso a user response que es lo que recibe
        UserResponse response = new UserResponse();
        response.setId(99L);
        Mockito.when(userMapper.toUserResponse(savedUser)).thenReturn(response);

        // Y lo intento crear
        userService.createUser(request);

        // Verifico que se llame a sendSms que es lo que tocaria y que el mensaje es correcto
        Mockito.verify(notificationClient, Mockito.times(1)).sendSms(new SmsNotificationRequest("666777888", "usuario guardado"));
    }

    @Test
    void getUserById_Found() {
        // GIVEN
        Long userId = 99L;

        // Usuario simulado que devuelve el repositorio
        User user = new User();
        user.setId(userId);
        user.setName("test");
        user.setEmail("test@email.com");

        // DTO esperado
        UserDetailResponse expectedResponse = new UserDetailResponse();
        expectedResponse.setId(userId);

        // Mockeo el repositorio y el mapper
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(userMapper.toUserDetail(user)).thenReturn(expectedResponse);

        // WHEN
        UserDetailResponse response = userService.getUserById(userId);

        // THEN
        assertNotNull(response);
        assertEquals(userId, response.getId());
        Mockito.verify(userRepository).findById(userId);
        Mockito.verify(userMapper).toUserDetail(user);
    }

    @Test
    void getUserById_NotFound() {

        // Simulo la peticion a un id inexistente
        Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.getUserById(99L);
        });

        // Verifica que devuelve 404 y el mensaje correcto
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("No existe un usuario con ese id", exception.getReason());

        // Verifico que NO se llama al mapper
        Mockito.verify(userMapper, Mockito.never()).toUserResponse(Mockito.any());
    }

}

