param(
    [string]$OutputPath = "preview/elevator_ding.wav"
)

$sampleRate = 48000
$durationSeconds = 1.4
$sampleCount = [int]($sampleRate * $durationSeconds)
$samples = New-Object double[] $sampleCount
$random = [System.Random]::new(481516)

# A compact bronze-bell spectrum: a clear A5 strike with lightly inharmonic overtones.
$partials = @(
    @{ Frequency = 880.00;  Amplitude = 1.00; Decay = 3.2; Phase = 0.10 },
    @{ Frequency = 1768.80; Amplitude = 0.34; Decay = 5.0; Phase = 1.30 },
    @{ Frequency = 2340.80; Amplitude = 0.20; Decay = 6.2; Phase = 2.10 },
    @{ Frequency = 3141.60; Amplitude = 0.12; Decay = 7.4; Phase = 0.60 },
    @{ Frequency = 4215.20; Amplitude = 0.06; Decay = 9.0; Phase = 2.70 }
)

$peak = 0.0
for ($i = 0; $i -lt $sampleCount; $i++) {
    $time = $i / [double]$sampleRate
    $attack = 1.0 - [Math]::Exp(-$time * 260.0)
    $body = 0.0

    foreach ($partial in $partials) {
        $wobble = 0.0012 * [Math]::Exp(-$time * 7.0) * [Math]::Sin(2.0 * [Math]::PI * 5.1 * $time)
        $angle = 2.0 * [Math]::PI * $partial.Frequency * (1.0 + $wobble) * $time + $partial.Phase
        $body += $partial.Amplitude * [Math]::Exp(-$partial.Decay * $time) * [Math]::Sin($angle)
    }

    # A tiny, fast-decaying metallic impact gives the note a believable strike.
    $impact = (($random.NextDouble() * 2.0) - 1.0) * 0.10 * [Math]::Exp(-$time * 70.0)
    $sample = ($attack * $body) + $impact

    # Smoothly fade the final 120 ms to guarantee a click-free endpoint.
    $remaining = $durationSeconds - $time
    if ($remaining -lt 0.12) {
        $sample *= [Math]::Sin(([Math]::PI / 2.0) * ($remaining / 0.12))
    }

    $samples[$i] = $sample
    $peak = [Math]::Max($peak, [Math]::Abs($sample))
}

$outputDirectory = Split-Path -Parent $OutputPath
if ($outputDirectory) {
    New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null
}

$stream = [System.IO.File]::Create($OutputPath)
$writer = [System.IO.BinaryWriter]::new($stream)
try {
    $bytesPerSample = 2
    $dataSize = $sampleCount * $bytesPerSample
    $writer.Write([System.Text.Encoding]::ASCII.GetBytes("RIFF"))
    $writer.Write([int](36 + $dataSize))
    $writer.Write([System.Text.Encoding]::ASCII.GetBytes("WAVE"))
    $writer.Write([System.Text.Encoding]::ASCII.GetBytes("fmt "))
    $writer.Write([int]16)
    $writer.Write([int16]1)
    $writer.Write([int16]1)
    $writer.Write([int]$sampleRate)
    $writer.Write([int]($sampleRate * $bytesPerSample))
    $writer.Write([int16]$bytesPerSample)
    $writer.Write([int16]16)
    $writer.Write([System.Text.Encoding]::ASCII.GetBytes("data"))
    $writer.Write([int]$dataSize)

    $gain = 0.88 / $peak
    foreach ($sample in $samples) {
        $value = [Math]::Max(-1.0, [Math]::Min(1.0, $sample * $gain))
        $writer.Write([int16][Math]::Round($value * 32767.0))
    }
}
finally {
    $writer.Dispose()
    $stream.Dispose()
}

Write-Output "Generated $OutputPath ($durationSeconds seconds, ${sampleRate}Hz mono)"
