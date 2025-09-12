package com.petcare.portal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = ThePetCareApplication.class)
@TestPropertySource(properties = {
    "DB_URL=jdbc:h2:mem:testdb",
    "DB_USERNAME=sa",
    "DB_PASSWORD=",
    "ACCESS_TOKEN_SECRET=testsecret",
    "ACCESS_TOKEN_EXPIRATION_MS=3600000",
    "REFRESH_TOKEN_SECRET=testrefresh",
    "REFRESH_TOKEN_EXPIRATION_MS=86400000",
    "OPENAI_API_KEY=testkey",
    "OPENAI_MODEL=gpt-3.5-turbo",
    "MAIL_HOST=smtp.gmail.com",
    "MAIL_PORT=587",
    "MAIL_USERNAME=test@example.com",
    "MAIL_PASSWORD=testpass",
    "FRONTEND_URL=http://localhost:3000"
})
class ThePetCareApplicationTests {

	@Test
	void contextLoads() {
	}

}
