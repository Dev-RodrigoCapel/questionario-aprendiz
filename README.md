# Questionário do Aprendiz

App de estudo do Questionário de Estudos Maçônicos (Grau de Aprendiz): 51 perguntas, modo em sequência
ou aleatório, e leitura em voz alta da pergunta, da pausa para pensar e da resposta.

| Plataforma | Como instalar |
|---|---|
| **Android** | Baixe o `Aprendiz.apk` em [Releases](../../releases/tag/apk) e instale (permita "apps desconhecidos"). |
| **iPhone** | Abra **https://dev-rodrigocapel.github.io/questionario-aprendiz/** no Safari › Compartilhar › *Adicionar à Tela de Início*. Passo a passo em [docs/INSTALAR-IPHONE.md](docs/INSTALAR-IPHONE.md). |

Os dois usam a mesma tela (`www/`) e os mesmos áudios gravados; muda só quem toca o som.

| | Android (APK) | iPhone (web instalável) |
|---|---|---|
| Áudio | `.ogg` dentro do APK, tocado pelo serviço nativo | `.mp3` tocados por um `<audio>` encadeado, com arquivos de silêncio nas pausas |
| Tela apagada | continua lendo | **pode parar**; a tela fica acesa sozinha durante a leitura |
| Sem internet | sim | sim, depois da primeira abertura (service worker) |
| Controles fora do app | notificação com **Parar** | tela de bloqueio / central de controle (Media Session) |

## Voz (v2)

As falas são gravadas no build com a voz neural **Piper `pt_BR-cadu-medium`** (masculina, a mais grave das três, gratuita, offline),
um pouco mais grave e pausada (`tools/gerar_audio.py`: RITMO, TOM, PAUSA_FRASE). Os `.ogg` vão dentro do APK
(`www/audio/`) e são tocados pelo serviço nativo. Para trocar de voz, mude `VOZ` em `.github/workflows/apk.yml`
(ex.: `pt_BR-faber-medium`, `pt_BR-jeff-medium`); amostras das três ficam na página Releases.

Localmente: `pip install piper-tts`, baixe a voz com `python -m piper.download_voices --download-dir .vozes pt_BR-cadu-medium`
e rode `python tools/gerar_audio.py` antes do `npx cap sync android`.

## Como a reprodução funciona no Android

O app usa um plugin nativo próprio (`Leitor`):

- `LeitorService.java` — serviço em primeiro plano (notificação com botão **Parar**) que toca a fila de áudios
  com MediaPlayer e as pausas entre eles. A leitura segue com a tela apagada.
- `LeitorPlugin.java` — ponte com o JavaScript: `play({items, rate})`, `stop()` e o evento `leitor`
  (`start`/`done`/`end`/`erro`) que sincroniza a tela.
- `www/questoes.js` — as 51 perguntas (texto exibido e texto falado).
- `www/index.html` — o app. Sem Capacitor (navegador comum), cai no `speechSynthesis` do navegador.


## Versão web (iPhone)

`tools/montar_web.py` monta `site/` a partir de `www/`: troca os áudios para `.mp3`, tira o Capacitor e gera o
`sw.js` com a lista de arquivos para uso offline. O workflow publica `site/` no GitHub Pages (job `web`).

**Configuração única:** em *Settings › Pages › Build and deployment › Source*, escolha **GitHub Actions**.

Testar localmente: gere os áudios com `MP3_DIR=site/audio python tools/gerar_audio.py`, rode
`python tools/montar_web.py` e depois `cd site && python -m http.server`.

## Gerar o APK

**Pelo GitHub (sem instalar nada):** cada push na branch `main` roda `.github/workflows/apk.yml`, que gera os áudios,
compila e publica o `Aprendiz.apk` na página **Releases** e a versão web no GitHub Pages. No celular, abra essa página, baixe o APK e
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
por cima da versão anterior. **Atenção: com o repositório público, essa chave também é pública** — qualquer
pessoa poderia assinar um APK que se passa por atualização deste. Para uso do grupo, instale só o APK baixado
da página Releases deste repositório. Para publicar na Play Store, gere uma chave própria e guarde-a como secret.
