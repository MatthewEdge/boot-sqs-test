package com.example.demo;

import java.util.Arrays;

import javax.annotation.PostConstruct;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.cloud.aws.messaging.config.annotation.NotificationMessage;
import org.springframework.cloud.aws.messaging.config.annotation.NotificationSubject;
import org.springframework.cloud.aws.messaging.config.annotation.SqsConfiguration;
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
    throw new RuntimeException("Fuck your message");
  }

  // Config required?
  @Value("${cloud.aws.endpoint.uri}")
  private String endpointUrl;

  @Value("${cloud.aws.region.static}")
  private String region;

  private AWSCredentialsProvider credentialsProvider() {
      return new AWSStaticCredentialsProvider(new BasicAWSCredentials("foo", "bar"));
  }

  private EndpointConfiguration endpointConfiguration() {
    log.info("Using endpoint: " + endpointUrl);
    log.info("Region: " + region);
    return new AwsClientBuilder.EndpointConfiguration(endpointUrl, region);
  }

  @Bean
  public AmazonSQS amazonSQS() {
    return AmazonSQSAsyncClientBuilder.standard()
        .withCredentials(credentialsProvider())
        .withEndpointConfiguration(endpointConfiguration())
        .build();
  }

  @Bean
  public AmazonSNS amazonSNS() {
    return AmazonSNSClientBuilder.standard()
        .withCredentials(credentialsProvider())
        .withEndpointConfiguration(endpointConfiguration())
        .build();
  }


  @Autowired AmazonSQS sqs;
  @Autowired AmazonSNS sns;


  // Create required Queue, Topic, and create subscription between them, if they don't exist
  @PostConstruct
  public void init() {
    var queue = sqs.createQueue("test");
    var topic = sns.createTopic("test");

    var queueArn = sqs.getQueueAttributes(queue.getQueueUrl(), Arrays.asList("QueueArn")).getAttributes().get("QueueArn");
    var existing = sns.listSubscriptions("").getSubscriptions().stream()
        .anyMatch(sub -> sub.getEndpoint().contentEquals(queueArn));

    if(!existing) {
      sns.subscribe(topic.getTopicArn(), "sqs", queueArn);
    }

  }

}
