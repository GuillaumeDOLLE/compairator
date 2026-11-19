# Comptes et indicateurs de performance

## Accès

L'inscription crée toujours un compte `USER`. Aucun paramètre envoyé par le navigateur ne permet de devenir administrateur. Après avoir créé le compte souhaité, un administrateur de la base peut lui attribuer le rôle avec cette commande MySQL, en remplaçant l'adresse :

```sql
UPDATE app_user SET role = 'ADMIN' WHERE email = 'administrateur@example.com';
```

Se déconnecter puis se reconnecter après ce changement. L'API `GET /stats` est réservée au rôle `ADMIN`. Un utilisateur ordinaire reçoit 403 et un visiteur non connecté reçoit 401.

Pour consulter les statistiques, utiliser le lien « Statistiques administrateur » affiché après connexion avec un compte administrateur, ou ouvrir `/admin/stats`. La page présente les indicateurs, un filtre de dates inclusives, les données quotidiennes, les préférences par tâche et la consommation par utilisateur. La page et son accès HTML direct sont protégés côté serveur.

Pour accéder au JSON, ouvrir `/stats` ou appeler `/stats?from=2026-11-01&to=2026-11-19` avec la même session. L'API ne renvoie ni mots de passe, ni questions, ni réponses IA. Le formulaire convertit la date de fin inclusive vers la borne exclusive attendue par cette API.

## Périodes et définitions

- Les dates sont en UTC. `from` est inclus et `to` est exclu. La période par défaut couvre les 30 dates UTC se terminant aujourd'hui ; les observations s'arrêtent à l'heure de la requête pour la journée en cours.
- La durée demandée est limitée à 366 jours. Une période inversée ou une date future est refusée.
- Une comparaison désigne un appel à `/ai/compare`, avec les deux fournisseurs distincts actuels. Ses deux appels IA ne sont pas deux comparaisons.
- Une tentative valide enregistrée rend l'utilisateur actif, même si elle échoue. Une visite, une connexion et un appel à une seule IA ne constituent pas une comparaison.
- Une comparaison réussit lorsque les deux réponses exploitables ont été obtenues et enregistrées. Deux réponses identiques peuvent constituer une comparaison réussie.
- Les états commencés mais non terminés sont comptés séparément. Un arrêt brutal du serveur peut laisser une tentative dans cet état ; elle n'est pas automatiquement reclassée en échec.

## Indicateurs

| Champ | Définition |
|---|---|
| `registeredUsers` | Comptes créés avant la fin d'observation. |
| `newUsers` | Comptes créés pendant la période. |
| `activationWithin7Days` | Parmi les inscrits de la période ayant au moins 7 jours de recul, part ayant terminé une première comparaison réussie avant inscription + 7 jours. |
| `retentionDays7To14` | Parmi les inscrits de la période ayant au moins 14 jours de recul, part ayant lancé une comparaison entre inscription + 7 jours inclus et + 14 jours exclus. |
| `activeUsers` | Utilisateurs distincts ayant lancé une comparaison sur la période. |
| `dailyActiveUsers` | Utilisateurs distincts ayant lancé une comparaison durant le dernier jour UTC observé. |
| `monthlyActiveUsers` | Utilisateurs distincts ayant lancé une comparaison pendant les 30 jours glissants précédant la fin d'observation, indépendamment de `from`. |
| `comparisonsStarted/Successful/Failed/Pending` | Tentatives lancées sur la période et répartition de leur état à la fin d'observation. |
| `technicalSuccessRate` | Réussites / (réussites + échecs). Les tentatives non terminées sont exclues du dénominateur. |
| `successfulComparisonsPerActiveUser` | Comparaisons réussies / utilisateurs actifs de la période. |
| `medianSuccessfulDurationMs` | Médiane des durées complètes des comparaisons réussies, appels successifs et enregistrement compris. |
| `usefulness` | Avis « utile » / avis d'utilité reçus. Une absence d'avis est exclue. |
| `usefulnessParticipation` | Comparaisons ayant un avis d'utilité / comparaisons réussies. |
| `preferenceParticipation` | Comparaisons ayant une préférence / comparaisons réussies. « Équivalentes » et « Aucune » sont des avis. |
| `cost` | Coûts estimés et couverture des données, décrits ci-dessous. |
| `daily` | Inscriptions, utilisateurs actifs et comparaisons par jour, avec les jours sans activité. |
| `usageByUser` | Comparaisons, tokens connus, coûts connus et nombre d'appels inconnus par identifiant utilisateur. |
| `modelPreferences` | Victoires et apparitions évaluées par modèle réellement renvoyé et type de tâche. |

Les taux sont des fractions entre 0 et 1, pas des pourcentages. Un dénominateur nul donne `null`. Les cohortes trop récentes sont exclues des taux d'activation et de rétention, plutôt que considérées en échec.

Les préférences désignent le fournisseur choisi parmi les réponses affichées. Elles sont attribuées au modèle enregistré lors de l'appel, même si la configuration change ensuite. Les avis « Équivalentes » et « Aucune » augmentent le nombre d'apparitions évaluées mais n'attribuent aucune victoire. Les résultats reflètent les avis volontaires, pas une mesure objective de qualité.

Chaque comparaison conserve un avis courant modifiable. Pour une période passée, seuls les avis dont la dernière modification précède la fin d'observation sont retenus : cette version ne conserve pas l'historique des révisions d'avis et ne reconstruit pas les anciens votes remplacés.

## Tokens et coût estimé

Les fournisseurs renvoient les champs `usage.prompt_tokens` et `usage.completion_tokens`. Ils sont enregistrés par appel ; une information absente ou négative reste inconnue. Un résultat servi depuis le cache consomme zéro nouveau token fournisseur et a un coût additionnel nul.

Les tarifs sont configurables en USD par million de tokens dans `application.properties` ou via la configuration Spring correspondante :

```properties
ai.pricing.groq.model=identifiant-exact-renvoye-par-le-fournisseur
ai.pricing.groq.input-per-million=tarif-entree
ai.pricing.groq.output-per-million=tarif-sortie
```

Utiliser le préfixe `ai.pricing.mistral` pour Mistral. Remplacer les valeurs ci-dessus par des valeurs numériques vérifiées auprès du fournisseur. Aucun tarif n'est inventé ou activé par défaut. Si les tokens, le modèle exact ou un tarif manquent, le coût est `null` et le nombre d'appels au coût inconnu augmente.

Le coût calculé est `(tokens entrée × tarif entrée + tokens sortie × tarif sortie) / 1 000 000`. Le montant est enregistré lors de l'appel : un changement de tarif ne recalcule pas les appels passés. Il s'agit d'une estimation, sans prise en compte des crédits, remises, taxes ou éventuelles règles de facturation spécifiques au fournisseur.

`knownComparisonCost` additionne les coûts connus des appels rattachés aux comparaisons lancées pendant la période et reçus avant la fin d'observation, y compris les appels d'une comparaison échouée. `averagePerSuccessfulComparison` divise cette somme par les comparaisons réussies ; il reste `null` si un coût est inconnu ou s'il n'y a aucune réussite. Les tokens globaux et l'usage par utilisateur portent sur les appels enregistrés pendant la période et incluent les appels à une seule IA.

## API des comptes et avis

1. `GET /auth/csrf` fournit `headerName` et `token`. Envoyer ce jeton dans l'en-tête indiqué pour les requêtes qui modifient les données, y compris inscription, connexion et déconnexion.
2. `POST /auth/register` reçoit un JSON avec `email` et `password`. L'adresse est normalisée en minuscules ; le mot de passe doit contenir entre 12 et 128 caractères.
3. `POST /auth/login` reçoit `username` et `password` au format `application/x-www-form-urlencoded`. La session est renouvelée à la connexion. Récupérer un nouveau jeton CSRF après connexion ou déconnexion.
4. `GET /auth/me` donne le profil courant. `POST /auth/logout` invalide la session.
5. `PUT /comparisons/{comparisonId}/feedback` reçoit `preference` (`GROQ`, `MISTRAL`, `EQUIVALENT`, `NONE`) et/ou `useful` (`true` ou `false`). Seul le propriétaire peut évaluer sa comparaison réussie. Une mise à jour partielle conserve l'autre réponse ; une mise à jour simultanée conflictuelle reçoit 409.

Le formulaire web gère automatiquement session et CSRF. Les cookies de session sont HttpOnly et SameSite=Lax. En déploiement HTTPS, définir `SESSION_COOKIE_SECURE=true`.

## Données existantes et vérification

Les journaux existants sont conservés. Ils n'ont pas rétroactivement d'utilisateur, de comparaison ou de tokens : les KPI de comparaison et d'utilisateur commencent avec les nouvelles données. Les anciens journaux peuvent apparaître comme appels aux tokens et coûts inconnus s'ils sont inclus dans la période demandée.

`./mvnw.cmd test` exécute les tests sur H2 en mémoire, sans MySQL et sans appel IA réel. Ils couvrent les accès, la protection CSRF, les comptes, les comparaisons, les échecs partiels, les avis, le cache, les cohortes et les coûts inconnus. Le profil ci-dessous permet de vérifier les mêmes parcours sur MySQL.

Pour exécuter la même suite sur MySQL, démarrer le service et fournir `MYSQL_TEST_USERNAME` et `MYSQL_TEST_PASSWORD` (ou `DB_USERNAME` et `DB_PASSWORD`) au processus Maven, puis lancer depuis la racine :

```powershell
.\mvnw.cmd test '-Dspring.profiles.active=mysql-test'
```

Ce profil crée si nécessaire la base locale `compairator_kpi_test`. Le compte MySQL doit pouvoir créer cette base et ses tables. Les tables de test sont recréées puis supprimées par Hibernate ; ne pas y stocker de données à conserver. Un garde-fou exécuté avant Hibernate refuse les autres bases MySQL, notamment `compairator`. Les réponses IA restent simulées et les tarifs de test sont fictifs.
