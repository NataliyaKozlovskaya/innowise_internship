package unit;

import static com.innowise.payment.enums.PaymentStatus.COMPLETED;
import static com.innowise.payment.enums.PaymentStatus.FAILED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.payment.dto.PaymentIdResponse;
import com.innowise.payment.properties.ExternalServiceProperties;
import com.innowise.payment.rest.ExternalPaymentServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Tests for class {@link ExternalPaymentServiceClient}
 */
@ExtendWith(MockitoExtension.class)
class ExternalPaymentServiceClientTest {

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private ExternalServiceProperties externalServiceProperties;

  private ExternalPaymentServiceClient client;
  private final String SERVICE_URL = "https://api.external-service.com/payment";
  private final String RESPONSE_BODY = "1234";

  @BeforeEach
  void setUp() {
    client = new ExternalPaymentServiceClient(restTemplate, externalServiceProperties);
  }

  @Test
  @DisplayName("Should return successful response when external service returns valid even number")
  void generatePaymentId_ShouldReturnSuccessResponse_WhenExternalServiceReturnsValidEvenNumber() {
    when(externalServiceProperties.getUrl()).thenReturn(SERVICE_URL);
    when(restTemplate.getForObject(SERVICE_URL, String.class)).thenReturn(RESPONSE_BODY);

    PaymentIdResponse result = client.generatePaymentId();

    assertNotNull(result);
    assertEquals(RESPONSE_BODY, result.getPaymentId());
    assertEquals(COMPLETED, result.getStatus());
    verify(externalServiceProperties, times(1)).getUrl();
    verify(restTemplate).getForObject(SERVICE_URL, String.class);
  }

  @Test
  @DisplayName("Should return failed response when external service returns valid odd number")
  void generatePaymentId_ShouldReturnFailedResponse_WhenExternalServiceReturnsValidOddNumber() {
    when(externalServiceProperties.getUrl()).thenReturn(SERVICE_URL);
    when(restTemplate.getForObject(SERVICE_URL, String.class)).thenReturn("-123");

    PaymentIdResponse result = client.generatePaymentId();

    assertNotNull(result);
    assertEquals("-123", result.getPaymentId());
    assertEquals(FAILED, result.getStatus());
    verify(externalServiceProperties, times(1)).getUrl();
    verify(restTemplate).getForObject(SERVICE_URL, String.class);
  }

  @Test
  @DisplayName("Should trim whitespace from response when response contains whitespace")
  void generatePaymentId_ShouldTrimWhitespace_WhenResponseHasWhitespace() {
    when(externalServiceProperties.getUrl()).thenReturn(SERVICE_URL);
    when(restTemplate.getForObject(SERVICE_URL, String.class)).thenReturn(RESPONSE_BODY);

    PaymentIdResponse result = client.generatePaymentId();

    assertNotNull(result);
    assertEquals("1234", result.getPaymentId());
    assertEquals(COMPLETED, result.getStatus());
    verify(externalServiceProperties, times(1)).getUrl();
    verify(restTemplate).getForObject(SERVICE_URL, String.class);
  }

  @Test
  @DisplayName("Should throw exception when external service returns null response")
  void generatePaymentId_ShouldThrowException_WhenResponseIsNull() {
    when(externalServiceProperties.getUrl()).thenReturn(SERVICE_URL);
    when(restTemplate.getForObject(SERVICE_URL, String.class)).thenReturn(null);

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> client.generatePaymentId());

    assertEquals("Failed to call external payment service", exception.getMessage());
    verify(externalServiceProperties, times(1)).getUrl();
    verify(restTemplate).getForObject(SERVICE_URL, String.class);
  }

  @Test
  @DisplayName("Should throw exception when external service returns empty response")
  void generatePaymentId_ShouldThrowException_WhenResponseIsEmpty() {
    when(externalServiceProperties.getUrl()).thenReturn(SERVICE_URL);
    when(restTemplate.getForObject(SERVICE_URL, String.class)).thenReturn("");

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> client.generatePaymentId());

    assertEquals("Failed to call external payment service", exception.getMessage());
    verify(externalServiceProperties, times(1)).getUrl();
    verify(restTemplate).getForObject(SERVICE_URL, String.class);
  }

  @Test
  @DisplayName("Should throw exception when external service returns non-numeric response")
  void generatePaymentId_ShouldThrowException_WhenResponseIsNotNumber() {
    String responseBody = "invalid responseBody";

    when(externalServiceProperties.getUrl()).thenReturn(SERVICE_URL);
    when(restTemplate.getForObject(SERVICE_URL, String.class)).thenReturn(responseBody);

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> client.generatePaymentId());

    assertEquals("Failed to call external payment service", exception.getMessage());
    verify(externalServiceProperties, times(1)).getUrl();
    verify(restTemplate).getForObject(SERVICE_URL, String.class);
  }

  @Test
  @DisplayName("Should throw exception when RestTemplate throws exception")
  void generatePaymentId_ShouldThrowException_WhenRestTemplateThrowsException() {
    RuntimeException originalException = new RuntimeException("Connection failed");

    when(externalServiceProperties.getUrl()).thenReturn(SERVICE_URL);
    when(restTemplate.getForObject(SERVICE_URL, String.class)).thenThrow(originalException);

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> client.generatePaymentId());

    assertEquals("Failed to call external payment service", exception.getMessage());
    assertEquals(originalException, exception.getCause());
    verify(externalServiceProperties, times(1)).getUrl();
    verify(restTemplate).getForObject(SERVICE_URL, String.class);
  }

  @Test
  @DisplayName("Should return locally generated payment ID when service is unavailable")
  void recoverGeneratePaymentId_ShouldReturnLocalPaymentId_WhenServiceUnavailable() {
    HttpServerErrorException exception = mock(HttpServerErrorException.class);

    PaymentIdResponse result = client.recoverGeneratePaymentId(exception);

    assertNotNull(result);
    assertNotNull(result.getPaymentId());

    int paymentId = Integer.parseInt(result.getPaymentId());
    assertTrue(paymentId >= 1 && paymentId <= 1000);

    if (paymentId % 2 == 0) {
      assertEquals(COMPLETED, result.getStatus());
    } else {
      assertEquals(FAILED, result.getStatus());
    }
  }
}
