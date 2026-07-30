[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^\d{1,3}(\.\d{1,3}){3}$')]
    [string]$MysqlIp,

    [Parameter(Mandatory = $true)]
    [ValidatePattern('^\d{1,3}(\.\d{1,3}){3}$')]
    [string]$RocketMqIp,

    [string]$SshUser = "ubuntu"
)

$ErrorActionPreference = "Stop"
$SshOptions = @("-o", "StrictHostKeyChecking=accept-new", "-o", "ConnectTimeout=10", "-o", "ServerAliveInterval=15")
$MysqlScript = Join-Path $PSScriptRoot "install-mysql.sh"
$RocketMqScript = Join-Path $PSScriptRoot "install-rocketmq.sh"

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

function Read-ValidatedSecret([string]$Prompt, [switch]$AlphaNumericOnly) {
    $Plain = Convert-SecureToPlain (Read-Host $Prompt -AsSecureString)
    $Confirm = Convert-SecureToPlain (Read-Host "$Prompt（再次输入）" -AsSecureString)
    if ($Plain -cne $Confirm) {
        throw "两次输入的密码不一致：$Prompt"
    }
    if ($AlphaNumericOnly) {
        if ($Plain.Length -lt 24 -or $Plain.Length -gt 64 -or $Plain -notmatch '^[A-Za-z0-9]+$') {
            throw "RocketMQ 密码必须为 24～64 位，只能包含大小写字母和数字"
        }
    } elseif ($Plain.Length -lt 12 -or $Plain.Length -gt 64 -or $Plain -notmatch '^[A-Za-z0-9!@#%^*_.-]+$') {
        throw "MySQL 密码必须为 12～64 位，只能包含字母、数字和 !@#%^*_.-"
    }
    return $Plain
}

function Convert-ToBase64([string]$Value) {
    return [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($Value))
}

function Copy-ToNode([string]$Ip, [string]$LocalPath, [string]$RemotePath) {
    & scp @SshOptions $LocalPath "${SshUser}@${Ip}:${RemotePath}"
    Assert-LastExitCode "复制文件到 $Ip"
}

function Invoke-Node([string]$Ip, [string]$Command) {
    & ssh @SshOptions -t "${SshUser}@${Ip}" $Command
    Assert-LastExitCode "在 $Ip 执行远程安装"
}

Write-Host "`n[1/6] 本地和远程连接检查" -ForegroundColor Cyan
foreach ($Command in @("ssh", "scp")) {
    if (-not (Get-Command $Command -ErrorAction SilentlyContinue)) {
        throw "Windows 缺少 OpenSSH Client 命令：$Command"
    }
}
foreach ($File in @($MysqlScript, $RocketMqScript)) {
    if (-not (Test-Path $File)) {
        throw "找不到安装脚本：$File"
    }
}
if ($MysqlIp -eq $RocketMqIp) {
    throw "8 GB 实验节点不建议同时承载 MySQL 和 RocketMQ，请传入两个不同 IP"
}
foreach ($Ip in @($MysqlIp, $RocketMqIp)) {
    if (-not (Test-NetConnection -ComputerName $Ip -Port 22 -InformationLevel Quiet)) {
        throw "无法连接 ${Ip}:22"
    }
}

Write-Host "`n[2/6] 输入账号密码（不会写入仓库）" -ForegroundColor Cyan
$MysqlRootPassword = Read-ValidatedSecret "MySQL root 密码"
$MysqlAppPassword = Read-ValidatedSecret "MySQL scm_app 密码"
$RocketMqAdminPassword = Read-ValidatedSecret "RocketMQ scm_rmq_admin 密码" -AlphaNumericOnly
$RocketMqAppPassword = Read-ValidatedSecret "RocketMQ scm_app 密码" -AlphaNumericOnly

$MysqlConfig = Join-Path ([IO.Path]::GetTempPath()) "mysql-secrets-$PID.env"
$RocketMqConfig = Join-Path ([IO.Path]::GetTempPath()) "rocketmq-secrets-$PID.env"
try {
    @(
        "MYSQL_ROOT_PASSWORD_B64=$(Convert-ToBase64 $MysqlRootPassword)"
        "MYSQL_APP_PASSWORD_B64=$(Convert-ToBase64 $MysqlAppPassword)"
    ) | Set-Content -Encoding Ascii $MysqlConfig
    @(
        "ROCKETMQ_ADMIN_PASSWORD_B64=$(Convert-ToBase64 $RocketMqAdminPassword)"
        "ROCKETMQ_APP_PASSWORD_B64=$(Convert-ToBase64 $RocketMqAppPassword)"
    ) | Set-Content -Encoding Ascii $RocketMqConfig

    Write-Host "`n[3/6] 上传安装文件" -ForegroundColor Cyan
    Copy-ToNode $MysqlIp $MysqlScript "/tmp/install-mysql.sh"
    Copy-ToNode $MysqlIp $MysqlConfig "/tmp/mysql-secrets.env"
    Copy-ToNode $RocketMqIp $RocketMqScript "/tmp/install-rocketmq.sh"
    Copy-ToNode $RocketMqIp $RocketMqConfig "/tmp/rocketmq-secrets.env"

    Write-Host "`n[4/6] 在 $MysqlIp 原生安装 MySQL" -ForegroundColor Cyan
    $MysqlOctets = $MysqlIp.Split('.')
    $VmwareMysqlHost = "$($MysqlOctets[0]).$($MysqlOctets[1]).$($MysqlOctets[2]).0/255.255.255.0"
    $MysqlCommand = "sed -i 's/\r$//' /tmp/install-mysql.sh && chmod 600 /tmp/mysql-secrets.env && sudo bash /tmp/install-mysql.sh /tmp/mysql-secrets.env $MysqlIp $VmwareMysqlHost"
    Invoke-Node $MysqlIp $MysqlCommand

    Write-Host "`n[5/6] 在 $RocketMqIp 原生安装 RocketMQ" -ForegroundColor Cyan
    $RocketMqCommand = "sed -i 's/\r$//' /tmp/install-rocketmq.sh && chmod 600 /tmp/rocketmq-secrets.env && sudo bash /tmp/install-rocketmq.sh /tmp/rocketmq-secrets.env $RocketMqIp"
    Invoke-Node $RocketMqIp $RocketMqCommand
} finally {
    $MysqlRootPassword = $null
    $MysqlAppPassword = $null
    $RocketMqAdminPassword = $null
    $RocketMqAppPassword = $null
    foreach ($File in @($MysqlConfig, $RocketMqConfig)) {
        if (Test-Path $File) {
            Remove-Item -Force $File
        }
    }
}

Write-Host "`n[6/6] 从 Windows 验证端口" -ForegroundColor Cyan
$Checks = @(
    @{ Host = $MysqlIp; Port = 3306; Name = "MySQL" },
    @{ Host = $RocketMqIp; Port = 9876; Name = "RocketMQ NameServer" },
    @{ Host = $RocketMqIp; Port = 10911; Name = "RocketMQ Broker" }
)
foreach ($Check in $Checks) {
    if (-not (Test-NetConnection -ComputerName $Check.Host -Port $Check.Port -InformationLevel Quiet)) {
        throw "$($Check.Name) 端口 $($Check.Host):$($Check.Port) 无法访问"
    }
    Write-Host "通过：$($Check.Name) $($Check.Host):$($Check.Port)" -ForegroundColor Green
}

Write-Host "`nMySQL 与 RocketMQ 原生安装及账号配置全部完成。" -ForegroundColor Green
