#!/bin/sh
awslocal sqs create-queue --queue-name test
awslocal sns create-topic --name test
awslocal sns subscribe --topic-arn arn:aws:sns:us-east-1:000000000000:test --protocol sqs --notification-endpoint arn:aws:sqs:us-east-1:000000000000:test
