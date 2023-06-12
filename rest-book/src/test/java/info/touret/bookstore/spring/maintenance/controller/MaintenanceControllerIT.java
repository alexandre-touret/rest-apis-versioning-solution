package info.touret.bookstore.spring.maintenance.controller;

import info.touret.bookstore.spring.book.entity.Book;
import info.touret.bookstore.spring.book.generated.dto.MaintenanceDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.RequestEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;


@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
class MaintenanceControllerIT {

    public static final String BOOKS_API_PREFIX = "/v1/books";
    public static final String MAINTENANCE_API_PREFIX = "/v1/maintenance";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String maintenanceUrl;
    private String booksUrl;

    @BeforeEach
    void setUp() throws Exception {
        maintenanceUrl = "http://127.0.0.1:" + port + MAINTENANCE_API_PREFIX;
        booksUrl = "http://127.0.0.1:" + port + BOOKS_API_PREFIX;
        /* Initializes the maintenance flag */
        var requestEntity = RequestEntity.put(new URI(maintenanceUrl)).body(FALSE.toString());
        var responseEntity = restTemplate.exchange(requestEntity, Void.class);

    }

    @Test
    void should_get_inMaintenance() {
        var response = restTemplate.getForEntity(maintenanceUrl, MaintenanceDto.class);
        assertEquals(OK, response.getStatusCode());
        var maintenanceDTO = response.getBody();
        assertNotNull(maintenanceDTO);
        assertFalse(maintenanceDTO.getInMaintenance());
    }

    @Test
    void should_set_inMaintenance() throws Exception {
        var requestEntity = RequestEntity.put(new URI(maintenanceUrl)).body(TRUE.toString());
        var responseEntity = restTemplate.exchange(requestEntity, Void.class);
        assertEquals(NO_CONTENT, responseEntity.getStatusCode());
        var response = restTemplate.getForEntity(maintenanceUrl, MaintenanceDto.class).getBody();
        assertTrue(response.getInMaintenance());
    }

    @Test
    void should_throw_i_m_teapot_http_code() throws Exception {
        var requestEntity = RequestEntity.put(new URI(maintenanceUrl)).body(TRUE.toString());
        var responseEntity = restTemplate.exchange(requestEntity, Void.class);
        assertEquals(NO_CONTENT, responseEntity.getStatusCode());
        var response = restTemplate.getForEntity(booksUrl, Book.class);
        assertEquals(I_AM_A_TEAPOT, response.getStatusCode());
    }

}
