# Questionário do Aprendiz — app Android

App de estudo do Questionário de Estudos Maçônicos (Grau de Aprendiz): 51 perguntas, modo em sequência
ou aleatório, e leitura em voz alta da pergunta, da pausa para pensar e da resposta.

## Como a voz funciona no Android

A WebView do Android não tem `speechSynthesis`, então o app usa um plugin nativo próprio (`Leitor`):

- `LeitorService.java` — serviço em primeiro plano (notificação com botão **Parar**) que entrega a rodada
  inteira ao TextToSpeech do Android: falas + pausas (`playSilentUtterance`). A leitura segue com a tela apagada.
- `LeitorPlugin.java` — ponte com o JavaScript: `play({items, rate, voice})`, `stop()`, `getVoices()` e o
  evento `leitor` (`start`/`done`/`end`/`erro`) que sincroniza a tela.
- `www/index.html` — o app. Sem Capacitor (navegador comum), cai no `speechSynthesis` do navegador.

Requisito no aparelho: voz **Português (Brasil)** instalada em
Configurações › Acessibilidade (ou Sistema) › Saída de conversão de texto em voz (Google Speech Services).

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

Para editar perguntas ou respostas, altere o array `Q` em `www/index.html` (texto exibido e, se preciso,
um 5º campo com o texto falado), depois `npx cap sync android`.

## Assinatura

`android/app/aprendiz.keystore` (senha `android`) assina todos os builds, para que as atualizações instalem
por cima da versão anterior. Serve para uso pessoal/do grupo. Para publicar na Play Store, gere uma chave
própria e guarde-a fora do repositório.
