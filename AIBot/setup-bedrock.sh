#!/bin/bash
# AWS Bedrock Setup for OpenClaw
# Run this before starting OpenClaw: source ./setup-bedrock.sh

# Set your AWS credentials here
export AWS_ACCESS_KEY_ID="your-access-key-id-here"
export AWS_SECRET_ACCESS_KEY="your-secret-access-key-here"
export AWS_REGION="us-east-1"  # Change if using different region

# Optional: If using AWS Profile instead
# export AWS_PROFILE="your-profile-name"

echo "AWS Bedrock environment variables set:"
echo "  AWS_REGION: $AWS_REGION"
echo "  AWS_ACCESS_KEY_ID: ${AWS_ACCESS_KEY_ID:0:8}..."
echo ""
echo "You can now run: openclaw onboard"
