package com.github.observer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder;
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;

@SpringBootTest
public class ObserverLambdaIntegrationTest {

    MockLambdaContext lambdaContext = new MockLambdaContext();

    @Test
    @DisplayName("Should return a list of repositories when valid path is invoked via Lambda")
    void whenTheUsersPathIsInvokedViaLambda_thenShouldReturnAList() {
        LambdaHandler lambdaHandler = new LambdaHandler();
        AwsProxyRequest req = new AwsProxyRequestBuilder("/repositories/volodya4u/true", "GET").build();
        AwsProxyResponse resp = lambdaHandler.handleRequest(req, lambdaContext);

        Assertions.assertNotNull(resp.getBody(), "Response body should not be null");
        Assertions.assertEquals(200, resp.getStatusCode(), "Status code should be 200");

        String responseBody = resp.getBody();
        Assertions.assertTrue(responseBody.contains("volodya4u"), "Response body should contain 'volodya4u'");
    }

    @Test
    @DisplayName("Should return 400 Bad Request when wrong path is invoked via Lambda")
    void whenWrongPathIsInvokedViaLambda_thenShouldBadRequest() {
        LambdaHandler lambdaHandler = new LambdaHandler();
        AwsProxyRequest req = new AwsProxyRequestBuilder("/repositories/2/volodya4u", "GET").build();
        AwsProxyResponse resp = lambdaHandler.handleRequest(req, lambdaContext);

        Assertions.assertEquals(400, resp.getStatusCode(), "Status code should be 400");

        String responseBody = resp.getBody();
        Assertions.assertTrue(responseBody.contains("BAD_REQUEST"), "Response body should contain 'Bad Request'");
    }

    @Test
    @DisplayName("Should return 406 Not Acceptable when wrong Accept header is invoked via Lambda")
    void whenWrongAcceptHeaderIsInvokedViaLambda_thenShouldReturnNotAcceptable() {
        LambdaHandler lambdaHandler = new LambdaHandler();
        AwsProxyRequest req = new AwsProxyRequestBuilder("/repositories/volodya4u/true", "GET")
                .header("Accept", "application/xml")
                .build();
        AwsProxyResponse resp = lambdaHandler.handleRequest(req, lambdaContext);

        Assertions.assertEquals(406, resp.getStatusCode(), "Status code should be 406");

        String responseBody = resp.getBody();
        Assertions.assertTrue(responseBody.contains("NOT_ACCEPTABLE"), "Response body should contain 'Not Acceptable'");
    }

    @Test
    @DisplayName("Should return 400 Bad Request when unsupported method is invoked via Lambda")
    void whenUnsupportedMethodIsInvokedViaLambda_thenShouldReturnBadRequest() {
        LambdaHandler lambdaHandler = new LambdaHandler();
        AwsProxyRequest req = new AwsProxyRequestBuilder("/repositories/volodya4u/true", "POST").build();
        AwsProxyResponse resp = lambdaHandler.handleRequest(req, lambdaContext);

        Assertions.assertEquals(400, resp.getStatusCode(), "Status code should be 400");

        String responseBody = resp.getBody();
        Assertions.assertTrue(responseBody.contains("METHOD_NOT_ALLOWED"), "Response body should contain 'Method Not Allowed'");
    }
}
