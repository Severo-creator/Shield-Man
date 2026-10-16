param([string]$BaseUrl = 'http://localhost:8080')

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Esperar-Api {
    for ($tentativa = 0; $tentativa -lt 45; $tentativa++) {
        try {
            $null = Invoke-RestMethod -Uri "$BaseUrl/mestres?tamanho=1" -TimeoutSec 3
            return
        } catch {
            Start-Sleep -Seconds 2
        }
    }
    throw 'A API nao ficou disponivel no tempo esperado.'
}

$mestreTeste = $null
$escudoTeste = $null
Push-Location (Split-Path -Parent $PSScriptRoot)
try {
    Esperar-Api
    $identificadorTeste = [guid]::NewGuid().ToString()
    $mestreTeste = Invoke-RestMethod -Method Post -Uri "$BaseUrl/mestres" -ContentType 'application/json' `
        -Body (@{ nome = "Teste persistencia $identificadorTeste" } | ConvertTo-Json)
    $escudoTeste = Invoke-RestMethod -Method Post -Uri "$BaseUrl/escudos" -ContentType 'application/json' `
        -Body (@{ mestreId = $mestreTeste.id; nome = "Campanha $identificadorTeste" } | ConvertTo-Json)

    docker compose restart api
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao reiniciar a aplicacao.' }
    Esperar-Api

    $mestreRecuperado = Invoke-RestMethod -Uri "$BaseUrl/mestres/$($mestreTeste.id)"
    $escudoRecuperado = Invoke-RestMethod -Uri "$BaseUrl/escudos/$($escudoTeste.id)"
    if ($mestreRecuperado.id -ne $mestreTeste.id -or $mestreRecuperado.nome -ne $mestreTeste.nome `
        -or $escudoRecuperado.id -ne $escudoTeste.id -or $escudoRecuperado.nome -ne $escudoTeste.nome `
        -or $escudoRecuperado.mestreId -ne $mestreTeste.id) {
        throw 'Os dados recuperados depois do reinicio sao diferentes dos dados gravados.'
    }
    Write-Output 'SUCESSO: mestre, escudo e vinculo preservados apos reiniciar a aplicacao.'
} finally {
    # Remove somente os dois cadastros criados por esta execucao.
    try {
        if ($null -ne $escudoTeste) {
            $null = Invoke-RestMethod -Method Delete -Uri "$BaseUrl/escudos/$($escudoTeste.id)"
        }
        if ($null -ne $mestreTeste) {
            $null = Invoke-RestMethod -Method Delete -Uri "$BaseUrl/mestres/$($mestreTeste.id)"
        }
    } catch {
        Write-Warning "Nao foi possivel limpar os cadastros deste teste: $($_.Exception.Message)"
    }
    Pop-Location
}
