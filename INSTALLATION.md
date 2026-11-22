# Fragments d'Instants — Installer et lancer le projet

Pour lancer le projet, il faut installer 3 logiciels (Git, Java 21, Node.js 24), puis lancer deux
programmes en même temps, chacun dans son propre terminal.

- **Le backend** (Spring Boot, en Java) : le serveur, sur `http://localhost:8080`. Il crée tout seul
  sa base de données SQLite au premier lancement.
- **Le frontend** (Angular) : l'interface, sur `http://localhost:4200`. C'est cette adresse qu'on
  ouvre dans le navigateur.

À savoir : l'application est en cours de construction. Pour l'instant on voit la barre de
navigation et 7 pages encore vides ; les écrans arrivent au fil des prochaines étapes.

## 1. Installer les logiciels

Ces trois logiciels suffisent. Pas besoin d'installer Maven ni Angular à part : le projet les
fournit (`mvnw` pour Maven, `npx ng` pour Angular).

| Logiciel | Version | Où le trouver | À quoi il sert |
| --- | --- | --- | --- |
| Git | n'importe quelle version récente | [git-scm.com](https://git-scm.com/downloads) | récupérer le projet |
| Java (JDK) | **21** (pas 17, pas 25) | [adoptium.net](https://adoptium.net/temurin/releases/?version=21) (Temurin 21, LTS) | faire tourner le backend |
| Node.js | **24 LTS** (au moins 24.15) ; 22.22.3 ou plus marche aussi | [nodejs.org](https://nodejs.org/) | faire tourner le frontend ; npm est installé avec |

Sous Windows, pendant l'installation de Temurin, coche l'option **« Set JAVA_HOME variable »**
(elle est décochée par défaut).

Une fois tout installé, **ferme et rouvre ton terminal**, puis vérifie :

```
git --version
java -version
node -v
npm -v
```

`java -version` doit afficher `21.x` et `node -v` doit afficher `v24.x` (ou `v22.22.3` ou plus).
Si une commande est « introuvable », le logiciel n'est pas installé ou le terminal n'a pas été rouvert.

## 2. Récupérer le projet

Le code à jour est sur la branche `dev`. Il vaut mieux cloner avec Git plutôt que télécharger le
ZIP : on peut ensuite récupérer les nouveautés avec une seule commande.

```
git clone -b dev https://github.com/YohanClement/FragmentsInstants.git
cd FragmentsInstants
```

Pour récupérer plus tard les dernières modifications, depuis le dossier `FragmentsInstants` :

```
git pull
```

Le dossier contient `backend/`, `frontend/` et `pilotage/` (la documentation du projet).

## 3. Lancer le backend (terminal n°1)

Ouvre un premier terminal dans le dossier `FragmentsInstants`. Le premier lancement prend quelques
minutes, le temps de télécharger Maven et les dépendances.

**Windows** (PowerShell) :

```
cd backend
.\mvnw.cmd spring-boot:run
```

**Mac ou Linux** :

```
cd backend
chmod +x mvnw
./mvnw spring-boot:run
```

Le `chmod +x mvnw` ne sert qu'une fois : il rend le script exécutable.

C'est prêt quand la dernière ligne contient **`Started FragmentsInstantsApplication`**. Laisse ce
terminal ouvert : fermer le terminal arrête le serveur (ou `Ctrl+C`).

Si tu ouvres `http://localhost:8080`, une page d'erreur « Whitelabel Error Page » (404) est normale
pour l'instant : le serveur tourne, mais aucune page n'est encore servie à cette adresse.

Facultatif, pour vérifier que tout fonctionne : `.\mvnw.cmd test` (Windows) ou `./mvnw test`
(Mac/Linux) lance les tests automatiques ; tout doit finir par `BUILD SUCCESS`.

## 4. Lancer le frontend (terminal n°2)

Ouvre un **deuxième** terminal (le premier fait tourner le backend), toujours dans le dossier
`FragmentsInstants`. Les commandes sont les mêmes sous Windows, Mac et Linux.

La première fois seulement, installe les dépendances (1 à 3 minutes) :

```
cd frontend
npm install
```

Puis, à chaque fois qu'on veut lancer l'application :

```
npx ng serve
```

C'est prêt quand le terminal affiche `Local: http://localhost:4200/`. Ouvre cette adresse dans le
navigateur. Comme pour le backend, laisse le terminal ouvert, et `Ctrl+C` pour arrêter.

Après un `git pull`, relance `npm install` avant `npx ng serve` : de nouvelles dépendances ont pu
être ajoutées.

Facultatif : `npx ng test --watch=false` lance les tests du frontend.

## 5. Problèmes fréquents

| Message ou symptôme | Cause probable | Solution |
| --- | --- | --- |
| `java`, `node` ou `git` « n'est pas reconnu » / « command not found » | logiciel absent, ou terminal ouvert avant l'installation | installer le logiciel, puis fermer et rouvrir le terminal |
| `JAVA_HOME is not set` ou `JAVA_HOME ... invalid` | Java installé sans la variable `JAVA_HOME` | réinstaller Temurin 21 en cochant « Set JAVA_HOME variable », puis rouvrir le terminal |
| `release version 21 not supported` ou erreur de version Java | une autre version de Java (17, par exemple) est utilisée | vérifier `java -version` ; installer Java 21 et le mettre en premier |
| Windows : `l'exécution de scripts est désactivée sur ce système` en lançant `npm` ou `npx` | politique de sécurité de PowerShell | une fois : `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned`, ou utiliser l'Invite de commandes (cmd) |
| Mac/Linux : `permission denied: ./mvnw` | le script n'est pas exécutable | `chmod +x mvnw`, ou lancer `sh mvnw spring-boot:run` |
| `The engine "node" is incompatible` ou `Node.js version ... is not supported` | Node.js trop ancien | installer Node.js 24 LTS |
| `Port 8080 was already in use` / `Port 4200 is already in use` | l'application tourne déjà dans un autre terminal | fermer l'autre terminal (ou `Ctrl+C` dedans), puis relancer |
| `ng serve` échoue sur `esbuild`, `lmdb` ou `@parcel/watcher` | npm a bloqué leurs scripts d'installation | dans `frontend/`, lancer `npm install-scripts approve`, puis `npm install` |
| « FlywayValidateException » ou erreur de checksum au démarrage du backend | base de données créée par une ancienne version du projet | arrêter le backend, supprimer `backend/data/fragments-instants.db`, relancer |

Si rien ne marche, envoie la dernière vingtaine de lignes du terminal (avec le message d'erreur) à Yohan.
