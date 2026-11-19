'use strict';

let currentUser = null;
let csrfToken = null;
let comparisonId = null;
let hasSavedFeedback = false;

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
  if (!currentUser) {
    error.textContent = 'Connectez-vous pour utiliser les IA.';
    error.hidden = false;
    return;
  }
  if (!prompt || providers.length === 0) {
    error.textContent = 'Saisissez une question et choisissez au moins une IA.';
    error.hidden = false;
    return;
  }
  section.hidden = true;
  document.querySelector('#feedback').hidden = true;
  comparisonId = null;
  results.replaceChildren();
  controls.disabled = true;
  document.querySelector('#logout').disabled = true;
  form.setAttribute('aria-busy', 'true');
  status.textContent = 'Les IA préparent leurs réponses. Cela peut prendre un moment…';
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 120000);
  try {
    const compare = providers.length > 1;
    const response = await apiFetch(compare ? '/ai/compare' : '/ai/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
      body: JSON.stringify(compare ? { prompt, providers, taskType: document.querySelector('#task-type').value } : { prompt, provider: providers[0] }),
      signal: controller.signal
    });
    if (response.status === 401) throw new Error('Votre session a expiré. Reconnectez-vous.');
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
    if (compare) showFeedback(data.comparisonId, answers);
    status.textContent = `${answers.length} réponse(s) reçue(s).`;
  } catch (exception) {
    error.textContent = exception.name === 'AbortError'
      ? 'Le délai d’attente est dépassé. Le serveur peut encore traiter la demande. Réessayez dans un moment.'
      : exception instanceof TypeError ? 'Le serveur est injoignable. Vérifiez votre connexion puis réessayez.' : exception.message;
    error.hidden = false;
    status.textContent = 'La demande n’a pas abouti.';
  } finally {
    clearTimeout(timeout);
    controls.disabled = !currentUser;
    document.querySelector('#logout').disabled = false;
    form.removeAttribute('aria-busy');
  }
});

async function refreshCsrf() {
  const response = await fetch('/auth/csrf', { credentials: 'same-origin' });
  if (!response.ok) throw new Error('Impossible de préparer la connexion. Réessayez.');
  csrfToken = await response.json();
}

async function apiFetch(url, options = {}) {
  const headers = new Headers(options.headers);
  if (options.method && options.method !== 'GET') {
    if (!csrfToken) await refreshCsrf();
    headers.set(csrfToken.headerName, csrfToken.token);
  }
  const response = await fetch(url, { ...options, headers, credentials: 'same-origin' });
  if (response.status === 401 && url !== '/auth/login') setSession(null);
  return response;
}

function setSession(user) {
  currentUser = user;
  controls.disabled = !user;
  document.querySelector('#account-form').hidden = !!user;
  document.querySelector('#logout').hidden = !user;
  document.querySelector('#account-status').textContent = user
    ? `Connecté : ${user.email}` : 'Connectez-vous ou créez un compte pour lancer une comparaison.';
  if (!user) {
    section.hidden = true;
    results.replaceChildren();
    comparisonId = null;
    document.querySelector('#feedback').hidden = true;
  }
}

const accountForm = document.querySelector('#account-form');
async function authenticate(register) {
  const accountError = document.querySelector('#account-error');
  accountError.hidden = true;
  if (!accountForm.reportValidity()) return;
  const email = document.querySelector('#email').value.trim();
  const password = document.querySelector('#password').value;
  if (register && (password.length < 12 || password.length > 128)) {
    accountError.textContent = 'Le mot de passe doit contenir entre 12 et 128 caractères.';
    accountError.hidden = false;
    return;
  }
  const buttons = [...accountForm.querySelectorAll('button')];
  buttons.forEach(button => button.disabled = true);
  try {
    await refreshCsrf();
    if (register) {
      const response = await apiFetch('/auth/register', {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ email, password })
      });
      if (!response.ok) throw new Error(response.status === 409
        ? 'Cette adresse est déjà utilisée.' : 'Inscription impossible. Vérifiez les champs et réessayez.');
    }
    const response = await apiFetch('/auth/login', {
      method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ username: email, password })
    });
    if (!response.ok) throw new Error(register
      ? 'Compte créé. La connexion a échoué : réessayez de vous connecter.'
      : 'Connexion impossible. Vérifiez votre adresse et votre mot de passe.');
    await refreshCsrf();
    const profile = await apiFetch('/auth/me');
    if (!profile.ok) throw new Error('Impossible de récupérer le compte. Réessayez.');
    setSession(await profile.json());
    document.querySelector('#password').value = '';
  } catch (exception) {
    accountError.textContent = exception instanceof TypeError ? 'Le serveur est injoignable.' : exception.message;
    accountError.hidden = false;
  } finally {
    buttons.forEach(button => button.disabled = false);
  }
}
accountForm.addEventListener('submit', event => { event.preventDefault(); authenticate(false); });
document.querySelector('#register').addEventListener('click', () => authenticate(true));
document.querySelector('#logout').addEventListener('click', async event => {
  event.target.disabled = true;
  try {
    const response = await apiFetch('/auth/logout', { method: 'POST' });
    if (!response.ok) throw new Error('Déconnexion impossible. Rechargez la page et réessayez.');
    setSession(null);
    csrfToken = null;
    await refreshCsrf();
  } catch (exception) {
    const accountError = document.querySelector('#account-error');
    accountError.textContent = exception.message;
    accountError.hidden = false;
  } finally { event.target.disabled = false; }
});

function showFeedback(id, answers) {
  comparisonId = id;
  hasSavedFeedback = false;
  const options = document.querySelector('#preference-options');
  options.replaceChildren();
  document.querySelector('#feedback-status').textContent = '';
  document.querySelector('#usefulness').hidden = true;
  document.querySelectorAll('[data-useful]').forEach(button => button.setAttribute('aria-pressed', 'false'));
  const choices = answers.map(answer => [answer.provider, `${answer.provider} — ${answer.model}`]);
  choices.push(['EQUIVALENT', 'Équivalentes'], ['NONE', 'Aucune ne convient']);
  for (const [value, label] of choices) {
    const button = document.createElement('button');
    button.type = 'button';
    button.textContent = label;
    button.dataset.preference = value;
    button.setAttribute('aria-pressed', 'false');
    button.addEventListener('click', () => saveFeedback({ preference: value }));
    options.append(button);
  }
  document.querySelector('#feedback').hidden = false;
}

async function saveFeedback(feedback) {
  const id = comparisonId;
  if (!id || !currentUser) return;
  const buttons = [...document.querySelectorAll('#feedback button')];
  buttons.forEach(button => button.disabled = true);
  document.querySelector('#feedback-status').textContent = 'Enregistrement de votre avis…';
  try {
    const response = await apiFetch(`/comparisons/${id}/feedback`, {
      method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(feedback)
    });
    if (!response.ok) throw new Error(response.status === 401
      ? 'Votre session a expiré. Reconnectez-vous.' : 'Avis non enregistré. Réessayez.');
    const saved = await response.json();
    if (id !== comparisonId) return;
    document.querySelectorAll('[data-preference]').forEach(button =>
      button.setAttribute('aria-pressed', String(button.dataset.preference === saved.preference)));
    document.querySelectorAll('[data-useful]').forEach(button =>
      button.setAttribute('aria-pressed', String(saved.useful !== null && String(saved.useful) === button.dataset.useful)));
    document.querySelector('#usefulness').hidden = false;
    document.querySelector('#feedback-status').textContent = hasSavedFeedback
      ? 'Avis mis à jour. Vos nouveaux choix remplacent les précédents.'
      : 'Avis enregistré. Vous pouvez modifier vos choix à tout moment.';
    hasSavedFeedback = true;
  } catch (exception) {
    if (id === comparisonId) document.querySelector('#feedback-status').textContent = exception.message;
  } finally { buttons.forEach(button => button.disabled = false); }
}
document.querySelectorAll('[data-useful]').forEach(button => button.addEventListener('click', () =>
  saveFeedback({ useful: button.dataset.useful === 'true' })));

(async () => {
  try {
    await refreshCsrf();
    const response = await apiFetch('/auth/me');
    if (response.status !== 401 && !response.ok) throw new Error('Impossible de vérifier la connexion.');
    setSession(response.ok ? await response.json() : null);
  } catch (exception) {
    setSession(null);
    document.querySelector('#account-error').textContent = 'Le serveur est injoignable. Rechargez la page pour réessayer.';
    document.querySelector('#account-error').hidden = false;
  }
})();
