package pl.symentis.shorturl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Currency;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class PaymentGatewayServiceTest
{
    // system under tests
    private PaymentGatewayService paymentGatewayService;
    private PaymentGatewayMock paymentGateway;

    @BeforeEach
    public void setUp() throws Exception
    {
        ObjectMapper objectMapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplateBuilder()
                .messageConverters( new MappingJackson2HttpMessageConverter( objectMapper ) ).build();
        paymentGatewayService = new PaymentGatewayService( restTemplate, new PaymentGatewayProperties( new URL( "http://localhost:9090/gateway/payments" )) );
        paymentGateway = PaymentGatewayMock.create( paymentGatewayService::completeRecharge ).start();
    }

    @AfterEach
    public void tearDown() throws InterruptedException
    {
        paymentGateway.shutdown();
    }

    @Test
    void rechargeAndCallbackPayment() throws MalformedURLException, RestClientException, URISyntaxException
    {
        PaymentRequest paymentRequest = new PaymentRequest( 
                "random@random.org", 
                BigDecimal.valueOf( 50 ),
                Currency.getInstance( "PLN" ), 
                new URL( "http://localhost:9090/payments/callback" ) );
        
        UUID randomUUID = UUID.randomUUID();
        
        paymentGateway.when( paymentRequest )
            .then( () -> new PaymentResponse( randomUUID, PaymentStatus.OK ) )
            .callback( () -> new PaymentCallbackRequest( randomUUID, PaymentStatus.SUCCESS ) );
        
        PaymentResponse paymentResponse = paymentGatewayService.recharge( paymentRequest );
        
        await().atMost( 5, TimeUnit.SECONDS )
                .until( () -> paymentGatewayService.paymentStatus( paymentResponse.getUuid() ) == PaymentStatus.SUCCESS );
    }
}