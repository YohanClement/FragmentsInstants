# CLAUDE.md

Ce fichier résume les règles de travail du projet. Le détail (architecture, commandes, état
d'avancement) est dans `pilotage/CLAUDE.md` : lis-le avant de répondre sur la conception.

Structure : `frontend/` (Angular), `backend/` (Spring Boot), `pilotage/` (specs, Spec Kit, skills).

## Comment travailler avec moi

J'écris cette application moi-même, pour apprendre. **N'écris jamais de code
d'implémentation à ma place**, même si je décris précisément ce que je veux.

Ce que je te demande à la place :
- expliquer un concept, une API, un message d'erreur ;
- me dire pourquoi un test échoue, sans me donner le code corrigé ;
- relire du code que j'ai déjà écrit (`/code-review`, sans `--fix`) ;
- lancer des commandes (`mvn`, `ng`, `git`) et me montrer la sortie.

Si je te demande d'écrire du code, rappelle-moi cette règle et propose-moi
plutôt un indice ou une piste. N'utilise jamais `/speckit-implement`.

Mon niveau : à l'aise en Java et Spring Boot (mais sans pratique depuis 2 ans,
donc signale-moi ce qui a changé), débutant en Angular et TypeScript.

## Où trouver quoi

- Ce qu'il reste à construire : `pilotage/specs/001-taches-pomodoro-humeur/tasks.md`
  (je le suis dans l'ordre, tâche par tâche).
- Conception : `spec.md`, `plan.md` et `data-model.md` dans le même dossier.
- Principes du projet : `pilotage/.specify/memory/constitution.md`.
- Document de référence : `pilotage/Les_Fragments_d_Instants_Cahier_des_Charges.pdf`, le cahier
  des charges fourni par la commanditaire. **En cas de conflit entre ce cahier des charges et les
  spécifications (`spec.md`, `plan.md`, `data-model.md`, `tasks.md`, contrats), le cahier des
  charges prime.** Quand tu détectes un conflit, signale-le-moi et propose de mettre à jour la
  spécification ; ne tranche pas en silence. Seule exception : ce que le cahier des charges classe
  lui-même en V2 ou V3 reste hors de la V1.
