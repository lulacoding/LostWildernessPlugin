# AWS Bedrock Setup for OpenClaw (PowerShell) - Bearer Token Method
# Run this before starting OpenClaw: .\setup-bedrock-bearer.ps1

# Set your AWS Bedrock bearer token
$env:AWS_BEARER_TOKEN_BEDROCK = "ABSKQmVkcm9ja0FQSUtleS1nbXBuLWF0LTQxNjY4OTQxOTgzODpYbjZoYTJkcDJDaDJrc3RkZTUrSjEyOE85STgwYnhlUGl5SXVtRnNkRENqcnh4VWlZOWVXU3g0UDNaVT0="
$env:AWS_REGION = "ap-southeast-2"
$env:AWS_REGION = "ap-southeast-2"  # Change if using different region

Write-Host "AWS Bedrock environment variables set:" -ForegroundColor Green
Write-Host "  AWS_REGION: $env:AWS_REGION"
Write-Host "  AWS_BEARER_TOKEN_BEDROCK: $($env:AWS_BEARER_TOKEN_BEDROCK.Substring(0,10))...***"
Write-Host ""
Write-Host "You can now run: openclaw onboard"
