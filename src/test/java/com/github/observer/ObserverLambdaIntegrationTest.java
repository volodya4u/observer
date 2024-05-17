package com.github.observer;

import java.io.IOException;

import com.github.observer.lambda.ObserverLambdaHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder;
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ObserverLambdaIntegrationTest {

    MockLambdaContext lambdaContext = new MockLambdaContext();

    @Test
    void whenTheUsersPathIsInvokedViaLambda_thenShouldReturnAList() throws IOException {
        ObserverLambdaHandler lambdaHandler = new ObserverLambdaHandler();
        AwsProxyRequest req = new AwsProxyRequestBuilder("/repositories/volodya4u", "GET").build();
        AwsProxyResponse resp = lambdaHandler.handleRequest(req, lambdaContext);
        Assertions.assertNotNull(resp.getBody());
        Assertions.assertEquals(200, resp.getStatusCode());
    }

    @Test
    void whenWrongPathPathIsInvokedViaLambda_thenShouldBadRequest() throws IOException {
        ObserverLambdaHandler lambdaHandler = new ObserverLambdaHandler();
        AwsProxyRequest req = new AwsProxyRequestBuilder("/repositories/2/volodya4u", "GET").build();
        AwsProxyResponse resp = lambdaHandler.handleRequest(req, lambdaContext);
        Assertions.assertEquals(400, resp.getStatusCode());
    }

    @Test
    void whenWrongAcceptHeaderIsInvokedViaLambda_thenShouldNotFound() throws IOException {
        ObserverLambdaHandler lambdaHandler = new ObserverLambdaHandler();
        AwsProxyRequest req
                = new AwsProxyRequestBuilder("/repositories/volodya4u", "GET")
                .header("Accept", "application/xml")
                .build();
        AwsProxyResponse resp = lambdaHandler.handleRequest(req, lambdaContext);
        Assertions.assertEquals("406 NOT_ACCEPTABLE \"Could not find acceptable representation\"",
                resp.getBody());
    }
}