[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^\d{1,3}(\.\d{1,3}){3}$')]
    [string]$ControlPlaneIp,

    [ValidatePattern('^[A-Za-z][A-Za-z0-9_.-]{2,31}$')]
    [string]$NginxUser = "scm_nginx",

    [string]$SshUser = "ubuntu"
)

$ErrorActionPreference = "Stop"
$SshOptions = @("-o", "StrictHostKeyChecking=accept-new", "-o", "ConnectTimeout=10", "-o", "ServerAliveInterval=15")
$Manifest = Join-Path $PSScriptRoot "nginx-k8s.yaml"
$RemoteManifest = "/tmp/nginx-k8s-$PID.yaml"
$RemoteAuthFile = "/tmp/nginx-auth-$PID.htpasswd"

function Assert-LastExitCode([string]$Action) {
    if ($LASTEXITCODE -ne 0) {
        throw "$Action 失败，退出码：$LASTEXITCODE"
    }
}

function Convert-SecureToPlain([System.Security.SecureString]$Secure) {
    $Bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($Secure)
    try {
        return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($Bstr)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($Bstr)
    }
}

function Read-NginxPassword {
    $Plain = Convert-SecureToPlain (Read-Host "Nginx $NginxUser 密码" -AsSecureString)
    $Confirm = Convert-SecureToPlain (Read-Host "Nginx $NginxUser 密码（再次输入）" -AsSecureString)
    if ($Plain -cne $Confirm) {
        throw "两次输入的 Nginx 密码不一致"
    }
    if ($Plain.Length -lt 24 -or $Plain.Length -gt 64 -or $Plain -notmatch '^[A-Za-z0-9]+$') {
        throw "Nginx 密码必须为 24～64 位，只能包含大小写字母和数字"
    }
    return $Plain
}

function Invoke-ControlPlane([string]$Command, [string]$Action) {
    & ssh @SshOptions -t "${SshUser}@${ControlPlaneIp}" $Command
    Assert-LastExitCode $Action
}

function Invoke-ControlPlaneWithInput([string]$Content, [string]$Command, [string]$Action) {
    $Result = $Content | & ssh @SshOptions "${SshUser}@${ControlPlaneIp}" $Command
    Assert-LastExitCode $Action
    return $Result
}

Write-Host "`n[1/7] 检查本地文件、OpenSSH 和 control-plane 连通性" -ForegroundColor Cyan
foreach ($Command in @("ssh", "scp")) {
    if (-not (Get-Command $Command -ErrorAction SilentlyContinue)) {
        throw "Windows 缺少 OpenSSH Client 命令：$Command"
    }
}
if (-not (Test-Path $Manifest)) {
    throw "找不到 Kubernetes 清单：$Manifest"
}
if (-not (Test-NetConnection -ComputerName $ControlPlaneIp -Port 22 -InformationLevel Quiet)) {
    throw "无法连接 ${ControlPlaneIp}:22"
}

Write-Host "`n[2/7] 输入 HTTP Basic Authentication 密码" -ForegroundColor Cyan
$NginxPassword = Read-NginxPassword

try {
    Write-Host "`n[3/7] 检查 Kubernetes 和 OpenSSL" -ForegroundColor Cyan
    Invoke-ControlPlane "kubectl cluster-info >/dev/null && command -v openssl >/dev/null" "检查 Kubernetes/OpenSSL"

    Write-Host "`n[4/7] 上传 Nginx Kubernetes 清单" -ForegroundColor Cyan
    & scp @SshOptions $Manifest "${SshUser}@${ControlPlaneIp}:${RemoteManifest}"
    Assert-LastExitCode "上传 nginx-k8s.yaml"

    Write-Host "`n[5/7] 创建命名空间和认证 Secret" -ForegroundColor Cyan
    Invoke-ControlPlane "kubectl create namespace scm-infra --dry-run=client -o yaml | kubectl apply -f -" "创建 scm-infra 命名空间"
    $AuthCommand = "set -eu; umask 077; trap 'rm -f $RemoteAuthFile' EXIT; IFS= read -r secret; secret=`$(printf '%s' `"`$secret`" | tr -d '\r\n'); hash=`$(printf '%s' `"`$secret`" | openssl passwd -6 -stdin); test -n `"`$hash`"; printf '%s:%s\n' '$NginxUser' `"`$hash`" > '$RemoteAuthFile'; kubectl -n scm-infra create secret generic nginx-auth --from-file=auth='$RemoteAuthFile' --dry-run=client -o yaml | kubectl apply -f -"
    $null = Invoke-ControlPlaneWithInput $NginxPassword $AuthCommand "创建 Nginx 认证 Secret"

    Write-Host "`n[6/7] 应用清单并等待滚动部署" -ForegroundColor Cyan
    Invoke-ControlPlane "kubectl apply -f '$RemoteManifest'" "应用 Nginx Kubernetes 资源"
    Invoke-ControlPlane "kubectl -n scm-infra rollout restart deployment/nginx && kubectl -n scm-infra rollout status deployment/nginx --timeout=10m" "等待 Nginx 启动"

    Write-Host "`n[7/7] 执行配置、健康检查和认证验收" -ForegroundColor Cyan
    Invoke-ControlPlane "kubectl -n scm-infra exec deployment/nginx -- nginx -t -c /etc/nginx-custom/nginx.conf" "Nginx 配置验收"
    Invoke-ControlPlane "kubectl -n scm-infra exec deployment/nginx -- command -v curl >/dev/null" "检查 Nginx 镜像内 curl"
    Invoke-ControlPlane "test `"`$(kubectl -n scm-infra exec deployment/nginx -- curl --silent --output /dev/null --write-out '%{http_code}' http://nginx.scm-infra.svc.cluster.local/healthz)`" = 200" "Nginx Service 健康接口验收"
    Invoke-ControlPlane "test `"`$(kubectl -n scm-infra exec deployment/nginx -- curl --silent --output /dev/null --write-out '%{http_code}' http://nginx.scm-infra.svc.cluster.local/)`" = 401" "Nginx Service 未认证访问验收"

    $CurlConfig = "user = `"${NginxUser}:$NginxPassword`"`nsilent`nshow-error`noutput = `"/dev/null`"`nwrite-out = `"%{http_code}`"`n"
    $AuthResult = Invoke-ControlPlaneWithInput $CurlConfig "kubectl -n scm-infra exec -i deployment/nginx -- curl --config - http://nginx.scm-infra.svc.cluster.local/" "Nginx Service 账号密码验收"
    $AuthStatus = ($AuthResult -join "").Trim()
    if ($AuthStatus -ne "200") {
        throw "Nginx 认证访问失败，HTTP 状态码：$AuthStatus"
    }
} finally {
    $NginxPassword = $null
    $CurlConfig = $null
    $AuthResult = $null
    & ssh @SshOptions "${SshUser}@${ControlPlaneIp}" "rm -f '$RemoteManifest' '$RemoteAuthFile'" 2>$null
}

Write-Host "`nNginx 部署成功。" -ForegroundColor Green
Write-Host "集群内地址：http://nginx.scm-infra.svc.cluster.local"
Write-Host "用户名：$NginxUser；密码为本次输入值。"
