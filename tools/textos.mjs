// Lista os textos falados do app (um arquivo de áudio por item) a partir de www/questoes.js.
import { readFileSync } from "node:fs";
import vm from "node:vm";
const ctx = {}; vm.createContext(ctx);
vm.runInContext(readFileSync(new URL("../www/questoes.js", import.meta.url), "utf8"), ctx);
const { Q, falas } = ctx.QUESTOES;
const out = [];
for (const x of Q) {
  const f = falas(x);
  out.push({ file: `q${x.n}.ogg`, text: f.q }, { file: `a${x.n}.ogg`, text: f.a });
  if (f.r) out.push({ file: `r${x.n}.ogg`, text: f.r });
}
process.stdout.write(JSON.stringify(out));
