# Interface web Compairator

## Démarrer

Depuis la racine du dépôt (le dossier contenant `pom.xml`), avec Java 21 ou supérieur :

La commande ci-dessous nécessite que `DB_USERNAME`, `DB_PASSWORD`, `GROQ_API_KEY` et `MISTRAL_API_KEY` soient définies dans le terminal courant. Les variables d'une configuration Run IntelliJ ne sont transmises qu'aux applications lancées avec cette configuration, pas automatiquement au terminal.

Pour ce projet local, si les variables sont déjà enregistrées dans la configuration IntelliJ `CompairatorApplication`, les charger sans afficher leurs valeurs :

```powershell
[xml]$ideaConfig = Get-Content -LiteralPath '.idea/workspace.xml' -Raw
$runConfig = $ideaConfig.SelectSingleNode('//configuration[@name="CompairatorApplication"]')
if ($null -eq $runConfig) { throw 'Configuration IntelliJ CompairatorApplication introuvable.' }
foreach ($entry in $runConfig.SelectNodes('.//env')) {
    if ($entry.name -in @('DB_USERNAME', 'DB_PASSWORD', 'GROQ_API_KEY', 'MISTRAL_API_KEY')) {
        [Environment]::SetEnvironmentVariable($entry.name, $entry.value, 'Process')
    }
}
```

Ces variables sont chargées uniquement pour la session PowerShell courante et ses processus enfants. Ce bloc dépend de la configuration IntelliJ locale et n'est pas requis si les variables sont déjà fournies par un autre moyen. On peut également démarrer directement `CompairatorApplication` avec le bouton Run d'IntelliJ.

Puis lancer :

```powershell
.\mvnw.cmd spring-boot:run
```

Ouvrir http://localhost:8080/web (ou `/`). L'interface est intégrée à la même application Spring Boot : aucun serveur frontend ni installation npm n'est nécessaire.

Les prérequis du backend restent nécessaires : MySQL accessible selon `src/main/resources/application.properties` et variables d'environnement `DB_USERNAME`, `DB_PASSWORD`, `GROQ_API_KEY`, `MISTRAL_API_KEY`. Le résolveur actuel exige les configurations des deux fournisseurs, même pour une demande à un seul fournisseur. Ne pas mettre de clés dans les ressources publiques.

Si MySQL affiche `Access denied for user '${DB_USERNAME}'@'localhost'`, le nom de variable a été envoyé littéralement : `DB_USERNAME` n'a pas été résolue dans ce processus. Vérifier le chargement des variables avant de modifier un mot de passe ou la configuration Hibernate.

## Organisation

- `main/java/com/will/compairator/web/WebController.java` : point d'entrée MVC Java, redirection interne vers la page.
- `main/resources/static/compairator-ui/` : HTML, styles et JavaScript dédiés à l'interface.
- Le package `ai` garde la responsabilité des appels IA, du cache et de la persistance.

L'interface utilise l'application Maven existante, sans nouveau module ni dépendance npm. Les comptes sont dans le package `user`, le suivi des comparaisons dans `comparison` et les statistiques dans `stats`.

## Utilisation

Créer un compte avec une adresse e-mail et un mot de passe de 12 à 128 caractères, ou se connecter à un compte existant. La connexion est obligatoire pour appeler les IA. Le mot de passe est haché avec PBKDF2 et la connexion utilise une session HTTP avec protection CSRF.

Saisir une question, choisir le type de tâche puis Groq et/ou Mistral et envoyer. Une sélection utilise `POST /ai/chat` avec `{ "prompt": "…", "provider": "GROQ" }`. Deux sélections utilisent `POST /ai/compare` avec `{ "prompt": "…", "providers": ["GROQ", "MISTRAL"], "taskType": "GENERAL" }`. La réponse de comparaison contient aussi son `comparisonId`.

Les réponses et modèles sont affichés côte à côte. Le texte renvoyé est affiché comme du texte, sans interpréter du HTML. Le formulaire évite les doubles envois et permet de réessayer après une erreur. L'attente côté navigateur est limitée à deux minutes ; son expiration n'annule pas les appels en cours côté serveur.

Après une comparaison réussie, l'utilisateur peut choisir une réponse préférée, les juger équivalentes ou indiquer qu'aucune ne convient. Une question facultative sur l'utilité apparaît ensuite. Les avis sont modifiables et n'empêchent jamais de continuer.

Les appels sont réels et peuvent consommer les quotas des fournisseurs. Il n'y a pas de conversation suivie ni d'historique affiché. Les KPI sont disponibles dans une API administrateur, sans tableau de bord graphique : voir [KPI.md](KPI.md).

## Vérification manuelle

1. Ouvrir `/web` et vérifier l'affichage sur ordinateur et mobile.
2. Vérifier que l'envoi est désactivé sans connexion, puis créer un compte et se reconnecter après une déconnexion.
3. Utiliser l'exemple avec les deux IA : deux réponses avec leur modèle doivent apparaître.
4. Choisir une préférence, répondre à la question sur l'utilité puis modifier l'avis. Il doit rester un seul avis par comparaison.
5. Décocher une IA et envoyer : une réponse doit apparaître, sans évaluation de comparaison.
6. Décocher les deux IA ou saisir uniquement des espaces : une erreur doit apparaître sans appel réseau.
7. Vérifier les états d'attente et d'erreur en cas d'indisponibilité du backend, puis la déconnexion et le masquage des résultats.

## Tests automatisés

Depuis la racine du dépôt : `./mvnw.cmd test`. Les tests utilisent H2 en mémoire et des réponses IA simulées : MySQL et les clés API réelles ne sont pas nécessaires. Les paramètres de test se trouvent uniquement dans `src/test/resources/application.properties`.

En usage local, MySQL doit être démarré et les quatre variables d'environnement indiquées plus haut doivent être disponibles dans le processus qui lance Spring Boot. Les nouvelles tables sont créées par la configuration Hibernate `ddl-auto=update` existante. Les anciens journaux sont conservés.
