$dir = "d:\Java Spring\e-ecommerce\demo\src\main\java\com\example\demo\dto\response"
$files = Get-ChildItem -Path $dir -Filter *.java

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw

    $changed = $false

    # Add import java.io.Serial if not present
    if ($content -notmatch "import\s+java\.io\.Serial;") {
        $content = $content -replace "(package\s+[^;]+;)", "`$1`r`nimport java.io.Serial;"
        $changed = $true
    }

    # Add @Serial to serialVersionUID if not already present
    if ($content -notmatch "@Serial\s*private\s+static\s+final\s+long\s+serialVersionUID") {
        $content = [regex]::Replace($content, "(private\s+static\s+final\s+long\s+serialVersionUID\s*=\s*1L;)", "@Serial`r`n    `$1")
        $changed = $true
    }

    if ($changed) {
        Set-Content -Path $file.FullName -Value $content
        Write-Host "Added @Serial to $($file.Name)"
    } else {
        Write-Host "Skipped $($file.Name)"
    }
}
