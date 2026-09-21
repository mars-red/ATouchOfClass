Add-Type -AssemblyName System.Drawing

$outputDirectory = Join-Path $PSScriptRoot "..\src\main\resources\assets\atouchofclass\textures\block"

function New-OvenWindowTexture {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,

        [Parameter(Mandatory = $true)]
        [bool]$Lit
    )

    $bitmap = [System.Drawing.Bitmap]::new(16, 16)
    try {
        for ($y = 0; $y -lt 16; $y++) {
            for ($x = 0; $x -lt 16; $x++) {
                if ($Lit) {
                    $vertical = 1.0 - ($y / 15.0)
                    $center = 1.0 - [Math]::Min(1.0, [Math]::Abs($x - 7.5) / 8.5)
                    $heat = [Math]::Max(0.0, [Math]::Min(1.0, 0.18 + 0.68 * $vertical + 0.22 * $center))
                    $r = [int](68 + 187 * $heat)
                    $g = [int](30 + 126 * $heat)
                    $b = [int](15 + 20 * $heat)

                    if ($y -ge 10) {
                        $flame = (($x + $y) % 5 -eq 0) -or (($x * 3 + $y) % 7 -eq 0)
                        if ($flame) {
                            $r = 255
                            $g = 196
                            $b = 48
                        }
                    }
                }
                else {
                    $r = 24 + (($x * 5 + $y * 3) % 9)
                    $g = 27 + (($x * 3 + $y * 5) % 10)
                    $b = 30 + (($x * 7 + $y * 2) % 12)
                    if (($x - $y + 18) % 9 -eq 0) {
                        $r += 14
                        $g += 15
                        $b += 16
                    }
                }

                if ($x -eq 0 -or $x -eq 15 -or $y -eq 0 -or $y -eq 15) {
                    $r = [int]($r * 0.42)
                    $g = [int]($g * 0.42)
                    $b = [int]($b * 0.42)
                }

                $bitmap.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(255, $r, $g, $b))
            }
        }
        $bitmap.Save($Path, [System.Drawing.Imaging.ImageFormat]::Png)
    }
    finally {
        $bitmap.Dispose()
    }
}

New-OvenWindowTexture -Path (Join-Path $outputDirectory "oven_window.png") -Lit $false
New-OvenWindowTexture -Path (Join-Path $outputDirectory "oven_window_lit.png") -Lit $true
