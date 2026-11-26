/* Questionário de Estudos Maçônicos — Grau de Aprendiz.
   Cada linha: [nº, pergunta, referência, resposta exibida, resposta falada?, pergunta falada?]
   Usado pelo app (window.QUESTOES) e pelo gerador de áudio do CI (tools/textos.mjs). */
(function (g) {
const ROWS=[
[1,"Sois Maçom?","p. 31","M:. M:. II:. C:. T:. M:. R:.","Eme. Eme. I, I. Cê. Tê. Eme. Erre."],
[2,"A que é consagrado o Grau de Aprendiz?","p. 47","À Fraternidade Humana, exemplificada na união dos irmãos."],
[3,"Como conseguistes entrar no Templo?","p. 276–277","Por duas pancadas fortes na porta do Templo. Tendo a ponta de uma espada encostada ao peito."],
[4,"Como fostes recebido Maçom?","p. 275","Nem nu, nem vestido. Vendaram-me os olhos, tiraram-me todos os metais, que representavam o egoísmo do mundo profano, e fui conduzido à porta do Templo por um amigo, que depois conheci como Ir:.","Nem nu, nem vestido. Vendaram-me os olhos, tiraram-me todos os metais, que representavam o egoísmo do mundo profano, e fui conduzido à porta do Templo por um amigo, que depois conheci como Irmão."],
[5,"Que fizeram para vos instruir?","p. 280","O V:. M:. tomou-me pela mão direita, deu-me o Toque, a Palavra e toda a instrução do Grau. Fiz, em seguida, a primeira lição na P:. B:. (Pedra Bruta) e, por fim, senti a ventura de ser proclamado Maçom.","O Venerável Mestre tomou-me pela mão direita, deu-me o Toque, a Palavra e toda a instrução do Grau. Fiz, em seguida, a primeira lição na Pedra Bruta e, por fim, senti a ventura de ser proclamado Maçom."],
[6,"Como terminou cada uma das Viagens?","p. 278–279","• 1ª Viagem: Coluna do Sul; três pancadas no ombro direito do 2° Vigilante.\n• 2ª Viagem: Coluna do Norte; três pancadas no ombro direito do 1° Vigilante.\n• 3ª Viagem: Altar do Venerável Mestre, onde bati da mesma forma.","Primeira viagem: Coluna do Sul; três pancadas no ombro direito do Segundo Vigilante. Segunda viagem: Coluna do Norte; três pancadas no ombro direito do Primeiro Vigilante. Terceira viagem: Altar do Venerável Mestre, onde bati da mesma forma."],
[7,"Onde se achavam situadas as Portas?","p. 50","No Ocidente, entre as Colunas J e B.","No Ocidente, entre as Colunas jota e bê."],
[8,"Que vos disseram quando bateste?","p. 276","“Quem vem lá?”\n“Um Candidato que quer ser iniciado na Ordem Maçônica e admitido em seus Augustos Mistérios.”","Perguntaram: Quem vem lá? E foi respondido: Um Candidato que quer ser iniciado na Ordem Maçônica e admitido em seus Augustos Mistérios."],
[9,"Que significam as Purificações?","p. 289","As purificações afirmam que o profano não pode, por si só, alcançar o templo da filosofia."],
[10,"Que vos deram depois?","p. 278","Deram-me um líquido a beber e, inexplicavelmente, o doce se fez amargo à minha revelia, pois o meu pensamento não sofrera mudança."],
[11,"Como vos ligastes à Ordem Maçônica?","p. 304","Por um Juramento e uma Consagração."],
[12,"Que prometestes?","p. 225–227","Reconhecer o GOB como Potência Maçônica Regular, Legal e Legítima, nunca revelar o que ouvir e prestar-lhe obediência quando me for comunicado como segredo da instituição.","Reconhecer o Grande Oriente do Brasil como Potência Maçônica Regular, Legal e Legítima, nunca revelar o que ouvir e prestar-lhe obediência quando me for comunicado como segredo da instituição."],
[13,"Como se reconhece um Maçom?","p. 282","Além dos atos que praticam, revelando o influxo da moral ensinada em nossos Templos, eles se reconhecem entre si pelos Sinais, pelos Toques e pelas Palavras."],
[14,"Como fazeis o Sinal?","p. 99 e 319","Deve ser feito em pé, com os pés em esquadrias, parado e com as mãos desocupadas."],
[15,"Como dais a Palavra?","p. 100 e 314","Ao ouvido direito de um para o outro, soletrando letra por letra, sem pronunciar a Palavra por inteiro."],
[16,"Que significa ela?","p. 99","Significa FORÇA e é representada por uma espiga de trigo.","Significa força, e é representada por uma espiga de trigo."],
[17,"Que é o Toque?","p. 244","É um sinal de reconhecimento entre irmãos."],
[18,"Como estais agora vestido?","p. 281","Estou vestido com este avental."],
[19,"Que significa o Avental?","p. 281","Ele nos lembra que o homem nasceu para o trabalho e que todo Maçom deve trabalhar infatigavelmente para a descoberta da verdade e o aperfeiçoamento da humanidade. O Avental, entre nós, é símbolo da fraternização e, por isso, dois irmãos desavindos não podem absolutamente usá-lo, antes da reconciliação."],
[20,"Qual é a idade do Aprendiz?","p. 22","Três anos."],
[21,"Como se colocam o Esquadro e o Compasso?","p. 101, 180 e 324","Com o Livro da Lei aberto, coloca-se o Esquadro sobre o Compasso, e este na posição do Grau, com as pontas voltadas para o Ocidente. Quando o Livro da Lei estiver fechado, o Esq:. e o Comp:. devem estar colocados sobre o mesmo na posição do Grau, com as pontas voltadas para o Or:.","Com o Livro da Lei aberto, coloca-se o Esquadro sobre o Compasso, e este na posição do Grau, com as pontas voltadas para o Ocidente. Quando o Livro da Lei estiver fechado, o Esquadro e o Compasso devem estar colocados sobre o mesmo na posição do Grau, com as pontas voltadas para o Oriente."],
[22,"Qual o número que domina o Grau do Aprendiz?","p. 292","O número 3. É o triângulo equilátero. A bateria com a marcha e a idade do aprendiz encerram o número 3.","O número três. É o triângulo equilátero. A bateria, com a marcha e a idade do aprendiz, encerram o número três."],
[23,"Qual é o Tempo de Trabalho?","p. 284","Do meio-dia à meia-noite."],
[24,"Quais as ferramentas do Aprendiz?","p. 292","O Maço e o Cinzel."],
[25,"Onde trabalham os Aprendizes?","p. 297","Na Pedra Bruta."],
[26,"Onde fostes recebido Maçom?","p. 296","Em uma Loja Justa, Perfeita e Regular."],
[27,"Que é preciso para que uma Loja seja Justa e Perfeita?","p. 296","Que 3 a Governem, 5 a componham e 7 a completem.","Que três a governem, cinco a componham e sete a completem."],
[28,"Que é uma Loja Regular?","p. 296","É a Loja Justa e Perfeita que obedece à Potência Regular da qual recebeu Carta Constitutiva, praticando rigorosamente os princípios básicos da Maçonaria Universal."],
[29,"Que se faz em vossa Loja?","p. 114","Levantam-se templos à virtude e cavam-se masmorras ao vício."],
[30,"Como é construída nossa Loja?","p. 293–294","Em forma de quadrilongo ou retângulo alongado, soma de três quadrados.\n• Comprimento: Oriente ao Ocidente\n• Largura: Sul ao Norte\n• Altura: Terra ao Céu\n• Profundidade: da superfície ao centro da Terra","Em forma de quadrilongo ou retângulo alongado, soma de três quadrados. Comprimento: do Oriente ao Ocidente. Largura: do Sul ao Norte. Altura: da Terra ao Céu. Profundidade: da superfície ao centro da Terra."],
[31,"Quantos degraus existem entre o piso do Ocidente e do Oriente e quantos conduzem ao Trono? Dizei-lhes os nomes.","p. 301","• Do Ocidente ao Oriente: quatro degraus – FORÇA, TRABALHO, CIÊNCIA e VIRTUDE.\n• Conduzindo ao Trono: três degraus – PUREZA, LUZ e VERDADE.","Do Ocidente ao Oriente: quatro degraus. Força, trabalho, ciência e virtude. Conduzindo ao Trono: três degraus. Pureza, luz e verdade."],
[32,"Quais são os pilares da Loja?","p. 301–302","São três: Sapientia, Salus e Stabilitas, situados no Oriente, Ocidente e Sul."],
[33,"Que significa S:. S:. S:.?","p. 302","Sapientia (Sabedoria), Salus (Força) e Stabilitas (Beleza), que sustentam a Loja.","Sapientia, sabedoria. Salus, força. E Stabilitas, beleza. Que sustentam a Loja.","Que significa esse, esse, esse?"],
[34,"Quais são os Paramentos da Loja?","p. 298","Dois – Delta Luminoso e o Livro da Lei.","Dois. O Delta Luminoso e o Livro da Lei."],
[35,"Quais são os Ornamentos de uma Loja?","p. 299","Três – Pavimento Mosaico, Corda de 81 nós e Compasso-Esquadro.","Três. Pavimento Mosaico, Corda de oitenta e um nós, e Compasso e Esquadro."],
[36,"Quais são as Joias Móveis?","p. 301","Esquadro, Nível e Prumo, que adornam o Venerável e os Vigilantes. E são transferidas aos sucessores."],
[37,"Quais são as Joias Fixas?","p. 300","Pedra Bruta, Pedra Cúbica e Prancha da Loja, sempre presentes como referência de ensino e instrução."],
[38,"Quantas são as Colunas?","p. 300","Duas:\n• J (Jachin): Ocidente, Norte – 1° Vigilante\n• B (Boaz): Ocidente, Sul – 2° Vigilante","Duas. Jota, Jaquim: Ocidente, Norte, Primeiro Vigilante. Bê, Boaz: Ocidente, Sul, Segundo Vigilante."],
[39,"Que representam as Quatro Borlas?","p. 303","Representam as quatro virtudes cardiais: Temperança, Justiça, Coragem e Prudência."],
[40,"Que significa a Espada Flamejante?","p. 300","A Espada Flamejante ou Ondulada é emblema de magistério e instrumento necessário à Consagração do Neófito, pois nela soa a Bateria do Grau."],
[41,"Que é o Livro da Lei?","p. 52, 299","No Rito Brasileiro, é a Bíblia Sagrada, símbolo de verdade espiritual e guia moral. O candidato pode jurar sobre o Livro de sua fé."],
[42,"Que nome damos ao Deus Criador?","p. 300","Supremo Arquiteto do Universo (S:. A:. D:. U:.)","Supremo Arquiteto do Universo."],
[43,"Onde se abre a Bíblia Sagrada?","p. 180","No Altar dos Juramentos, geralmente no Salmo 133, invocando a presença de Deus para abençoar os trabalhos.","No Altar dos Juramentos, geralmente no Salmo cento e trinta e três, invocando a presença de Deus para abençoar os trabalhos."],
[44,"Por que as Lojas Maçônicas são chamadas de “Loja de São João”?","p. 285","Porque São João foi patrono das corporações de construtores na Idade Média. A primeira Grande Loja foi fundada em 24 de junho de 1717, dia de São João Batista."],
[45,"Qual é a Virtude essencial do Maçom?","p. 307","Tolerância, entendida como polimento da Pedra Bruta e aprimoramento pessoal."],
[46,"Que exigiram para serdes Maçom?","p. 274","Ser livre e de bons costumes."],
[47,"Qual é o laço que nos une?","p. 307–308","A Solidariedade, simbolizada pela Corda de 81 nós, fortalecendo o vínculo entre irmãos.","A Solidariedade, simbolizada pela Corda de oitenta e um nós, fortalecendo o vínculo entre irmãos."],
[48,"Como se quebra a Cadeia de União?","p. 314–315","Forma-se no interior do Templo, após o encerramento da Sessão, para transmitir a Palavra Semestral. É quebrada quando há divergência na transmissão."],
[49,"Quais são os fundamentos maçônicos da Solidariedade?","p. 309","Os ensinamentos de nossa Ordem nos obrigam, sim, a proteger nossos IIr:., mas devidamente, em tudo o que for Justo e Honesto. A Solidariedade Maçônica situa-se entre estas duas paralelas: a Justiça e a Honestidade.","Os ensinamentos de nossa Ordem nos obrigam, sim, a proteger nossos Irmãos, mas devidamente, em tudo o que for Justo e Honesto. A Solidariedade Maçônica situa-se entre estas duas paralelas: a Justiça e a Honestidade."],
[50,"A que jurisdição pertence a nossa Loja e qual é o Rito que praticamos?","—","Federada ao Grande Oriente do Brasil (GOB), jurisdicionada pelo GOB/MT e praticante do Rito Brasileiro.","Federada ao Grande Oriente do Brasil, jurisdicionada pelo Grande Oriente do Brasil em Mato Grosso, e praticante do Rito Brasileiro."],
[51,"Que é a Maçonaria?","p. 274","Uma Ordem discreta que admite homens de honra e bons costumes. Seu objetivo é tornar a humanidade mais feliz em âmbito universal, fundamentada em filosofia, filantropia, moral, ajuda mútua e busca da verdade."]
];
const Q = ROWS.map(r => ({n:r[0], q:r[1], ref:r[2], a:r[3], fa:r[4]||r[3], fq:r[5]||r[1]}));
Q[50].label = "Resposta pessoal";
const refFala = r => r === "—" ? "" : "Referência: " + r.replace(/^p\.\s*/, "página ").replace(/\s*[–-]\s*/g, " a ") + ".";
const falas = x => ({ q: `Pergunta ${x.n}. ${x.fq}`, a: `${x.label || "Resposta"}. ${x.fa}`, r: refFala(x.ref) });
g.QUESTOES = { Q, refFala, falas };
})(typeof window !== "undefined" ? window : globalThis);
