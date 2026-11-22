'use strict';
const api = '/api/pontos-turisticos';
const form = document.querySelector('#formulario');
const campos = ['nome', 'descricao', 'endereco', 'latitude', 'longitude'];
let editando = null;
let ocupado = false;

function mensagem(texto, erro = false) {
  const elemento = document.querySelector('#mensagem');
  elemento.textContent = texto;
  elemento.classList.toggle('erro', erro);
}

async function requisicao(url, options = {}) {
  const resposta = await fetch(url, options);
  if (!resposta.ok) {
    const erro = await resposta.json().catch(() => ({}));
    const detalhes = Object.values(erro.campos || {}).join('; ');
    throw new Error([erro.mensagem || `Erro HTTP ${resposta.status}`, detalhes].filter(Boolean).join(': '));
  }
  return resposta.status === 204 ? null : resposta.json();
}

function bloquear(valor) {
  ocupado = valor;
  document.querySelectorAll('button').forEach(b => b.disabled = valor);
  form.querySelectorAll('input, textarea').forEach(e => e.disabled = valor);
}

async function executar(acao) {
  if (ocupado) return;
  bloquear(true);
  try { await acao(); }
  catch (erro) { mensagem(erro.message === 'Failed to fetch' ? 'Não foi possível conectar à API. Verifique se o servidor está rodando.' : erro.message, true); }
  finally { bloquear(false); }
}

function limpar() {
  editando = null;
  form.reset();
  document.querySelector('#titulo-form').textContent = 'Cadastrar ponto';
  document.querySelector('#salvar').textContent = 'Cadastrar';
  document.querySelector('#cancelar').hidden = true;
}

function botao(texto, acao, classe = 'secundario') {
  const b = document.createElement('button');
  b.type = 'button'; b.textContent = texto; b.className = classe;
  b.addEventListener('click', () => executar(acao));
  return b;
}

async function listar() {
  const pontos = await requisicao(api);
  const lista = document.querySelector('#lista');
  lista.replaceChildren();
  document.querySelector('#contagem').textContent = pontos.length ? `${pontos.length} ponto(s) cadastrado(s)` : 'Nenhum ponto cadastrado. Use o formulário para começar.';
  for (const ponto of pontos) {
    const artigo = document.createElement('article'); artigo.className = 'ponto';
    const titulo = document.createElement('h3'); titulo.textContent = ponto.nome;
    const id = document.createElement('small'); id.textContent = `ID ${ponto.id}`;
    const endereco = document.createElement('p'); endereco.textContent = ponto.endereco;
    const acoes = document.createElement('div'); acoes.className = 'acoes';
    acoes.append(botao('Consultar', async () => {
      const atual = await requisicao(`${api}/${ponto.id}`);
      const dados = document.querySelector('#dados-detalhe'); dados.replaceChildren();
      const rotulos = {id:'ID', nome:'Nome', descricao:'Descrição', endereco:'Endereço', latitude:'Latitude', longitude:'Longitude'};
      for (const [chave, rotulo] of Object.entries(rotulos)) {
        const dt = document.createElement('dt'); dt.textContent = rotulo;
        const dd = document.createElement('dd'); dd.textContent = atual[chave]; dados.append(dt, dd);
      }
      document.querySelector('#detalhes').hidden = false;
      document.querySelector('#detalhes').scrollIntoView({behavior:'smooth'});
    }), botao('Editar', async () => {
      const atual = await requisicao(`${api}/${ponto.id}`);
      editando = atual.id;
      campos.forEach(c => form.elements[c].value = atual[c]);
      document.querySelector('#titulo-form').textContent = `Editar ponto #${atual.id}`;
      document.querySelector('#salvar').textContent = 'Salvar alterações';
      document.querySelector('#cancelar').hidden = false;
      form.scrollIntoView({behavior:'smooth'});
    }), botao('Excluir', async () => {
      if (!window.confirm(`Excluir o ponto “${ponto.nome}”? Esta operação é definitiva.`)) return;
      await requisicao(`${api}/${ponto.id}`, {method:'DELETE'});
      if (editando === ponto.id) limpar();
      document.querySelector('#detalhes').hidden = true;
      mensagem('Ponto excluído com sucesso.'); await listar();
    }, 'excluir'));
    artigo.append(id, titulo, endereco, acoes); lista.append(artigo);
  }
}

form.addEventListener('submit', evento => {
  evento.preventDefault();
  if (!form.reportValidity()) return;
  // Capture antes de desabilitar os campos.
  const dados = Object.fromEntries(campos.map(c => [c, c === 'latitude' || c === 'longitude' ? Number(form.elements[c].value) : form.elements[c].value]));
  executar(async () => {
    const alteracao = editando !== null;
    await requisicao(alteracao ? `${api}/${editando}` : api, {method:alteracao ? 'PUT' : 'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify(dados)});
    limpar(); document.querySelector('#detalhes').hidden = true;
    mensagem(alteracao ? 'Ponto atualizado com sucesso.' : 'Ponto cadastrado com sucesso.'); await listar();
  });
});
document.querySelector('#cancelar').addEventListener('click', limpar);
document.querySelector('#recarregar').addEventListener('click', () => executar(listar));
document.querySelector('#fechar').addEventListener('click', () => document.querySelector('#detalhes').hidden = true);
executar(listar);
