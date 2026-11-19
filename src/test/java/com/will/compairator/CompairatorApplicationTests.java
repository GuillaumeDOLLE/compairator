package com.will.compairator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(classes = com.will.compairator.configuration.CompairatorApplication.class)
@ContextConfiguration(initializers = TestDatabaseGuard.class)
class CompairatorApplicationTests {

	@Test
	void contextLoads() {
	}

}
