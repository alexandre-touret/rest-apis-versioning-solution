package info.touret.bookstore.spring.number.controller;

import info.touret.bookstore.spring.number.generated.dto.BookNumbersDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class BookNumbersControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void should_get_book_numbers() {
        var response = restTemplate.getForEntity("http://127.0.0.1:" + port + "/v1/isbns", BookNumbersDto.class);
        assertEquals(OK, response.getStatusCode());
        final var body = response.getBody();
        assertNotNull(body.getAsin());
        assertNotNull(body.getEan8());
        assertNotNull(body.getEan13());
        assertNotNull(body.getIsbn10());
        assertNotNull(body.getIsbn13());
    }
}
