package com.hayden.graphql;

import com.hayden.graphql.models.client.ClientRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(classes = ClientRequest.class)
@ExtendWith(SpringExtension.class)
class GraphqlApplicationTests {

    @Test
    void contextLoads() {}

}
