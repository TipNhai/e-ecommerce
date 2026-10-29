$dir = "d:\Java Spring\e-ecommerce\demo\src\main\java\com\example\demo\dto\response"
$files = Get-ChildItem -Path $dir -Filter *.java

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw

    # Check if already implements Serializable
    if ($content -notmatch "implements\s+Serializable") {
        # 1. Add import if not present
        if ($content -notmatch "import\s+java\.io\.Serializable;") {
            $content = $content -replace "(package\s+[^;]+;)", "`$1`r`nimport java.io.Serializable;"
        }

        # 2. Add implements Serializable and serialVersionUID
        # This matches "public class ClassName {" or "public class ClassName<T> {"
        $content = [regex]::Replace($content, "(public\s+class\s+\w+(?:<[^>]+>)?)\s*\{", "`$1 implements Serializable {`r`n    private static final long serialVersionUID = 1L;")
        
        Set-Content -Path $file.FullName -Value $content
        Write-Host "Updated $($file.Name)"
    } else {
        # It implements Serializable, but does it have serialVersionUID?
        if ($content -notmatch "serialVersionUID") {
            $content = [regex]::Replace($content, "(public\s+class\s+\w+(?:<[^>]+>)?.*?implements\s+Serializable[^{]*)\{", "`$1{`r`n    private static final long serialVersionUID = 1L;")
            Set-Content -Path $file.FullName -Value $content
            Write-Host "Added serialVersionUID to $($file.Name)"
        } else {
            Write-Host "Skipped $($file.Name) (Already Serializable)"
        }
    }
}
