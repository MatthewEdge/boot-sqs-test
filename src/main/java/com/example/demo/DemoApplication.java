package com.example.demo;

import java.util.Arrays;

import javax.annotation.PostConstruct;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.aws.messaging.config.annotation.NotificationMessage;
import org.springframework.cloud.aws.messaging.config.annotation.NotificationSubject;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class DemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }

  @SqsListener("test")
  public void receiveMessage(@NotificationMessage String message, @Payload String payload) {
    log.info(payload);
    log.info(message);
  }

  // Config required?
  @Value("${aws.endpoint}")
  private String endpointUrl;

  @Value("${cloud.aws.region.static}")
  private String region;

  @Bean
  @Primary
  public AWSCredentialsProvider credentialsProvider() {
      return new EnvironmentVariableCredentialsProvider();
  }

  @Bean
  @Primary
  public EndpointConfiguration endpointConfiguration() {
      log.info(endpointUrl);
      log.info(region);
    return new AwsClientBuilder.EndpointConfiguration(endpointUrl, region);
  }

  @Bean
  @Primary
  public AmazonSQS amazonSQS(AWSCredentialsProvider credentialsProvider) {
    return AmazonSQSAsyncClientBuilder.standard()
        .withCredentials(credentialsProvider)
        .withEndpointConfiguration(endpointConfiguration())
        .build();
  }

  // @Bean
  // public AmazonSNS amazonSNS(AWSCredentialsProvider credentialsProvider, EndpointConfiguration endpointConfiguration) {
    // return AmazonSNSClientBuilder.standard()
        // .withEndpointConfiguration(endpointConfiguration)
        // .withCredentials(credentialsProvider)
        // .build();
  // }


  // @Autowired AmazonSQS sqs;
  // @Autowired AmazonSNS sns;

  // @PostConstruct
  // public void init() {
    // var queue = sqs.createQueue("test");
    // var topic = sns.createTopic("test");

    // var queueArn = sqs.getQueueAttributes(queue.getQueueUrl(), Arrays.asList("QueueArn")).getAttributes().get("QueueArn");
    // var existing = sns.listSubscriptions("").getSubscriptions().stream()
        // .anyMatch(sub -> sub.getEndpoint().contentEquals(queueArn));

    // if(!existing) {
      // sns.subscribe(topic.getTopicArn(), "sqs", queueArn);
    // }

    // // Finally - test message
    // sns.publish(topic.getTopicArn(), "Hello");
  // }

}
