"""Monta a versão web instalável (PWA, para iPhone e navegador) em ./site.

Uso: MP3_DIR=site/audio python tools/gerar_audio.py ... (gera os .mp3) e depois
     python tools/montar_web.py
"""
import hashlib, json, os, shutil

WWW, SITE = "www", "site"
IGNORAR = {"capacitor.js", "cordova.js", "cordova_plugins.js", "audio"}

os.makedirs(SITE, exist_ok=True)
for nome in os.listdir(WWW):
    if nome in IGNORAR:
        continue
    origem, destino = os.path.join(WWW, nome), os.path.join(SITE, nome)
    if os.path.isdir(origem):
        shutil.copytree(origem, destino, dirs_exist_ok=True)
    else:
        shutil.copy2(origem, destino)

# a versão web não usa o Capacitor
idx = os.path.join(SITE, "index.html")
with open(idx, encoding="utf-8") as f:
    html = f.read()
with open(idx, "w", encoding="utf-8") as f:
    f.write(html.replace('<script src="capacitor.js"></script>\n', ""))

with open(os.path.join(SITE, "config.js"), "w", encoding="utf-8") as f:
    f.write('window.APP_AUDIO = { ext: "mp3" };\n')

# lista de arquivos para funcionar offline + versão do cache derivada do conteúdo
arquivos, h = [], hashlib.sha256()
for raiz, _, nomes in os.walk(SITE):
    for n in sorted(nomes):
        if n in ("sw.js", ".nojekyll"):
            continue
        caminho = os.path.join(raiz, n)
        rel = os.path.relpath(caminho, SITE).replace(os.sep, "/")
        arquivos.append(rel)
        with open(caminho, "rb") as f:
            h.update(rel.encode()); h.update(f.read())
arquivos.sort()
versao = h.hexdigest()[:12]

sw = f"""// Gerado por tools/montar_web.py — não editar à mão.
const CACHE = "aprendiz-{versao}";
const ARQUIVOS = {json.dumps(["./"] + arquivos)};
self.addEventListener("install", e => {{
  e.waitUntil(caches.open(CACHE).then(c => c.addAll(ARQUIVOS)).then(() => self.skipWaiting()));
}});
self.addEventListener("activate", e => {{
  e.waitUntil(caches.keys().then(ks => Promise.all(ks.filter(k => k !== CACHE).map(k => caches.delete(k))))
    .then(() => self.clients.claim()));
}});
async function parcial(req) {{
  const hit = await caches.match(req.url, {{ ignoreSearch: true }});
  if (!hit) return fetch(req);
  const buf = await hit.arrayBuffer();
  const m = /bytes=(\\d+)-(\\d*)/.exec(req.headers.get("range")) || [];
  const ini = +(m[1] || 0), fim = m[2] ? Math.min(+m[2], buf.byteLength - 1) : buf.byteLength - 1;
  return new Response(buf.slice(ini, fim + 1), {{ status: 206, headers: {{
    "Content-Type": hit.headers.get("Content-Type") || "audio/mpeg",
    "Content-Range": `bytes ${{ini}}-${{fim}}/${{buf.byteLength}}`,
    "Content-Length": String(fim - ini + 1), "Accept-Ranges": "bytes" }} }});
}}
self.addEventListener("fetch", e => {{
  const req = e.request;
  if (req.method !== "GET") return;
  if (req.headers.has("range")) {{ e.respondWith(parcial(req)); return; }} // Safari pede áudio em faixas
  e.respondWith(caches.match(req, {{ ignoreSearch: true }}).then(hit => hit || fetch(req).then(res => {{
    if (res.ok && (new URL(req.url).origin === location.origin || req.url.includes("fonts.g"))) {{
      const copia = res.clone(); caches.open(CACHE).then(c => c.put(req, copia));
    }}
    return res;
  }}).catch(() => caches.match("./"))));
}});
"""
with open(os.path.join(SITE, "sw.js"), "w", encoding="utf-8") as f:
    f.write(sw)
open(os.path.join(SITE, ".nojekyll"), "w").close()
print(f"site/ pronto: {len(arquivos)} arquivos, cache {versao}")
