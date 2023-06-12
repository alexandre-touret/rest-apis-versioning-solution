package info.touret.bookstore.spring.book.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.touret.bookstore.spring.book.dto.IsbnNumbers;
import info.touret.bookstore.spring.book.generated.dto.BookDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Sql("classpath:/books-data.sql")
@DirtiesContext(classMode = BEFORE_CLASS)
class BookControllerIT {


    public static final String BOOKS_API_PREFIX = "/v1/books";
    @Value("${booknumbers.api.url}")
    public String isbnAPIURL;
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private String booksUrl;
    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        booksUrl = "http://127.0.0.1:" + port + BOOKS_API_PREFIX;
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        mockServer.reset();
    }

    /**
     * Gets a mock server for book numbers api
     *
     * @throws URISyntaxException
     * @throws JsonProcessingException
     */
    private void createMockServerStandard() throws URISyntaxException, JsonProcessingException {
        IsbnNumbers isbnNumbers = new IsbnNumbers();
        isbnNumbers.setIsbn10("0123456789");
        isbnNumbers.setIsbn13("0123456789012");
        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI(isbnAPIURL)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(isbnNumbers))
                );
    }

    /**
     * Creates a mockserver which hangs
     *
     * @throws URISyntaxException
     * @throws JsonProcessingException
     */
    private void createMockServerTimeout() throws URISyntaxException, JsonProcessingException {
        IsbnNumbers isbnNumbers = new IsbnNumbers();
        isbnNumbers.setIsbn10("0123456789");
        isbnNumbers.setIsbn13("0123456789012");
        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI(isbnAPIURL)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(request -> {
                    var max = System.currentTimeMillis()+30000;
                    while(System.currentTimeMillis()<max){
                        // do nothing
                    }
                    return new MockClientHttpResponse(isbnNumbers.toString().getBytes(), HttpStatus.OK);
                });
    }

    @Test
    void should_get_a_random_book() {
        var bookDto = testRestTemplate.getForEntity(booksUrl + "/random", BookDto.class).getBody();
        assertNotNull(bookDto.getId());
    }


    @Test
    void should_find_all_books() throws Exception {
        var requestEntity = RequestEntity.get(new URI(booksUrl)).accept(MediaType.APPLICATION_JSON).build();
        var bookDtos = testRestTemplate.exchange(requestEntity, new ParameterizedTypeReference<List<BookDto>>() {
        }).getBody();
        assertNotNull(bookDtos);
        assertEquals(1, bookDtos.size());
    }

    @Test
    void should_get_a_count() throws Exception {
        var requestEntity = RequestEntity.get(new URI(booksUrl +"/count")).accept(MediaType.APPLICATION_JSON).build();
        var books = testRestTemplate.exchange(requestEntity, new ParameterizedTypeReference<Map<String, Long>>() {
        }).getBody();
        assertNotNull(books);
        assertEquals(1, books.size());
        assertEquals(1, books.get("books.count"));
    }

    @Test
    void should_find_a_book() throws Exception {
        var responseEntity = testRestTemplate.getForEntity(booksUrl + "/100", BookDto.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        var bookDto = responseEntity.getBody();
        assertNotNull(bookDto);
        assertEquals(100L, bookDto.getId());
    }

    @Test
    void should_find_no_book() throws Exception {
        var responseEntity = testRestTemplate.getForEntity(booksUrl + "/999", BookDto.class);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertFalse(responseEntity.hasBody());
    }

    @Test
    void should_register_a_book_successfully() throws Exception {
        createMockServerStandard();
        BookDto bookDto = new BookDto();
        bookDto.setAuthor("George Orwell");
        bookDto.setTitle("Animal's farm");
        var responseEntity = testRestTemplate.postForEntity(booksUrl, bookDto, BookDto.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        var uri = responseEntity.getHeaders().getLocation();
        assertNotNull(uri);
        assertTrue(uri.getPath().matches(BOOKS_API_PREFIX + "/[1-9]+$"));
        mockServer.verify();
    }

    @Test
    void should_throw_a_timeout_while_registering() throws Exception {
        createMockServerTimeout();
        var bookDto = new BookDto();
        bookDto.setAuthor("George Orwell");
        bookDto.setTitle("Animal's farm");
        var responseEntity = testRestTemplate.postForEntity(booksUrl, bookDto, BookDto.class);
        assertEquals(HttpStatus.REQUEST_TIMEOUT, responseEntity.getStatusCode());
    }

    @Test
    void should_update_book() throws Exception {
        var book = new BookDto();
        book.setId(100L);
        book.setAuthor("George Orwell");
        book.setTitle("Animal's farm");
        var responseEntity = testRestTemplate.exchange(booksUrl, HttpMethod.PUT, new HttpEntity<>(book), BookDto.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void should_delete_book() throws Exception {
        testRestTemplate.delete(booksUrl +"/100");
        var responseEntity = testRestTemplate.getForEntity(booksUrl + "/100", BookDto.class);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }
}
