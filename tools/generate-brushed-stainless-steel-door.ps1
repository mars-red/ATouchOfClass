$ErrorActionPreference = 'Stop'

$sourcePath = Join-Path $PSScriptRoot '..\src\main\resources\assets\atouchofclass\textures\block\brushed_stainless_steel.png'
$outputPath = Join-Path $PSScriptRoot '..\src\main\resources\assets\atouchofclass\textures\block\brushed_stainless_steel_door.png'

Add-Type -AssemblyName System.Drawing

$source = [System.Drawing.Bitmap]::FromFile($sourcePath)
$output = [System.Drawing.Bitmap]::new($source)
$source.Dispose()

try {
    $edgeFactors = @{
        0  = 0.55
        1  = 0.76
        14 = 0.76
        15 = 0.55
    }

    foreach ($x in $edgeFactors.Keys) {
        $factor = $edgeFactors[$x]
        for ($y = 0; $y -lt $output.Height; $y++) {
            $pixel = $output.GetPixel($x, $y)
            $red = [Math]::Round($pixel.R * $factor)
            $green = [Math]::Round($pixel.G * $factor)
            $blue = [Math]::Round($pixel.B * $factor)
            $output.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($pixel.A, $red, $green, $blue))
        }
    }

    # Preserve exact vertical tiling after all color operations.
    for ($x = 0; $x -lt $output.Width; $x++) {
        $output.SetPixel($x, $output.Height - 1, $output.GetPixel($x, 0))
    }

    $output.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
}
finally {
    $output.Dispose()
}

Write-Output $outputPath
