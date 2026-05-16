package br.com.cleanprosolutions.payment.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for the payment service.
 *
 * <p>Configures the consumer for {@code ContractCreated} and the
 * publishers for {@code PaymentApproved} and {@code PaymentFailed}.</p>
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
@Configuration
public class RabbitMQConfig {

    // --- Consumer Configs ---
    @Value("${rabbitmq.exchange.contract:contract.exchange}")
    private String contractExchange;

    @Value("${rabbitmq.queue.contract-created:contract.created.queue}")
    private String contractCreatedQueue;

    @Value("${rabbitmq.routing-key.contract-created:contract.created}")
    private String contractCreatedRoutingKey;

    @Value("${rabbitmq.queue.contract-canceled:contract.canceled.payment.queue}")
    private String contractCanceledQueue;

    @Value("${rabbitmq.routing-key.contract-canceled:contract.canceled}")
    private String contractCanceledRoutingKey;

    // --- Publisher Configs ---
    @Value("${rabbitmq.exchange.payment:payment.exchange}")
    private String paymentExchange;

    @Value("${rabbitmq.routing-key.payment-failed:payment.failed}")
    private String paymentFailedRoutingKey;

    @Bean
    public Queue contractCreatedQueue() {
        return new Queue(contractCreatedQueue, true);
    }

    @Bean
    public TopicExchange contractExchange() {
        return new TopicExchange(contractExchange, true, false);
    }

    @Bean
    public Binding contractCreatedBinding() {
        return BindingBuilder
                .bind(contractCreatedQueue())
                .to(contractExchange())
                .with(contractCreatedRoutingKey);
    }

    @Bean
    public Queue contractCanceledQueue() {
        return new Queue(contractCanceledQueue, true);
    }

    @Bean
    public Binding contractCanceledBinding() {
        return BindingBuilder
                .bind(contractCanceledQueue())
                .to(contractExchange())
                .with(contractCanceledRoutingKey);
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(paymentExchange, true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
