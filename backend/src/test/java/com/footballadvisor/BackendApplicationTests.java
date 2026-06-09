package com.footballadvisor;

import jade.wrapper.AgentContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

	@MockBean
	private AgentContainer jadeContainer;

	@Test
	void contextLoads() {
	}

}
