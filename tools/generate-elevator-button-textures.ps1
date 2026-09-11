$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.Drawing

$textureDirectory = Join-Path $PSScriptRoot '..\src\main\resources\assets\atouchofclass\textures\block'
$sourcePath = Join-Path $textureDirectory 'brushed_stainless_steel.png'

$colors = @{
    Rim       = [System.Drawing.Color]::FromArgb(255, 48, 52, 53)
    RimLight  = [System.Drawing.Color]::FromArgb(255, 78, 83, 84)
    MetalHigh = [System.Drawing.Color]::FromArgb(255, 238, 242, 242)
    Metal     = [System.Drawing.Color]::FromArgb(255, 168, 174, 175)
    MetalLow  = [System.Drawing.Color]::FromArgb(255, 102, 108, 109)
    DarkHigh  = [System.Drawing.Color]::FromArgb(255, 73, 78, 79)
    Dark      = [System.Drawing.Color]::FromArgb(255, 40, 44, 45)
    DarkLow   = [System.Drawing.Color]::FromArgb(255, 24, 27, 28)
    GreenHigh = [System.Drawing.Color]::FromArgb(255, 190, 255, 164)
    Green     = [System.Drawing.Color]::FromArgb(255, 55, 210, 76)
    GreenLow  = [System.Drawing.Color]::FromArgb(255, 24, 116, 44)
    RedHigh   = [System.Drawing.Color]::FromArgb(255, 255, 185, 174)
    Red       = [System.Drawing.Color]::FromArgb(255, 226, 55, 47)
    RedLow    = [System.Drawing.Color]::FromArgb(255, 132, 24, 25)
}

function Set-PixelPattern {
    param(
        [System.Drawing.Bitmap]$Image,
        [int]$Left,
        [int]$Top,
        [string[]]$Pattern,
        [hashtable]$Palette
    )

    for ($y = 0; $y -lt $Pattern.Count; $y++) {
        for ($x = 0; $x -lt $Pattern[$y].Length; $x++) {
            $key = [string]$Pattern[$y][$x]
            if ($key -ne '.' -and $Palette.ContainsKey($key)) {
                $Image.SetPixel($Left + $x, $Top + $y, $Palette[$key])
            }
        }
    }
}

function New-TextureCopy {
    $source = [System.Drawing.Bitmap]::FromFile($sourcePath)
    try {
        $bounds = [System.Drawing.Rectangle]::new(0, 0, $source.Width, $source.Height)
        return $source.Clone($bounds, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    }
    finally { $source.Dispose() }
}

function Save-Texture {
    param([System.Drawing.Bitmap]$Image, [string]$Name)
    $path = Join-Path $textureDirectory $Name
    $Image.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $Image.Dispose()
    Write-Output $path
}

$buttonPattern = @(
    '..RR...',
    '.RHHD..',
    'RHMMMD.',
    'RMMLLD.',
    '.DLLD..',
    '..DD...'
)
$buttonPalette = @{
    R = $colors.RimLight
    D = $colors.Rim
    H = $colors.MetalHigh
    M = $colors.Metal
    L = $colors.MetalLow
}

$callButtonPattern = @(
    '.RR..',
    'RHHD.',
    'RMMMD',
    '.LLD.',
    '..D..'
)

$lightPattern = @(
    '.RR..',
    'RHHD.',
    'RMMMD',
    '.LLD.',
    '..D..'
)

$interior = New-TextureCopy
Set-PixelPattern $interior 5 5 $buttonPattern $buttonPalette
Save-Texture $interior 'elevator_button_interior.png'

$states = @(
    @{
        Name = 'elevator_call_button_inactive.png'
        Palette = @{ R = $colors.RimLight; D = $colors.Rim; H = $colors.DarkHigh; M = $colors.Dark; L = $colors.DarkLow }
    },
    @{
        Name = 'elevator_call_button_unlocked.png'
        Palette = @{ R = $colors.RimLight; D = $colors.Rim; H = $colors.GreenHigh; M = $colors.Green; L = $colors.GreenLow }
    },
    @{
        Name = 'elevator_call_button_locked.png'
        Palette = @{ R = $colors.RimLight; D = $colors.Rim; H = $colors.RedHigh; M = $colors.Red; L = $colors.RedLow }
    }
)

foreach ($state in $states) {
    $image = New-TextureCopy
    Set-PixelPattern $image 6 2 $lightPattern $state.Palette
    Set-PixelPattern $image 6 9 $callButtonPattern $buttonPalette
    Save-Texture $image $state.Name
}
