# Interface web Compairator

## Démarrer

Depuis la racine du dépôt (le dossier contenant `pom.xml`), avec Java 21 ou supérieur :

```powershell
.\mvnw.cmd spring-boot:run
```

Ouvrir http://localhost:8080/web (ou `/`). L'interface est intégrée à la même application Spring Boot : aucun serveur frontend ni installation npm n'est nécessaire.

Les prérequis du backend restent nécessaires : MySQL accessible selon `src/main/resources/application.properties` et variables d'environnement `DB_USERNAME`, `DB_PASSWORD`, `GROQ_API_KEY`, `MISTRAL_API_KEY`. Le résolveur actuel exige les configurations des deux fournisseurs, même pour une demande à un seul fournisseur. Ne pas mettre de clés dans les ressources publiques.

## Organisation

- `main/java/com/will/compairator/web/WebController.java` : point d'entrée MVC Java, redirection interne vers la page.
- `main/resources/static/compairator-ui/` : HTML, styles et JavaScript dédiés à l'interface.
- Le package `ai` garde la responsabilité des appels IA, du cache et de la persistance.

Il s'agit d'une fonctionnalité séparée par package et par répertoire, dans l'application Maven existante, sans nouveau module Maven ni changement des API.

## Utilisation

Saisir une question, choisir Groq et/ou Mistral puis envoyer. Une sélection utilise `POST /ai/chat` avec `{ "prompt": "…", "provider": "GROQ" }`. Deux sélections utilisent `POST /ai/compare` avec `{ "prompt": "…", "providers": ["GROQ", "MISTRAL"] }`.

Les réponses et modèles sont affichés côte à côte. Le texte renvoyé est affiché comme du texte, sans interpréter du HTML. Le formulaire évite les doubles envois et permet de réessayer après une erreur. L'attente côté navigateur est limitée à deux minutes ; son expiration n'annule pas les appels en cours côté serveur.

Les appels sont réels et peuvent consommer les quotas des fournisseurs. Il n'y a pas de réponses simulées, d'authentification, de conversation suivie ou d'historique affiché.

## Vérification manuelle

1. Ouvrir `/web` et vérifier l'affichage sur ordinateur et mobile.
2. Utiliser l'exemple avec les deux IA : deux réponses avec leur modèle doivent apparaître.
3. Décocher une IA et envoyer : une réponse doit apparaître.
4. Décocher les deux IA ou saisir uniquement des espaces : une erreur doit apparaître sans appel réseau.
5. Vérifier les états d'attente et d'erreur en cas d'indisponibilité du backend.
