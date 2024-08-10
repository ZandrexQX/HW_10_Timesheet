
package ru.gb.timesheet.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;
import ru.gb.timesheet.model.Timesheet;
import ru.gb.timesheet.repository.TimesheetRepository;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureWebTestClient
class TimesheetControllerTest {

    @Autowired
    TimesheetRepository timesheetRepo;

    @LocalServerPort
    private int port;
    private RestClient restClient;

    @BeforeEach
    void beforeEach() {
        restClient = RestClient.create("http://localhost:" + port);
    }

    @Test
    void getByIdNotFound() {
        // GET /timesheets/{id}
        ResponseEntity<Void> response = restClient.get()
                .uri("/projects/999")
                .retrieve()
                .toBodilessEntity(); // less
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getByIdAllOk() {
        // given
        Timesheet timesheet = new Timesheet();
        LocalDate createdAt = LocalDate.now();
        timesheet.setProjectId(ThreadLocalRandom.current().nextLong(1, 6));
        timesheet.setCreatedAt(createdAt);
        timesheet.setMinutes(ThreadLocalRandom.current().nextInt(100, 1000));

        Timesheet expected = timesheetRepo.save(timesheet);


        // GET /timesheets/{id}
        ResponseEntity<Timesheet> actual = restClient.get()
                .uri("/timesheets/" + expected.getId())
                .retrieve()
                .toEntity(Timesheet.class);

        // assert 200 OK
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        Timesheet responseBody = actual.getBody();
        assertNotNull(responseBody);
        assertEquals(timesheet.getId(), responseBody.getId());
        assertEquals(timesheet.getProjectId(), responseBody.getProjectId());
        assertEquals(timesheet.getMinutes(), responseBody.getMinutes());
        assertEquals(timesheet.getCreatedAt(), responseBody.getCreatedAt());
    }

    @Test
    void testCreate() {
        // POST /timesheets
        Timesheet toCreate = new Timesheet();
        toCreate.setId(1000L);

        ResponseEntity<Timesheet> response = restClient.post()
                .uri("/timesheets")
                .body(toCreate)
                .retrieve()
                .toEntity(Timesheet.class);

        // Проверяем HTTP-ручку сервера
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Timesheet responseBody = response.getBody();
        assertNotNull(responseBody);
        assertNotNull(responseBody.getId());

        // Проверяем, что запись в БД есть
        assertTrue(timesheetRepo.existsById(responseBody.getId()));

    }

    @Test
    void testDeleteById() {
        // DELETE /timesheets
        Timesheet toDelete = new Timesheet();
        toDelete.setId(2000L);
        toDelete = timesheetRepo.save(toDelete);

        ResponseEntity<Void> response = restClient.delete()
                .uri("/timesheets/" + toDelete.getId())
                .retrieve()
                .toBodilessEntity(); // less
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Проверяем, что запись в БД НЕТ
        assertFalse(timesheetRepo.existsById(toDelete.getId()));
    }

    @Test
    void testUpdate() {
        // PUT /timesheets
        Timesheet toUpdate = new Timesheet();
        toUpdate.setId(3000L);
        toUpdate = timesheetRepo.save(toUpdate);
        toUpdate.setMinutes(999);
        ResponseEntity<Timesheet> response = restClient.put()
                .uri("/timesheets/" + toUpdate.getId())
                .body(toUpdate)
                .retrieve()
                .toEntity(Timesheet.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Timesheet responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(toUpdate.getId(), responseBody.getId());
        assertEquals(toUpdate.getMinutes(), responseBody.getMinutes());
    }

}