Add-Type -AssemblyName System.Drawing

$outputDirectory = Join-Path $PSScriptRoot "..\src\main\resources\assets\atouchofclass\textures\block"
$ringColor = [System.Drawing.Color]::FromArgb(255, 65, 68, 72)
$ringHighlight = [System.Drawing.Color]::FromArgb(255, 83, 86, 91)
$ringCenter = [System.Drawing.Color]::FromArgb(255, 76, 79, 84)
$controlColor = [System.Drawing.Color]::FromArgb(255, 102, 106, 112)
$ringOffsets = @(
    @(-2, -1), @(-2, 0), @(-1, -2), @(0, -2), @(1, -2),
    @(2, -1), @(2, 0), @(1, 2), @(0, 2), @(-1, 2)
)

function New-GlassSurface {
    param(
        [Parameter(Mandatory = $true)] [int]$GlobalOffset,
        [Parameter(Mandatory = $true)] [int[]]$RingCenters,
        [Parameter(Mandatory = $true)] [array]$Controls,
        [Parameter(Mandatory = $true)] [string]$Path
    )

    $bitmap = [System.Drawing.Bitmap]::new(16, 16)
    try {
        for ($z = 0; $z -lt 16; $z++) {
            for ($x = 0; $x -lt 16; $x++) {
                $globalX = $x + $GlobalOffset
                $variation = ($globalX * 5 + $z * 3) % 6
                $shade = 18 + $variation
                $reflection = ($globalX - $z + 36) % 18
                if ($reflection -ge 2 -and $reflection -le 4) {
                    $shade += 8
                }
                $bitmap.SetPixel($x, $z,
                        [System.Drawing.Color]::FromArgb(255, $shade, $shade + 2, $shade + 4))
            }
        }

        foreach ($globalCenterX in $RingCenters) {
            $cx = $globalCenterX - $GlobalOffset
            if ($cx -lt 0 -or $cx -ge 16) { continue }
            foreach ($cz in @(5, 11)) {
                foreach ($offset in $ringOffsets) {
                    $x = $cx + $offset[0]
                    $z = $cz + $offset[1]
                    if ($x -ge 0 -and $x -lt 16 -and $z -ge 0 -and $z -lt 16) {
                        $color = if ($offset[0] -lt 0 -or $offset[1] -lt 0) {
                            $ringHighlight
                        } else {
                            $ringColor
                        }
                        $bitmap.SetPixel($x, $z, $color)
                    }
                }
                $bitmap.SetPixel($cx, $cz, $ringCenter)
            }
        }

        foreach ($control in $Controls) {
            $x = $control[0] - $GlobalOffset
            $z = $control[1]
            if ($x -ge 0 -and $x -lt 16) {
                $bitmap.SetPixel($x, $z, $controlColor)
            }
        }

        $bitmap.Save($Path, [System.Drawing.Imaging.ImageFormat]::Png)
    }
    finally {
        $bitmap.Dispose()
    }
}

$singleControls = @(@(3, 1), @(4, 1), @(4, 2), @(6, 1), @(8, 1), @(10, 1))
New-GlassSurface -GlobalOffset 0 -RingCenters @(5, 11) -Controls $singleControls `
        -Path (Join-Path $outputDirectory "induction_cooktop.png")

$doubleControls = @(@(13, 1), @(15, 1), @(15, 2), @(16, 1), @(16, 2), @(18, 1))
New-GlassSurface -GlobalOffset 0 -RingCenters @(4, 12, 20, 28) -Controls $doubleControls `
        -Path (Join-Path $outputDirectory "induction_cooktop_left.png")
New-GlassSurface -GlobalOffset 16 -RingCenters @(4, 12, 20, 28) -Controls $doubleControls `
        -Path (Join-Path $outputDirectory "induction_cooktop_right.png")
