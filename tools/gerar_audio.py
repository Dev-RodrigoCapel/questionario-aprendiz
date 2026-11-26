"""Gera www/audio/*.ogg com a voz neural Piper (offline, gratuita).

Variáveis de ambiente:
  VOZ          modelo Piper (padrão pt_BR-cadu-medium, masculina e grave)
  VOZES_DIR    pasta com os .onnx baixados (padrão .vozes)
  RITMO        length_scale; >1 fala mais devagar/compassado (padrão 1.18)
  TOM          fator de tom; <1 deixa a voz mais grave (padrão 0.93)
  PAUSA_FRASE  silêncio entre frases, em segundos (padrão 0.4)
  MP3_DIR      se definido, grava também .mp3 (versão web/iPhone) nessa pasta,
               junto com silencio1..15.mp3 usados como pausas no player web
"""
import json, os, subprocess, sys, tempfile, wave
from piper import PiperVoice, SynthesisConfig

VOZ = os.environ.get("VOZ", "pt_BR-cadu-medium")
DIR = os.environ.get("VOZES_DIR", ".vozes")
RITMO = float(os.environ.get("RITMO", "1.18"))
TOM = float(os.environ.get("TOM", "0.93"))
PAUSA = float(os.environ.get("PAUSA_FRASE", "0.4"))
OUT = sys.argv[1] if len(sys.argv) > 1 else "www/audio"
MP3_DIR = os.environ.get("MP3_DIR")


def sintetizar(voice, texto, destino, mp3=None):
    pcm, sr = bytearray(), 22050
    for ch in voice.synthesize(texto, SynthesisConfig(length_scale=RITMO)):
        sr = ch.sample_rate
        pcm += ch.audio_int16_bytes
        pcm += b"\0\0" * int(sr * PAUSA)
    with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as t:
        wav = t.name
    with wave.open(wav, "wb") as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(sr); w.writeframes(bytes(pcm))
    filtro = f"asetrate={int(sr * TOM)},aresample={sr},atempo={1 / TOM:.4f},loudnorm=I=-16:TP=-1.5"
    cmd = ["ffmpeg", "-loglevel", "error", "-y", "-i", wav, "-af", filtro,
           "-ac", "1", "-c:a", "libopus", "-b:a", "40k", destino]
    if mp3:
        cmd += ["-af", filtro, "-ac", "1", "-c:a", "libmp3lame", "-b:a", "64k", mp3]
    subprocess.run(cmd, check=True)
    os.unlink(wav)


def main():
    voice = PiperVoice.load(os.path.join(DIR, f"{VOZ}.onnx"))
    os.makedirs(OUT, exist_ok=True)
    if os.environ.get("AMOSTRA"):
        sintetizar(voice, os.environ["AMOSTRA"], os.path.join(OUT, f"amostra-{VOZ}.ogg"))
        return
    itens = json.loads(subprocess.check_output(["node", "tools/textos.mjs"]))
    if MP3_DIR:
        os.makedirs(MP3_DIR, exist_ok=True)
        for sec in range(1, 16):
            subprocess.run(["ffmpeg", "-loglevel", "error", "-y", "-f", "lavfi", "-t", str(sec), "-i",
                            "anullsrc=r=22050:cl=mono", "-c:a", "libmp3lame", "-b:a", "32k",
                            os.path.join(MP3_DIR, f"silencio{sec}.mp3")], check=True)
    for i, it in enumerate(itens, 1):
        mp3 = os.path.join(MP3_DIR, it["file"].replace(".ogg", ".mp3")) if MP3_DIR else None
        sintetizar(voice, it["text"], os.path.join(OUT, it["file"]), mp3)
        if i % 25 == 0:
            print(f"{i}/{len(itens)} arquivos", flush=True)
    print(f"Pronto: {len(itens)} arquivos em {OUT} com {VOZ}")


if __name__ == "__main__":
    main()
