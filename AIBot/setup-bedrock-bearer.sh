#!/bin/bash
# AWS Bedrock Setup for OpenClaw - Bearer Token Method
# Run this before starting OpenClaw: source ./setup-bedrock-bearer.sh

# Set your AWS Bedrock bearer token
export AWS_BEARER_TOKEN_BEDROCK="your-bearer-token-here"
export AWS_REGION="us-east-1"  # Change if using different region

echo "AWS Bedrock environment variables set:"
echo "  AWS_REGION: $AWS_REGION"
echo "  AWS_BEARER_TOKEN_BEDROCK: ${AWS_BEARER_TOKEN_BEDROCK:0:10}...***"
echo ""
echo "You can now run: openclaw onboard"
