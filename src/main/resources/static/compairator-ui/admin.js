'use strict';

const periodForm = document.querySelector('#period-form');
const fromInput = document.querySelector('#from');
const toInput = document.querySelector('#to');
const statsContent = document.querySelector('#stats-content');
const statsStatus = document.querySelector('#stats-status');
const statsError = document.querySelector('#stats-error');
const integer = new Intl.NumberFormat('fr-FR');
const decimal = new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 2 });
const money = new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'USD', maximumFractionDigits: 6 });
const percent = value => value == null ? 'Non disponible' : `${decimal.format(value * 100)} %`;
const count = value => integer.format(value);
const amount = value => value == null ? 'Non disponible' : money.format(value);
const sample = rate => `${count(rate.numerator)} sur ${count(rate.denominator)}`;
const dateValue = date => date.toISOString().slice(0, 10);
const addDays = (date, days) => { const result = new Date(`${date}T00:00:00Z`); result.setUTCDate(result.getUTCDate() + days); return dateValue(result); };
const today = dateValue(new Date());
fromInput.value = addDays(today, -29);
toInput.value = today;
fromInput.max = today;
toInput.max = today;

function cards(target, values) {
  const container = document.querySelector(target);
  container.replaceChildren();
  for (const [title, value, detail] of values) {
    const card = document.createElement('article'); card.className = 'kpi-card';
    const heading = document.createElement('h3'); heading.textContent = title;
    const number = document.createElement('p'); number.className = 'kpi-value'; number.textContent = value;
    const explanation = document.createElement('p'); explanation.className = 'kpi-detail'; explanation.textContent = detail;
    card.append(heading, number, explanation); container.append(card);
  }
}

function table(target, captionText, headers, rows) {
  const container = document.querySelector(target); container.replaceChildren();
  if (!rows.length) { const empty = document.createElement('p'); empty.textContent = 'Aucune donnée pour cette période.'; container.append(empty); return; }
  const element = document.createElement('table');
  const caption = document.createElement('caption'); caption.textContent = captionText; element.append(caption);
  const head = document.createElement('thead'); const headerRow = document.createElement('tr');
  for (const title of headers) { const cell = document.createElement('th'); cell.scope = 'col'; cell.textContent = title; headerRow.append(cell); }
  head.append(headerRow); element.append(head);
  const body = document.createElement('tbody');
  for (const values of rows) { const row = document.createElement('tr'); for (const value of values) { const cell = document.createElement('td'); cell.textContent = value; row.append(cell); } body.append(row); }
  element.append(body); container.append(element);
}

function render(data) {
  cards('#kpi-cards', [
    ['Utilisateurs inscrits', count(data.registeredUsers), `${count(data.newUsers)} nouvelles inscriptions sur la période.`],
    ['Activation sous 7 jours', percent(data.activationWithin7Days.rate), `${sample(data.activationWithin7Days)} comptes ayant au moins 7 jours de recul.`],
    ['Utilisateurs actifs', count(data.activeUsers), `${count(data.dailyActiveUsers)} au dernier jour observé · ${count(data.monthlyActiveUsers)} sur les 30 jours glissants.`],
    ['Rétention entre J7 et J14', percent(data.retentionDays7To14.rate), `${sample(data.retentionDays7To14)} comptes ayant au moins 14 jours de recul.`],
    ['Comparaisons par utilisateur actif', data.successfulComparisonsPerActiveUser == null ? 'Non disponible' : decimal.format(data.successfulComparisonsPerActiveUser), 'Comparaisons réussies divisées par les utilisateurs ayant lancé une comparaison.'],
    ['Coût par comparaison réussie', amount(data.cost.averagePerSuccessfulComparison), `${count(data.cost.comparisonCallsWithUnknownCost)} appels de comparaison au coût inconnu. Estimation incluant les appels échoués.`],
    ['Comparaisons jugées utiles', percent(data.usefulness.rate), `${sample(data.usefulness)} avis d’utilité reçus. Participation : ${percent(data.usefulnessParticipation.rate)}.`]
  ]);
  cards('#service-cards', [
    ['Comparaisons lancées', count(data.comparisonsStarted), `${count(data.comparisonsSuccessful)} réussies · ${count(data.comparisonsFailed)} échouées · ${count(data.comparisonsPending)} non terminées.`],
    ['Réussite technique', percent(data.technicalSuccessRate), 'Part des comparaisons réussies parmi celles terminées.'],
    ['Durée médiane', data.medianSuccessfulDurationMs == null ? 'Non disponible' : `${decimal.format(data.medianSuccessfulDurationMs / 1000)} s`, 'Durée complète des comparaisons réussies.'],
    ['Participation aux préférences', percent(data.preferenceParticipation.rate), `${sample(data.preferenceParticipation)} comparaisons réussies ont reçu une préférence.`]
  ]);
  cards('#cost-cards', [
    ['Tokens d’entrée connus', count(data.cost.knownPromptTokens), 'Appels enregistrés sur la période, y compris les demandes à une seule IA.'],
    ['Tokens de sortie connus', count(data.cost.knownCompletionTokens), `${count(data.cost.callsWithUnknownTokens)} appels ont une consommation inconnue.`],
    ['Sous-total des coûts connus', amount(data.cost.knownComparisonCost), `${count(data.cost.comparisonCallsWithUnknownCost)} appels au coût inconnu : ce sous-total peut être incomplet.`]
  ]);
  table('#daily-table', 'Activité par journée UTC', ['Date', 'Inscriptions', 'Utilisateurs actifs', 'Comparaisons', 'Réussies'],
    data.daily.map(day => [day.date, count(day.newUsers), count(day.activeUsers), count(day.comparisons), count(day.successfulComparisons)]));
  const tasks = { GENERAL: 'Général', WRITING: 'Rédaction', CODING: 'Code', SUMMARY: 'Résumé', ANALYSIS: 'Analyse', OTHER: 'Autre' };
  table('#preferences-table', 'Modèles préférés selon la tâche', ['Tâche', 'Modèle', 'Victoires', 'Apparitions évaluées', 'Taux de préférence'],
    data.modelPreferences.map(model => [tasks[model.taskType] || model.taskType, model.model, count(model.wins), count(model.evaluatedAppearances), percent(model.preferenceRate)]));
  table('#users-table', 'Consommation par utilisateur', ['Utilisateur', 'Comparaisons', 'Tokens entrée', 'Tokens sortie', 'Appels tokens inconnus', 'Coût connu', 'Appels coût inconnu'],
    data.usageByUser.map(user => [count(user.userId), count(user.comparisons), count(user.knownPromptTokens), count(user.knownCompletionTokens), count(user.callsWithUnknownTokens), amount(user.knownCostUsd), count(user.callsWithUnknownCost)]));
}

async function loadStats(event) {
  event?.preventDefault();
  statsError.hidden = true;
  if (!periodForm.reportValidity()) return;
  const days = (Date.parse(`${toInput.value}T00:00:00Z`) - Date.parse(`${fromInput.value}T00:00:00Z`)) / 86400000 + 1;
  if (days < 1 || days > 366 || toInput.value > today) {
    statsError.textContent = 'Choisissez une période de 1 à 366 jours, sans date future.'; statsError.hidden = false; return;
  }
  const button = periodForm.querySelector('button'); button.disabled = true;
  statsContent.hidden = true; statsContent.setAttribute('aria-busy', 'true');
  statsStatus.textContent = 'Chargement des statistiques…';
  try {
    const query = new URLSearchParams({ from: fromInput.value, to: addDays(toInput.value, 1) });
    const response = await fetch(`/stats?${query}`, { credentials: 'same-origin', headers: { Accept: 'application/json' } });
    if (!response.ok) throw new Error(response.status === 401 ? 'Votre session a expiré. Retournez aux comparaisons pour vous reconnecter.'
      : response.status === 403 ? 'Cette page est réservée aux administrateurs.'
      : response.status === 400 ? 'La période demandée est invalide.' : 'Les statistiques sont indisponibles. Réessayez.');
    const data = await response.json(); render(data); statsContent.hidden = false;
    statsStatus.textContent = `Données du ${fromInput.value} au ${toInput.value} inclus, en UTC. Actualisées à ${new Date().toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })}.`;
  } catch (error) {
    statsError.textContent = error instanceof TypeError ? 'Le serveur est injoignable. Vérifiez la connexion et réessayez.' : error.message;
    statsError.hidden = false; statsStatus.textContent = 'Les statistiques n’ont pas pu être chargées.';
  } finally { button.disabled = false; statsContent.removeAttribute('aria-busy'); }
}
periodForm.addEventListener('submit', loadStats);
loadStats();
