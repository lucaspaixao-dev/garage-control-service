#!/bin/bash
# Provisions the AWS resources the services expect, inside LocalStack.
# Runs automatically once LocalStack reports ready (ready.d hook).
set -euo pipefail

# FIFO queue (name must end in .fifo); content-based dedup as a safety net on top
# of the per-message deduplication id the publisher sends.
awslocal sqs create-queue \
  --queue-name vehicle-events.fifo \
  --attributes FifoQueue=true,ContentBasedDeduplication=true

# Standard queue: garage-control-service publishes already-processed events here so the
# dashboard service can fan them out to browsers over SSE (order/dedup not required).
awslocal sqs create-queue \
  --queue-name dashboard-events

awslocal dynamodb create-table \
  --table-name webhook_events \
  --attribute-definitions AttributeName=id,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST

echo "LocalStack ready: sqs fifo queue 'vehicle-events.fifo' and dynamodb table 'webhook_events' created"
