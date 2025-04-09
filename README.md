
# capgemini-test

1. prerequisitos
   - java 17
   - maven
   - git
   - docker



1. Ejecución del docker para tener infraestructura necesaria:
   - git clone <repo>
   - dentro de la carpeta docker
   - docker-compose up



2. el docker tendrá dos contenedores     
   - postgres con la base de datos   
   - mock-server con una api expuesta en el puerto 1080

   
   
         api check-dni:   
              method: PATCH  
                url: http//localhost:1080/check-dni
                body:                  
                   {  
                   "dni": <dni>"
                    } 
               reponse:                   
                ok - http: 200 para cualquier dni  
   
                ko - http: 409 que sea 99999999w               
   
           api notification:   
              method: post  
              url: http//localhost:1080/email  
                  body:                   
                     {  
                              "email": <email, 
                               "message": <msg
                            } 
                   reponse:                   
                              ok - http: 200  
               method: post  
                  url: http//localhost:1080/sms
                      body:                
                             {  
                            "phone": <phone,
                                "message": <msg
                             } 
                       reponse:     
                     ok - http: 200




2.  #### Contexto #####  
Tenemos diferentes salas que se identifican cada una con id diferente (long incremental) y en cada sala debemos poder guardar usuarios.  
Una sala podrá tener N usuarios y un usuario podrá estar en una única sala. Cuando se guarda el usuario en la sala, se valida contra una api externa para chequear si el dni es válido y  
se le asignara un id (long incremental) retornando como respuesta el id del usuario guardado.



3. Requisitos

- name: no deberá de contener más de 6 caracteres
- email: deberá de contener un @ y un .
- El rol podrá ser admin o superadmin
- Si el usuario existe por email deberemos de mandar una exception.
- Chequear el dni contra una api externa expuesta en el mock-server
- Cuando se guarde el usuario correctamente en la base de datos se notificará en base al rol:
- admin -> un email con texto: "usuario guardado"
-	superadmin -> en sms con texto "usuario guardado"
- Retornar el id generado

  4. Métodos a implementar

     - Método POST para guardar un usuario dentro de una sala 1 retornado un id
       - el json de ejemplo que debemos guardar:

               {  
                   "name": "pablo",  
                   "email" : "email@email.com",  
                   "phone": "677998899"  
                   "rol": "admin"  
                   "dni": "23454234W"  
                } 

     - Método GET donde obtendremos el usuario en base al id del usuario obtenido que se aloja dentro de la sala 1. 
    
     - nota: no implementar la creación de sala e imaginar que la sala ya está creada. 


5. Condiciones a tener en cuenta opcionales  
   La aplicación podrá escalar a futuro pudiendo añadir diferentes contextos: pagos, pedidos....  
   La aplicación podrá recibir 1 o millones de peticiones. (no hace falta implementar nada de kubernetes)

7. Respuesta de la API  
   crear usuario:
   -  OK ->
      - http code: 201 created
      -  body:
       ```sh
               { id: <id> } 
       ```
   -  KO email o userName o dni:
      -  http code: 409 conflic
      -  body:

               {  
               "code": 409  
                  "message": "error validation <email | userName | dni"  
               } 


      - KO usuario existe:  
         -  http code: 409  
         -  body: 
         
               {  
                 "code": 409  
                    "message": "error validation <email | userName | dni"  
                 } 


8. Cosas que se valoran.

   - test: unitarios, integración y aceptación
   - arquitectura e implementación
   - uso de spring y capación de abtracción del framework.
   - patrones de diseño

9. Método de entrega

   Se deberá de entregar en un repositorio público personal con todos los commit realizado en la rama main.
