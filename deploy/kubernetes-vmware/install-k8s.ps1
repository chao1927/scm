[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^\d{1,3}(\.\d{1,3}){3}$')]
    [string]$ControlPlaneIp,

    [Parameter(Mandatory = $true)]
    [ValidateCount(2, 2)]
    [string[]]$WorkerIps,

    [string]$SshUser = "ubuntu",
    [string]$KubernetesMinor = "v1.36",
    [string]$PodCidr = "10.244.0.0/16"
)

$ErrorActionPreference = "Stop"
$NodeScript = Join-Path $PSScriptRoot "k8s-node.sh"
$SshOptions = @("-o", "StrictHostKeyChecking=accept-new", "-o", "ConnectTimeout=10", "-o", "ServerAliveInterval=15")
$AllNodes = @(
    @{ Ip = $ControlPlaneIp; Name = "k8s-control"; Role = "control-plane" },
    @{ Ip = $WorkerIps[0]; Name = "k8s-worker1"; Role = "worker" },
    @{ Ip = $WorkerIps[1]; Name = "k8s-worker2"; Role = "worker" }
)

function Assert-LastExitCode([string]$Action) {
    if ($LASTEXITCODE -ne 0) {
        throw "$Action 失败，退出码：$LASTEXITCODE"
    }
}

function Copy-ToNode([string]$Ip, [string]$LocalPath, [string]$RemotePath) {
    & scp @SshOptions $LocalPath "${SshUser}@${Ip}:${RemotePath}"
    Assert-LastExitCode "复制 $LocalPath 到 $Ip"
}

function Invoke-Node([string]$Ip, [string]$Command, [switch]$NoTty) {
    if ($NoTty) {
        & ssh @SshOptions "${SshUser}@${Ip}" $Command
    } else {
        & ssh @SshOptions -t "${SshUser}@${Ip}" $Command
    }
    Assert-LastExitCode "在 $Ip 执行远程命令"
}

Write-Host "`n[1/6] 本地前置检查" -ForegroundColor Cyan
if (-not (Test-Path $NodeScript)) {
    throw "找不到节点脚本：$NodeScript"
}
foreach ($Command in @("ssh", "scp")) {
    if (-not (Get-Command $Command -ErrorAction SilentlyContinue)) {
        throw "Windows 未安装 OpenSSH Client，缺少命令：$Command"
    }
}
if (($AllNodes.Ip | Sort-Object -Unique).Count -ne 3) {
    throw "三个节点 IP 必须互不相同"
}
foreach ($Node in $AllNodes) {
    Write-Host "检查 SSH：$($Node.Name) $($Node.Ip)"
    if (-not (Test-NetConnection -ComputerName $Node.Ip -Port 22 -InformationLevel Quiet)) {
        throw "无法连接 $($Node.Ip):22；请检查虚拟机 IP、SSH 服务和 VMware 网络"
    }
}

Write-Host "`n[2/6] 上传节点安装脚本" -ForegroundColor Cyan
foreach ($Node in $AllNodes) {
    Copy-ToNode $Node.Ip $NodeScript "/tmp/k8s-node.sh"
}

Write-Host "`n[3/6] 准备并初始化控制平面（需要输入该虚拟机的 sudo 密码）" -ForegroundColor Cyan
$MasterCommand = "sed -i 's/\r$//' /tmp/k8s-node.sh && sudo bash /tmp/k8s-node.sh prepare k8s-control $KubernetesMinor && sudo bash /tmp/k8s-node.sh init $ControlPlaneIp $PodCidr"
Invoke-Node $ControlPlaneIp $MasterCommand

Write-Host "`n[4/6] 获取有效期 2 小时的节点加入命令" -ForegroundColor Cyan
$JoinFile = Join-Path ([System.IO.Path]::GetTempPath()) "k8s-join-$PID.sh"
try {
    & scp @SshOptions "${SshUser}@${ControlPlaneIp}:~/k8s-join.sh" $JoinFile
    Assert-LastExitCode "下载 kubeadm join 命令"

    Write-Host "`n[5/6] 安装并加入两个工作节点（每台需要输入一次 sudo 密码）" -ForegroundColor Cyan
    for ($Index = 0; $Index -lt 2; $Index++) {
        $WorkerIp = $WorkerIps[$Index]
        $WorkerName = "k8s-worker$($Index + 1)"
        Copy-ToNode $WorkerIp $JoinFile "/tmp/k8s-join.sh"
        $WorkerCommand = "sed -i 's/\r$//' /tmp/k8s-node.sh && sudo bash /tmp/k8s-node.sh prepare $WorkerName $KubernetesMinor && sudo bash /tmp/k8s-node.sh join /tmp/k8s-join.sh"
        Invoke-Node $WorkerIp $WorkerCommand
    }
} finally {
    if (Test-Path $JoinFile) {
        Remove-Item -Force $JoinFile
    }
}

Write-Host "`n[6/6] 等待集群就绪并执行 nginx 冒烟测试" -ForegroundColor Cyan
Invoke-Node $ControlPlaneIp "bash /tmp/k8s-node.sh verify"

Write-Host "`n全部完成。登录控制平面后执行：kubectl get nodes -o wide" -ForegroundColor Green
