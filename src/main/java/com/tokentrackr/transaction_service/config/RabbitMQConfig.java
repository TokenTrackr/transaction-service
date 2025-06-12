package com.tokentrackr.transaction_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TRANSACTION_SAGA_EXCHANGE = "transaction.saga.exchange";
    public static final String BALANCE_UPDATE_QUEUE = "balance.update.queue";
    public static final String ASSET_UPDATE_QUEUE = "asset.update.queue";
    public static final String TRANSACTION_COMPLETED_QUEUE = "transaction.completed.queue";
    public static final String TRANSACTION_FAILED_QUEUE = "transaction.failed.queue";

    @Bean
    public TopicExchange transactionSagaExchange() {
        return new TopicExchange(TRANSACTION_SAGA_EXCHANGE);
    }

    @Bean
    public Queue balanceUpdateQueue() {
        return QueueBuilder.durable(BALANCE_UPDATE_QUEUE).build();
    }

    @Bean
    public Queue assetUpdateQueue() {
        return QueueBuilder.durable(ASSET_UPDATE_QUEUE).build();
    }

    @Bean
    public Queue transactionCompletedQueue() {
        return QueueBuilder.durable(TRANSACTION_COMPLETED_QUEUE).build();
    }

    @Bean
    public Queue transactionFailedQueue() {
        return QueueBuilder.durable(TRANSACTION_FAILED_QUEUE).build();
    }

    @Bean
    public Binding balanceUpdateBinding() {
        return BindingBuilder.bind(balanceUpdateQueue())
                .to(transactionSagaExchange())
                .with("balance.update");
    }

    @Bean
    public Binding assetUpdateBinding() {
        return BindingBuilder.bind(assetUpdateQueue())
                .to(transactionSagaExchange())
                .with("asset.update");
    }

    @Bean
    public Binding transactionCompletedBinding() {
        return BindingBuilder.bind(transactionCompletedQueue())
                .to(transactionSagaExchange())
                .with("transaction.completed");
    }

    @Bean
    public Binding transactionFailedBinding() {
        return BindingBuilder.bind(transactionFailedQueue())
                .to(transactionSagaExchange())
                .with("transaction.failed");
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