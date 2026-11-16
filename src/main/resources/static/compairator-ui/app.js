'use strict';

const form = document.querySelector('#compare-form');
const controls = document.querySelector('#controls');
const promptInput = document.querySelector('#prompt');
const submit = document.querySelector('#submit');
const status = document.querySelector('#status');
const error = document.querySelector('#error');
const section = document.querySelector('#results-section');
const results = document.querySelector('#results');
const selectedProviders = () => [...form.querySelectorAll('input[name="provider"]:checked')].map(input => input.value);

form.addEventListener('change', () => {
  submit.textContent = selectedProviders().length === 1 ? 'Obtenir une réponse →' : 'Comparer les réponses →';
});
document.querySelector('#example').addEventListener('click', () => {
  promptInput.value = 'Explique simplement les avantages et les limites de la voiture électrique pour une famille.';
  promptInput.focus();
});

form.addEventListener('submit', async event => {
  event.preventDefault();
  const providers = selectedProviders();
  const prompt = promptInput.value.trim();
  error.hidden = true;
  if (!prompt || providers.length === 0) {
    error.textContent = 'Saisissez une question et choisissez au moins une IA.';
    error.hidden = false;
    return;
  }
  section.hidden = true;
  results.replaceChildren();
  controls.disabled = true;
  form.setAttribute('aria-busy', 'true');
  status.textContent = 'Les IA préparent leurs réponses. Cela peut prendre un moment…';
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 120000);
  try {
    const compare = providers.length > 1;
    const response = await fetch(compare ? '/ai/compare' : '/ai/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
      body: JSON.stringify(compare ? { prompt, providers } : { prompt, provider: providers[0] }),
      signal: controller.signal
    });
    if (!response.ok) {
      throw new Error(response.status === 400
        ? 'La demande est invalide. Vérifiez votre question et les IA sélectionnées.'
        : 'Impossible d’obtenir les réponses. Vérifiez la connexion du serveur aux fournisseurs IA et à la base de données, puis réessayez.');
    }
    const data = await response.json();
    const answers = compare ? data.output : [{ provider: providers[0], model: data.model, completion: data.content }];
    if (!Array.isArray(answers) || answers.length !== providers.length || answers.some(answer => typeof answer.completion !== 'string')) {
      throw new Error('Le serveur a renvoyé une réponse inexploitable. Réessayez.');
    }
    for (const answer of answers) {
      const card = document.createElement('article');
      card.className = 'result';
      const title = document.createElement('h3');
      title.textContent = answer.provider;
      const model = document.createElement('p');
      model.className = 'model';
      model.textContent = answer.model;
      const content = document.createElement('p');
      content.className = 'completion';
      content.textContent = answer.completion;
      card.append(title, model, content);
      results.append(card);
    }
    document.querySelector('#question').textContent = prompt;
    section.hidden = false;
    status.textContent = `${answers.length} réponse(s) reçue(s).`;
  } catch (exception) {
    error.textContent = exception.name === 'AbortError'
      ? 'Le délai d’attente est dépassé. Le serveur peut encore traiter la demande. Réessayez dans un moment.'
      : exception instanceof TypeError ? 'Le serveur est injoignable. Vérifiez votre connexion puis réessayez.' : exception.message;
    error.hidden = false;
    status.textContent = 'La demande n’a pas abouti.';
  } finally {
    clearTimeout(timeout);
    controls.disabled = false;
    form.removeAttribute('aria-busy');
  }
});
