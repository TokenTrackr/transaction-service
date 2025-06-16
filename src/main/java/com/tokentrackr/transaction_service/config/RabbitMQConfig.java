package com.tokentrackr.transaction_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ========================
    // 1. DIRECT EXCHANGE (Commands / Responses)
    // ========================
    public static final String SAGA_DIRECT_EXCHANGE             = "saga.direct.exchange";

    // Command Queues (Transaction Service → User/Asset)
    public static final String BALANCE_UPDATE_QUEUE             = "balance.update.queue";
    public static final String ASSET_UPDATE_QUEUE               = "asset.update.queue";

    // Response Queues (User/Asset → Transaction Service)
    public static final String BALANCE_UPDATED_QUEUE            = "balance.updated.queue";
    public static final String BALANCE_UPDATE_FAILED_QUEUE      = "balance.update.failed.queue";
    public static final String ASSET_UPDATED_QUEUE              = "asset.updated.queue";
    public static final String ASSET_UPDATE_FAILED_QUEUE        = "asset.update.failed.queue";

    @Bean
    public DirectExchange sagaDirectExchange() {
        return new DirectExchange(SAGA_DIRECT_EXCHANGE);
    }

    // Declare all direct queues
    @Bean Queue balanceUpdateQueue()            { return QueueBuilder.durable(BALANCE_UPDATE_QUEUE).build(); }
    @Bean Queue assetUpdateQueue()              { return QueueBuilder.durable(ASSET_UPDATE_QUEUE).build(); }
    @Bean Queue balanceUpdatedQueue()           { return QueueBuilder.durable(BALANCE_UPDATED_QUEUE).build(); }
    @Bean Queue balanceUpdateFailedQueue()      { return QueueBuilder.durable(BALANCE_UPDATE_FAILED_QUEUE).build(); }
    @Bean Queue assetUpdatedQueue()             { return QueueBuilder.durable(ASSET_UPDATED_QUEUE).build(); }
    @Bean Queue assetUpdateFailedQueue()        { return QueueBuilder.durable(ASSET_UPDATE_FAILED_QUEUE).build(); }

    // Bind commands
    @Bean Binding bindBalanceUpdate() {
        return BindingBuilder.bind(balanceUpdateQueue())
                .to(sagaDirectExchange())
                .with("balance.update");
    }
    @Bean Binding bindAssetUpdate() {
        return BindingBuilder.bind(assetUpdateQueue())
                .to(sagaDirectExchange())
                .with("asset.update");
    }

    // Bind responses
    @Bean Binding bindBalanceUpdated() {
        return BindingBuilder.bind(balanceUpdatedQueue())
                .to(sagaDirectExchange())
                .with("balance.updated");
    }
    @Bean Binding bindBalanceUpdateFailed() {
        return BindingBuilder.bind(balanceUpdateFailedQueue())
                .to(sagaDirectExchange())
                .with("balance.update.failed");
    }
    @Bean Binding bindAssetUpdated() {
        return BindingBuilder.bind(assetUpdatedQueue())
                .to(sagaDirectExchange())
                .with("asset.updated");
    }
    @Bean Binding bindAssetUpdateFailed() {
        return BindingBuilder.bind(assetUpdateFailedQueue())
                .to(sagaDirectExchange())
                .with("asset.update.failed");
    }

    // ========================
    // 2. TOPIC EXCHANGE (Final Outcome Events)
    // ========================
    public static final String SAGA_TOPIC_EXCHANGE      = "saga.topic.exchange";

    @Bean
    public TopicExchange sagaTopicExchange() {
        return new TopicExchange(SAGA_TOPIC_EXCHANGE);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}