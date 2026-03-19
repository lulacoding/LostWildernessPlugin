# AWS Bedrock Setup for OpenClaw (PowerShell)
# Run this before starting OpenClaw: .\setup-bedrock.ps1

# Set your AWS credentials here
$env:AWS_ACCESS_KEY_ID = "your-access-key-id-here"
$env:AWS_SECRET_ACCESS_KEY = "your-secret-access-key-here"
$env:AWS_REGION = "us-east-1"  # Change if using different region

# Optional: If using AWS Profile instead
# $env:AWS_PROFILE = "your-profile-name"

Write-Host "AWS Bedrock environment variables set:" -ForegroundColor Green
Write-Host "  AWS_REGION: $env:AWS_REGION"
Write-Host "  AWS_ACCESS_KEY_ID: $($env:AWS_ACCESS_KEY_ID.Substring(0,8))..."
Write-Host ""
Write-Host "You can now run: openclaw onboard"
