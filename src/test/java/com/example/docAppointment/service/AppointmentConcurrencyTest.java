
package com.example.docAppointment.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AppointmentConcurrencyTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void onlyOnePatientCanBookSameSlot() throws Exception {


        int patient1 = 1;
        int patient2 = 3;
        int doctorId = 1;
        int serviceId = 1;


        Integer slotId = jdbcTemplate.execute(
                (org.springframework.jdbc.core.ConnectionCallback<Integer>) connection -> {

                    String sql = """
                    INSERT INTO availability_slots
                    (dr_id, service_id, appointment_date,
                     start_time, end_time, is_avail)
                    VALUES (?, ?, ?, ?, ?, TRUE)
                    """;

                    try (PreparedStatement statement =
                                 connection.prepareStatement(
                                         sql,
                                         Statement.RETURN_GENERATED_KEYS
                                 )) {

                        statement.setInt(1, doctorId);
                        statement.setInt(2, serviceId);
                        statement.setObject(
                                3, LocalDate.now().plusDays(14)
                        );
                        statement.setObject(4, LocalTime.of(9, 0));
                        statement.setObject(5, LocalTime.of(9, 30));

                        statement.executeUpdate();

                        try (var keys = statement.getGeneratedKeys()) {
                            if (!keys.next()) {
                                throw new IllegalStateException(
                                        "Could not create test slot"
                                );
                            }

                            return keys.getInt(1);
                        }
                    }
                }
        );


        assertNotNull(slotId);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Callable<Boolean> booking1 = () ->
                    attemptBooking(patient1, slotId, serviceId, ready, start);

            Callable<Boolean> booking2 = () ->
                    attemptBooking(patient2, slotId, serviceId, ready, start);

            Future<Boolean> result1 = executor.submit(booking1);
            Future<Boolean> result2 = executor.submit(booking2);

            // Wait until both threads are ready.
            assertTrue(
                    ready.await(10, TimeUnit.SECONDS),
                    "Both booking threads should be ready"
            );


            start.countDown();

            boolean success1 = result1.get(20, TimeUnit.SECONDS);
            boolean success2 = result2.get(20, TimeUnit.SECONDS);


            assertEquals(
                    1,
                    (success1 ? 1 : 0) + (success2 ? 1 : 0),
                    "Exactly one patient should book the slot"
            );

            Integer bookingCount = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM appointments
                    WHERE avail_id = ?
                      AND status = 'SCHEDULED'
                    """,
                    Integer.class,
                    slotId
            );

            assertEquals(
                    1,
                    bookingCount,
                    "Database must contain exactly one active booking"
            );

            Boolean isAvailable = jdbcTemplate.queryForObject(
                    """
                    SELECT is_avail
                    FROM availability_slots
                    WHERE avail_id = ?
                    """,
                    Boolean.class,
                    slotId
            );

            assertEquals(
                    Boolean.FALSE,
                    isAvailable,
                    "Booked slot should no longer be available"
            );

        } finally {
            start.countDown();
            executor.shutdownNow();


            jdbcTemplate.update(
                    "DELETE FROM appointments WHERE avail_id = ?",
                    slotId
            );

            jdbcTemplate.update(
                    "DELETE FROM availability_slots WHERE avail_id = ?",
                    slotId
            );
        }
    }

    private boolean attemptBooking(
            int patientId,
            int slotId,
            int serviceId,
            CountDownLatch ready,
            CountDownLatch start
    ) throws InterruptedException {

        ready.countDown();
        start.await();

        try {
            appointmentService.bookAppointment(
                    patientId,
                    slotId,
                    serviceId
            );
            return true;

        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode() == HttpStatus.CONFLICT) {
                return false;
            }

            throw exception;
        }
    }
}
