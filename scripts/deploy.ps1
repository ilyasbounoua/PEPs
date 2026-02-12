# ============================================
# PEPs Docker Deployment Script (PowerShell)
# ============================================

param(
    [switch]$SkipHealthChecks = $false
)

$ErrorActionPreference = "Stop"

# Configuration
$ComposeFile = "docker-compose.yml"
$ComposeProdFile = "docker-compose.prod.yml"
$HealthCheckRetries = 30
$HealthCheckInterval = 10

# Functions
function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Green
}

function Write-Warn {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Write-Error-Custom {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

function Check-Prerequisites {
    Write-Info "Checking prerequisites..."
    
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        Write-Error-Custom "Docker is not installed"
        exit 1
    }
    
    if (-not (Get-Command docker-compose -ErrorAction SilentlyContinue)) {
        Write-Error-Custom "Docker Compose is not installed"
        exit 1
    }
    
    Write-Info "Prerequisites check passed"
}

function Check-EnvFile {
    if (-not (Test-Path .env)) {
        Write-Warn ".env file not found, creating from .env.example"
        if (Test-Path .env.example) {
            Copy-Item .env.example .env
            Write-Info "Please configure .env file before deployment"
            exit 0
        } else {
            Write-Error-Custom ".env.example not found"
            exit 1
        }
    }
}

function Build-Services {
    Write-Info "Building Docker images..."
    
    Write-Info "Building backend..."
    docker-compose -f $ComposeFile -f $ComposeProdFile build backend
    
    Write-Info "Building frontend..."
    docker-compose -f $ComposeFile -f $ComposeProdFile build frontend
    
    Write-Info "Building proxy..."
    docker-compose -f $ComposeFile -f $ComposeProdFile build proxy
    
    Write-Info "All images built successfully"
}

function Start-Services {
    Write-Info "Starting services..."
    docker-compose -f $ComposeFile -f $ComposeProdFile up -d
    Write-Info "Services started"
}

function Wait-ForHealth {
    param(
        [string]$ServiceName,
        [string]$Url
    )
    
    Write-Info "Waiting for $ServiceName to be healthy..."
    
    $retries = $HealthCheckRetries
    while ($retries -gt 0) {
        try {
            $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 5
            if ($response.StatusCode -eq 200) {
                Write-Info "$ServiceName is healthy"
                return $true
            }
        } catch {
            # Service not ready yet
        }
        
        $retries--
        Write-Host "." -NoNewline
        Start-Sleep -Seconds $HealthCheckInterval
    }
    
    Write-Error-Custom "$ServiceName failed to become healthy"
    return $false
}

function Test-HealthChecks {
    Write-Info "Running health checks..."
    
    # Wait for backend
    if (-not (Wait-ForHealth "Backend" "http://localhost:8080")) {
        Write-Error-Custom "Backend health check failed"
        Invoke-Rollback
        exit 1
    }
    
    # Wait for frontend
    if (-not (Wait-ForHealth "Frontend" "http://localhost:4200")) {
        Write-Error-Custom "Frontend health check failed"
        Invoke-Rollback
        exit 1
    }
    
    # Wait for proxy
    if (-not (Wait-ForHealth "Proxy" "http://localhost/health")) {
        Write-Error-Custom "Proxy health check failed"
        Invoke-Rollback
        exit 1
    }
    
    Write-Info "All health checks passed"
}

function Invoke-Rollback {
    Write-Warn "Rolling back deployment..."
    docker-compose -f $ComposeFile -f $ComposeProdFile down
    Write-Info "Rollback completed"
}

function Show-Status {
    Write-Info "Deployment Status:"
    docker-compose -f $ComposeFile -f $ComposeProdFile ps
    
    Write-Info "`nService URLs:"
    Write-Host "  - Frontend: http://localhost"
    Write-Host "  - Backend API: http://localhost/api"
    Write-Host "  - Health Check: http://localhost/health"
}

# Main deployment flow
function Main {
    Write-Info "Starting PEPs deployment..."
    
    Check-Prerequisites
    Check-EnvFile
    Build-Services
    Start-Services
    
    if (-not $SkipHealthChecks) {
        Test-HealthChecks
    }
    
    Show-Status
    
    Write-Info "Deployment completed successfully!"
}

# Run main function
Main
