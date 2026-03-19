# AWS Bedrock Setup for OpenClaw (PowerShell) - Sydney Region
# Run this before starting OpenClaw: .\setup-bedrock-bearer-sydney.ps1

# Set your AWS Bedrock bearer token
$env:AWS_BEARER_TOKEN_BEDROCK = "your-bearer-token-here"
$env:AWS_REGION = "ap-southeast-2"  # Sydney region

Write-Host "AWS Bedrock environment variables set:" -ForegroundColor Green
Write-Host "  AWS_REGION: $env:AWS_REGION (Sydney)"
Write-Host "  AWS_BEARER_TOKEN_BEDROCK: $($env:AWS_BEARER_TOKEN_BEDROCK.Substring(0,10))...***"
Write-Host ""
Write-Host "You can now run: openclaw onboard"
