package com.capgemini.test.code.controller;

import com.capgemini.test.code.clients.CheckDniResponse;
import com.capgemini.test.code.clients.DniClient;
import com.capgemini.test.code.clients.NotificationClient;
import com.capgemini.test.code.model.entity.Rooms;
import com.capgemini.test.code.model.entity.User;
import com.capgemini.test.code.model.repository.RoomRepository;
import com.capgemini.test.code.model.repository.UserRepository;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Map;

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Transactional
    @Test
    void createUser_Created() throws Exception {

        // Simulo que la API de DNI devuelve 200 OK
        Mockito.when(dniClient.check(Mockito.any())).thenReturn(ResponseEntity.ok(new CheckDniResponse("OK")));

        // Le digo que no devuelva nada solo que no falle cuando intenta enviar un email
        Mockito.when(notificationClient.sendEmail(Mockito.any())).thenReturn(ResponseEntity.ok().build());

        String json = """
                    {
                        "name": "test",
                        "dni": "12345671A",
                        "email": "test3@email.com",
                        "phone": "600000111",
                        "role": "admin"
                    }
                """;

        //Peticion post con el cuerpo creado arriba
        mockMvc.perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isCreated()).andExpect(jsonPath("$.id").exists());

    }

    @Transactional
    @Test
    void createUser_DuplicateEmail() throws Exception {
        // Obtengo la sala1
        Rooms room = roomRepository.findById(1L).orElseThrow(() -> new RuntimeException("Sala 'sala1' no existe"));

        // Crear y guardar usuario con email duplicado
        User user = new User();
        user.setName("test");
        user.setEmail("test3@email.com");
        user.setDni("12345674A");
        user.setPhone("600000089");
        user.setRole("admin");
        user.setRoom(room);
        userRepository.save(user);

        // Preparar el JSON de la petición con el mismo email
        String json = """
                {
                    "name": "nuevo",
                    "dni": "87654321B",
                    "email": "test3@email.com",
                    "phone": "600000001",
                    "rol": "admin"
                }
                """;

        // Simulo respuesta correcta del servicio de DNI
        Mockito.when(dniClient.check(Mockito.any())).thenReturn(ResponseEntity.ok(new CheckDniResponse("OK")));
        // Evitar fallo en envío de email
        Mockito.when(notificationClient.sendEmail(Mockito.any())).thenReturn(ResponseEntity.ok().build());

        // Realizar la petición y verificar el conflicto
        mockMvc.perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("error validation email")).andExpect(jsonPath("$.code").value(409));
    }

    @Transactional
    @Test
    void createUser_InvalidDni() throws Exception {
        // Simulo que la API de DNI devuelve 409 para ese DNI
        Request feignRequest = Request.create(Request.HttpMethod.POST, "/api/dni/check", Map.of(), null, StandardCharsets.UTF_8);
        Mockito.when(dniClient.check(Mockito.argThat(req -> req.getDni().equalsIgnoreCase("99999999w")))).thenThrow(new FeignException.Conflict("error validation dni", feignRequest, null, null));

        // Mock para que no falle por email duplicado ni notificación
        Mockito.when(notificationClient.sendEmail(Mockito.any())).thenReturn(ResponseEntity.ok().build());
        String json = """
                {
                    "name": "test",
                    "dni": "99999999w",
                    "email": "test4@email.com",
                    "phone": "600000003",
                    "role": "admin"
                }
                """;

        mockMvc.perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("error validation dni")).andExpect(jsonPath("$.code").value(409));
    }

    @Transactional
    @Test
    void createUser_InvalidRole() throws Exception {

        String json = """
                    {
                        "name": "test",
                        "dni": "12345611A",
                        "email": "test2@email.com",
                        "phone": "600000001",
                        "role": "user"
                    }
                """;

        mockMvc.perform(post("/user").contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("El rol debe ser admin o superadmin"));
    }

    @Transactional
    @Test
    void getUserById_Ok() throws Exception {

        // Obtengo la sala1
        Rooms room = roomRepository.findById(1L).orElseThrow(() -> new RuntimeException("Sala 'sala1' no existe"));
        //Simulo crear un usuario y luego lo consulto
        User user = new User();
        user.setId(3L);
        user.setName("test");
        user.setDni("12345681A");
        user.setEmail("test3@email.com");
        user.setPhone("600000098");
        user.setRole("admin");
        user.setRoom(room);
        userRepository.save(user);

        // Petición GET al endpoint con ese ID
        mockMvc.perform(get("/user/" + user.getId())).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("test")).andExpect(jsonPath("$.email").value("test3@email.com")).andExpect(jsonPath("$.phone").value("600000098"))
                .andExpect(jsonPath("$.role").value("admin")).andExpect(jsonPath("$.dni").value("12345681A"));
    }

    @Test
    void getUserById_NotFound() throws Exception {
        // Simulo la peticion a un id inexistente
        mockMvc.perform(get("/user/9999")).andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("No existe un usuario con ese id")).andExpect(jsonPath("$.code").value(404));
    }

}
