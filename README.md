# Requirements
1. Java 21
2. MySql
3. Intellij,IDE to run the application
   
# Run application 
1. Clone the repository
2. Open the project in Intellij
   
## Database setup
1. Start MySql
2. Create a database named doctor_appointment
3. The project already has schema.sql and seed.sql which will load the files when running SpringBoot

## Connect MySql
1. Create file under src/main/resources named application.properties
2. 
spring.datasource.url=jdbc:mysql://localhost:3306/doctor_appointment
spring.datasource.username=root
spring.datasource.password= MYSQLPASSWORD

spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
spring.sql.init.data-locations=classpath:seed.sql
* Note: *  replace MYSQLPASSWORD with your password

3. Open DocAppointment application
4. Then click run 

# Test endpoints:
1. View available slots for a certain doctor : http://localhost:8080/doctor/1
2.View appointment for a user : http://localhost:8080/user/1   
