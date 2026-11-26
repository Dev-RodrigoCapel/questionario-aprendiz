# Questionário do Aprendiz — app Android

App de estudo do Questionário de Estudos Maçônicos (Grau de Aprendiz): 51 perguntas, modo em sequência
ou aleatório, e leitura em voz alta da pergunta, da pausa para pensar e da resposta.

## Voz (v2)

As falas são gravadas no build com a voz neural **Piper `pt_BR-faber-medium`** (masculina, gratuita, offline),
um pouco mais grave e pausada (`tools/gerar_audio.py`: RITMO, TOM, PAUSA_FRASE). Os `.ogg` vão dentro do APK
(`www/audio/`) e são tocados pelo serviço nativo. Para trocar de voz, mude `VOZ` em `.github/workflows/apk.yml`
(ex.: `pt_BR-cadu-medium`, `pt_BR-jeff-medium`); amostras das três ficam na página Releases.

Localmente: `pip install piper-tts`, baixe a voz com `python -m piper.download_voices --download-dir .vozes pt_BR-faber-medium`
e rode `python tools/gerar_audio.py` antes do `npx cap sync android`.

## Como a reprodução funciona no Android

O app usa um plugin nativo próprio (`Leitor`):

- `LeitorService.java` — serviço em primeiro plano (notificação com botão **Parar**) que toca a fila de áudios
  com MediaPlayer e as pausas entre eles. A leitura segue com a tela apagada.
- `LeitorPlugin.java` — ponte com o JavaScript: `play({items, rate})`, `stop()` e o evento `leitor`
  (`start`/`done`/`end`/`erro`) que sincroniza a tela.
- `www/questoes.js` — as 51 perguntas (texto exibido e texto falado).
- `www/index.html` — o app. Sem Capacitor (navegador comum), cai no `speechSynthesis` do navegador.


## Gerar o APK

**Pelo GitHub (sem instalar nada):** cada push na branch `main` roda `.github/workflows/apk.yml`, que compila
e publica o `Aprendiz.apk` na página **Releases** do repositório. No celular, abra essa página, baixe o APK e
permita "instalar apps desconhecidos" para o navegador.

**Localmente:** Node 22+, JDK 21 e Android SDK (ou Android Studio):

```bash
npm ci
npx cap sync android
cd android && ./gradlew assembleRelease
# APK: android/app/build/outputs/apk/release/app-release.apk
```

Para editar perguntas ou respostas, altere `www/questoes.js` (texto exibido e, se preciso,
um 5º campo com o texto falado), depois `npx cap sync android`.

## Assinatura

`android/app/aprendiz.keystore` (senha `android`) assina todos os builds, para que as atualizações instalem
por cima da versão anterior. Serve para uso pessoal/do grupo. Para publicar na Play Store, gere uma chave
própria e guarde-a fora do repositório.
