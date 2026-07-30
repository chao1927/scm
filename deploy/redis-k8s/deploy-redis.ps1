[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^\d{1,3}(\.\d{1,3}){3}$')]
    [string]$ControlPlaneIp,

    [Parameter(Mandatory = $true)]
    [ValidatePattern('^\d{1,3}(\.\d{1,3}){3}$')]
    [string]$StorageNodeIp,

    [string]$StorageNodeName = "k8s-worker1",

    [string]$SshUser = "ubuntu"
)

$ErrorActionPreference = "Stop"
$SshOptions = @("-o", "StrictHostKeyChecking=accept-new", "-o", "ConnectTimeout=10", "-o", "ServerAliveInterval=15")
$ManifestTemplate = Join-Path $PSScriptRoot "redis-k8s.yaml"
$RemoteManifest = "/tmp/redis-k8s-$PID.yaml"
$RemoteSecret = "/tmp/redis-auth-$PID.yaml"

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

function Read-RedisPassword {
    $Plain = Convert-SecureToPlain (Read-Host "Redis scm_app 密码" -AsSecureString)
    $Confirm = Convert-SecureToPlain (Read-Host "Redis scm_app 密码（再次输入）" -AsSecureString)
    if ($Plain -cne $Confirm) {
        throw "两次输入的 Redis 密码不一致"
    }
    if ($Plain.Length -lt 24 -or $Plain.Length -gt 64 -or $Plain -notmatch '^[A-Za-z0-9]+$') {
        throw "Redis 密码必须为 24～64 位，只能包含大小写字母和数字"
    }
    return $Plain
}

function Invoke-Node([string]$Ip, [string]$Command, [string]$Action) {
    & ssh @SshOptions -t "${SshUser}@${Ip}" $Command
    Assert-LastExitCode $Action
}

function Copy-ToControlPlane([string]$LocalPath, [string]$RemotePath) {
    & scp @SshOptions $LocalPath "${SshUser}@${ControlPlaneIp}:${RemotePath}"
    Assert-LastExitCode "上传 $(Split-Path $LocalPath -Leaf)"
}

function Send-SecretToControlPlane([string]$Content, [string]$RemotePath) {
    $Content | & ssh @SshOptions "${SshUser}@${ControlPlaneIp}" "umask 077; cat > '$RemotePath'"
    Assert-LastExitCode "通过 SSH 发送临时 Secret"
}

Write-Host "`n[1/7] 检查本地文件、OpenSSH 和连通性" -ForegroundColor Cyan
foreach ($Command in @("ssh", "scp")) {
    if (-not (Get-Command $Command -ErrorAction SilentlyContinue)) {
        throw "Windows 缺少 OpenSSH Client 命令：$Command"
    }
}
if (-not (Test-Path $ManifestTemplate)) {
    throw "找不到 Kubernetes 清单：$ManifestTemplate"
}
foreach ($Ip in @($ControlPlaneIp, $StorageNodeIp)) {
    if (-not (Test-NetConnection -ComputerName $Ip -Port 22 -InformationLevel Quiet)) {
        throw "无法连接 ${Ip}:22"
    }
}
if ($StorageNodeName -notmatch '^[a-z0-9]([-a-z0-9.]*[a-z0-9])?$') {
    throw "StorageNodeName 不是合法的 Kubernetes 节点名"
}

Write-Host "`n[2/7] 输入 Redis 应用账号密码（不会写入仓库）" -ForegroundColor Cyan
$RedisPassword = Read-RedisPassword
$RenderedManifest = Join-Path ([IO.Path]::GetTempPath()) "redis-k8s-$PID.yaml"

try {
    $ManifestText = (Get-Content -Raw -Encoding UTF8 $ManifestTemplate).Replace("__STORAGE_NODE_NAME__", $StorageNodeName)
    Set-Content -Path $RenderedManifest -Value $ManifestText -Encoding UTF8

    $PasswordBase64 = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($RedisPassword))
    $Sha256 = [Security.Cryptography.SHA256]::Create()
    try {
        $PasswordHashBytes = $Sha256.ComputeHash([Text.Encoding]::UTF8.GetBytes($RedisPassword))
    } finally {
        $Sha256.Dispose()
    }
    $PasswordHash = -join ($PasswordHashBytes | ForEach-Object { $_.ToString("x2") })
    $AclText = "user default off`nuser scm_app reset on #$PasswordHash ~scm:* &scm:* +@read +@write +@connection +@transaction +@pubsub +@scripting -@admin -@dangerous +info`n"
    $AclBase64 = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($AclText))
    $SecretYaml = @"
apiVersion: v1
kind: Secret
metadata:
  name: redis-auth
  namespace: scm-infra
  labels:
    app.kubernetes.io/name: redis
    app.kubernetes.io/part-of: scm
type: Opaque
data:
  password: $PasswordBase64
  users.acl: $AclBase64
"@

    Write-Host "`n[3/7] 检查集群节点" -ForegroundColor Cyan
    Invoke-Node $ControlPlaneIp "kubectl get node '$StorageNodeName'" "检查 Kubernetes 存储节点"
    $ReadyCommand = "test `"`$(kubectl get node '$StorageNodeName' -o jsonpath='{.status.conditions[?(@.type==`"Ready`")].status}')`" = True"
    Invoke-Node $ControlPlaneIp $ReadyCommand "检查节点 Ready 状态"

    Write-Host "`n[4/7] 在 $StorageNodeName 创建持久化目录" -ForegroundColor Cyan
    $StorageCommand = "sudo install -d -o 10001 -g 10001 -m 0750 /var/lib/k8s-local-storage/redis"
    Invoke-Node $StorageNodeIp $StorageCommand "准备 Redis 持久化目录"

    Write-Host "`n[5/7] 上传清单和临时 Secret" -ForegroundColor Cyan
    Copy-ToControlPlane $RenderedManifest $RemoteManifest
    Send-SecretToControlPlane $SecretYaml $RemoteSecret

    Write-Host "`n[6/7] 部署 Redis 并等待就绪" -ForegroundColor Cyan
    $ExistingPvCheck = "existing=`$(kubectl get pv redis-data-pv -o jsonpath='{.spec.nodeAffinity.required.nodeSelectorTerms[0].matchExpressions[0].values[0]}' 2>/dev/null || true); test -z `"`$existing`" -o `"`$existing`" = '$StorageNodeName'"
    Invoke-Node $ControlPlaneIp $ExistingPvCheck "检查现有 Redis PV 的绑定节点"
    $ApplyCommand = "kubectl create namespace scm-infra --dry-run=client -o yaml | kubectl apply -f - && chmod 600 '$RemoteSecret' && kubectl apply -f '$RemoteSecret' && kubectl apply -f '$RemoteManifest'"
    Invoke-Node $ControlPlaneIp $ApplyCommand "应用 Redis Kubernetes 资源"
    Invoke-Node $ControlPlaneIp "kubectl -n scm-infra rollout restart statefulset/redis && kubectl -n scm-infra rollout status statefulset/redis --timeout=10m" "等待 Redis 启动"

    Write-Host "`n[7/7] 执行账号、读写和持久化验收" -ForegroundColor Cyan
    Invoke-Node $ControlPlaneIp "kubectl -n scm-infra exec redis-0 -- redis-cli --user scm_app SET scm:install:probe ok EX 300" "Redis SET 验收"
    Invoke-Node $ControlPlaneIp "test `"`$(kubectl -n scm-infra exec redis-0 -- redis-cli --user scm_app --raw GET scm:install:probe)`" = ok" "Redis GET 验收"
    $AnonymousCheck = "result=`$(kubectl -n scm-infra exec redis-0 -- sh -c 'unset REDISCLI_AUTH; redis-cli --raw PING' 2>&1 || true); echo `"`$result`" | grep -q NOAUTH"
    Invoke-Node $ControlPlaneIp $AnonymousCheck "Redis 默认用户关闭验收"
    $AclCheck = "result=`$(kubectl -n scm-infra exec redis-0 -- redis-cli --user scm_app --raw SET forbidden:install:probe denied 2>&1 || true); echo `"`$result`" | grep -q NOPERM"
    Invoke-Node $ControlPlaneIp $AclCheck "Redis ACL 键前缀验收"
    Invoke-Node $ControlPlaneIp "kubectl -n scm-infra exec redis-0 -- redis-cli --user scm_app INFO persistence | grep -q 'aof_enabled:1'" "Redis AOF 持久化验收"
} finally {
    $RedisPassword = $null
    $PasswordBase64 = $null
    $PasswordHashBytes = $null
    $PasswordHash = $null
    $AclText = $null
    $AclBase64 = $null
    $SecretYaml = $null
    foreach ($File in @($RenderedManifest)) {
        if (Test-Path $File) {
            Remove-Item -Force $File
        }
    }
    & ssh @SshOptions "${SshUser}@${ControlPlaneIp}" "rm -f '$RemoteManifest' '$RemoteSecret'" 2>$null
}

Write-Host "`nRedis 部署成功。" -ForegroundColor Green
Write-Host "集群内地址：redis.scm-infra.svc.cluster.local:6379"
Write-Host "用户名：scm_app；密码为本次输入值。"
