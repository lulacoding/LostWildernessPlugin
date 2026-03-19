# Set AWS Bedrock credentials as SYSTEM environment variables
# Run this in PowerShell as Administrator: .\set-aws-env-system.ps1

Write-Host "Setting AWS Bedrock environment variables..." -ForegroundColor Yellow

# Replace with your actual AWS bearer token
$bearerToken = Read-Host "Enter your AWS_BEARER_TOKEN_BEDROCK"
$region = "ap-southeast-2"  # Sydney region

# Set as system environment variables (available to all services)
[System.Environment]::SetEnvironmentVariable("AWS_BEARER_TOKEN_BEDROCK", $bearerToken, "Machine")
[System.Environment]::SetEnvironmentVariable("AWS_REGION", $region, "Machine")

Write-Host ""
Write-Host "✅ Environment variables set at system level!" -ForegroundColor Green
Write-Host "   AWS_BEARER_TOKEN_BEDROCK: $($bearerToken.Substring(0,10))...***" -ForegroundColor Cyan
Write-Host "   AWS_REGION: $region" -ForegroundColor Cyan
Write-Host ""
Write-Host "⚠️  IMPORTANT: Restart OpenClaw gateway for changes to take effect:" -ForegroundColor Yellow
Write-Host "   openclaw gateway restart" -ForegroundColor White
Write-Host ""
