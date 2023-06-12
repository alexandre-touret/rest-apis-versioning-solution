package info.touret.bookstore.spring.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

@SpringBootTest(classes = NoSecurityConfiguration.class)
@Profile("test")
class GatewayApplicationTests {

	@MockBean
	private ReactiveJwtDecoder reactiveJwtDecoder;

	@MockBean
	private JwtDecoder jwtDecoder;

	@BeforeEach
	void setUp() {
	}

	@Test
	void contextLoads() {
	}

}
