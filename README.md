# AWS SQS/SNS with Spring Boot

SNS topic publishes messages to the queue. App listens to the queue for messages and processes them accordingly.

## Testing with Localstack

```
aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name test
aws --endpoint-url=http://localhost:4566 sns create-topic --name test
aws --endpoint-url=http://localhost:4566 sns subscribe --topic-arn arn:aws:sns:us-east-1:000000000000:test --protocol sqs --notification-endpoint arn:aws:sqs:us-east-1:000000000000:test
```

To send messages to the topic:

```
aws --endpoint-url=http://localhost:4566 sns publish --topic-arn arn:aws:sns:us-east-1:000000000000:test --message "TEST"
```

## Docker

```
docker-compose up -d localstack
docker-compose up --build app
```
