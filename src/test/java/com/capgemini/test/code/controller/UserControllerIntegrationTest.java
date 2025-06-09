package com.capgemini.test.code.controller;

import com.capgemini.test.code.clients.CheckDniResponse;
import com.capgemini.test.code.clients.DniClient;
import com.capgemini.test.code.clients.NotificationClient;
import com.capgemini.test.code.model.entity.Rooms;
import com.capgemini.test.code.model.entity.User;
import com.capgemini.test.code.model.repository.RoomRepository;
import com.capgemini.test.code.model.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DniClient dniClient;

    @MockBean
    private NotificationClient notificationClient;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomRepository roomRepository;

    @Test
    void createUser_Created() throws Exception {

        // Simulo que la API de DNI devuelve 200 OK
        Mockito.when(dniClient.check(Mockito.any())).thenReturn(ResponseEntity.ok(new CheckDniResponse("OK")));

        // Le digo que no devuelva nada solo que no falle cuando intenta enviar un email
        Mockito.doNothing().when(notificationClient).sendEmail(Mockito.any());

        String json = """
                    {
                        "name": "test",
                        "dni": "12345678A",
                        "email": "test@email.com",
                        "phone": "600000001",
                        "rol": "admin"
                    }
                """;

        //Peticion post con el cuerpo creado arriba
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isCreated()).andExpect(jsonPath("$.id").exists());

    }

    @Test
    void getUserById_Ok() throws Exception {

        //Creo una sala, un usuario y lo guardo en la sala, para simular que ya estaba guardado en la bd
        Rooms room = new Rooms();
        room.setId(1L);
        room.setName("sala1");
        roomRepository.save(room);
        User user = new User();
        user.setName("test");
        user.setDni("12345678A");
        user.setEmail("test@email.com");
        user.setPhone("600000000");
        user.setRole("admin");
        user.setRoom(room);
        userRepository.save(user);

        // Petición GET al endpoint con ese ID
        mockMvc.perform(get("/users/" + user.getId())).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("test")).andExpect(jsonPath("$.email").value("test@email.com")).andExpect(jsonPath("$.phone").value("600000000"))
                .andExpect(jsonPath("$.rol").value("admin")).andExpect(jsonPath("$.dni").value("12345678A"));
    }

}
