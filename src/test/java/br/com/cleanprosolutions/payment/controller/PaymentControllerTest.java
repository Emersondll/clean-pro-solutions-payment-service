package br.com.cleanprosolutions.payment.controller;

import br.com.cleanprosolutions.payment.dto.PaymentResponse;
import br.com.cleanprosolutions.payment.dto.PaymentWebhookRequest;
import br.com.cleanprosolutions.payment.enumerations.PaymentStatus;
import br.com.cleanprosolutions.payment.exception.GlobalExceptionHandler;
import br.com.cleanprosolutions.payment.exception.PaymentNotFoundException;
import br.com.cleanprosolutions.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link PaymentController}.
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService service;

    @InjectMocks
    private PaymentController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("shouldProcessWebhookAndReturn200")
    void shouldProcessWebhookAndReturn200() throws Exception {
        final PaymentWebhookRequest request = new PaymentWebhookRequest(
                "txn-ext-1", "contract-1", true, null
        );

        mockMvc.perform(post("/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("shouldReturn400WhenWebhookRequestIsInvalid")
    void shouldReturn400WhenWebhookRequestIsInvalid() throws Exception {
        final String invalidJson = "{\"success\": true}";

        mockMvc.perform(post("/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("shouldReturn404WhenWebhookContractNotFound")
    void shouldReturn404WhenWebhookContractNotFound() throws Exception {
        final PaymentWebhookRequest request = new PaymentWebhookRequest(
                "txn-ext-1", "missing", true, null
        );
        doThrow(new PaymentNotFoundException("No pending payment found for contractId: missing"))
                .when(service).processWebhook(any(PaymentWebhookRequest.class));

        mockMvc.perform(post("/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("shouldGetPaymentByContractIdAndReturn200")
    void shouldGetPaymentByContractIdAndReturn200() throws Exception {
        final Instant now = Instant.now();
        final PaymentResponse response = new PaymentResponse(
                "pay-1", "contract-1", "client-1", new BigDecimal("150.00"),
                "txn-ext-1", PaymentStatus.APPROVED, now, now
        );
        when(service.findByContractId("contract-1")).thenReturn(response);

        mockMvc.perform(get("/payments/contract/contract-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractId").value("contract-1"))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("shouldReturn404WhenPaymentNotFoundByContractId")
    void shouldReturn404WhenPaymentNotFoundByContractId() throws Exception {
        when(service.findByContractId("missing"))
                .thenThrow(new PaymentNotFoundException("Payment not found for contractId: missing"));

        mockMvc.perform(get("/payments/contract/missing"))
                .andExpect(status().isNotFound());
    }
}
