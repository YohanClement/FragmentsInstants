# Feature Specification: Les Fragments d'Instants — V1 (tâches, Pomodoro, journée, humeur/fatigue)

**Feature Branch**: `001-taches-pomodoro-humeur`

**Created**: 2026-09-28

**Status**: Draft

**Input**: User description: "Application personnelle « Les Fragments d'Instants » : to-do list, Pomodoro, suivi du temps réel, statistiques et suivi humeur/fatigue, ton doux et encourageant. Périmètre : V1 uniquement. User stories priorisées : gestion des tâches, Pomodoro, dashboard journalier, tags, suivi humeur/fatigue, visualisations, persistance locale, paramètres. Règles de calcul pour le poids des tâches, la progression du jour, le temps réel/restant, les moyennes hebdomadaires humeur/fatigue et les pourcentages par tag. Hors périmètre V1 : YouTube, ambiances, gamification, comparaisons entre semaines, bilan hebdomadaire détaillé."

## Clarifications

### Session 2026-09-28

- Q: Une session Pomodoro interrompue avant la fin doit-elle compter comme un Pomodoro dans le
  nombre affiché sur le dashboard, ou seulement les sessions menées à leur terme ? → A: Seules
  les sessions menées à leur terme comptent dans le "nombre de Pomodoros" et dans le compteur de
  Pomodoros avant pause longue ; le temps d'une session interrompue reste comptabilisé dans le
  temps de focus total.
- Q: Une tâche déjà marquée "terminée" peut-elle être rouverte (annuler la complétion) ? → A: Oui,
  la complétion est réversible ; la tâche revient au statut actif.
- Q: Comment le temps d'une tâche sans aucun tag doit-il apparaître dans le donut de répartition
  par tag ? → A: Aucune tâche n'est jamais sans tag. Le tag "Autre" est assigné par défaut à la
  création et n'importe quel autre tag choisi le remplace immédiatement (retrait automatique
  d'"Autre" dès qu'un autre tag est ajouté). Si tous les autres tags sont retirés, "Autre" est
  réassigné automatiquement. Le tag "Autre" est protégé : il ne peut être ni supprimé ni renommé.
  Si un tag personnalisé est supprimé, les tâches qui n'avaient que ce tag basculent sur "Autre".
  Le donut totalise ainsi toujours 100 % du temps de focus enregistré.
- Q: Le compteur de Pomodoros avant une pause longue se réinitialise-t-il chaque jour, ou reste-t-il
  continu tant que la séquence n'est pas interrompue par une pause longue ? → A: Réinitialisation
  quotidienne ; seuls les Pomodoros Focus complétés le jour même comptent pour déclencher la pause
  longue.
- Q: La moyenne hebdomadaire d'humeur/fatigue (calculée uniquement sur les jours renseignés) doit-
  elle être exposée dans l'interface V1, ou retirée faute d'écran dédié ? → A: Exposée sous forme
  de cartes hebdomadaires sur le tableau de bord (une carte par échelle : humeur, fatigue physique,
  fatigue mentale), affichant "aucune donnée" si aucun jour de la semaine en cours n'a été
  renseigné.
- Q: Comment définir précisément "tâche prévue aujourd'hui", utilisée à la fois par le filtre
  "Aujourd'hui" et par le dénominateur de la progression pondérée du jour ? → A: Une tâche est
  "prévue aujourd'hui" si elle est non terminée avec une échéance égale ou antérieure à
  aujourd'hui (une échéance passée reste dans la journée, sans marque d'alerte ni ton punitif), OU
  si elle a été terminée aujourd'hui, quelle que soit son échéance. Une tâche sans échéance n'est
  jamais "prévue aujourd'hui" automatiquement, sauf si elle est terminée aujourd'hui. La même
  définition sert au filtre "Aujourd'hui" (les tâches terminées y restant masquées par défaut,
  comme le prévoit FR-004) et au dénominateur de la progression pondérée (FR-028).
- Q: Comment le temps d'une session Pomodoro sans tâche associée (ou dont la tâche a été
  supprimée) doit-il être compté dans la répartition par tag ? → A: Comptabilisé intégralement
  sous le tag "Autre".
- Q: La répartition du temps par tag (donut) se calcule-t-elle sur le temps total cumulé d'une
  tâche, ou séance par séance ? → A: Séance par séance, sur la période choisie (aujourd'hui ou
  cette semaine) : chaque session Focus de la période est répartie également entre les tags
  courants de sa tâche associée au moment du calcul (ou affectée entièrement à "Autre" si elle n'a
  pas de tâche associée).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Gérer mes tâches (Priority: P1)

En tant qu'utilisatrice, je veux créer, modifier, terminer, mettre en pause et supprimer mes
tâches, avec un titre, une description facultative, des tags multiples, une durée estimée, une
priorité et une date prévue/échéance facultative, afin d'organiser mon travail du jour.

**Why this priority**: Sans la gestion des tâches, aucune autre fonctionnalité (Pomodoro,
dashboard, statistiques) n'a de contenu à afficher ; c'est le socle de données de toute
l'application.

**Independent Test**: Peut être testé seul en créant une tâche avec tous ses attributs, en la
filtrant via chaque vue (Toutes, Aujourd'hui, Importantes, Terminées), puis en la terminant, en la
mettant en pause et en la supprimant — sans dépendre du Pomodoro ni des statistiques.

**Acceptance Scenarios**:

1. **Given** aucune tâche n'existe, **When** l'utilisatrice crée une tâche avec un titre, une
   priorité "haute" et une durée estimée de 60 minutes, **Then** la tâche apparaît dans la liste
   "Toutes" avec sa durée estimée affichée, un temps passé de 0 et un temps restant de 60 minutes.
2. **Given** une tâche existante avec une échéance aujourd'hui, **When** l'utilisatrice applique le
   filtre "Aujourd'hui", **Then** la tâche apparaît dans la liste filtrée.
3. **Given** une tâche de priorité "haute" ou "critique", **When** l'utilisatrice applique le filtre
   "Importantes", **Then** la tâche apparaît dans la liste filtrée.
4. **Given** une tâche en cours, **When** l'utilisatrice la marque comme terminée, **Then** elle
   disparaît des filtres "Toutes"/"Aujourd'hui"/"Importantes" par défaut et apparaît dans le filtre
   "Terminées".
5. **Given** une tâche en cours, **When** l'utilisatrice la met en pause, **Then** son statut change
   sans qu'elle soit comptée comme terminée, et elle peut être reprise plus tard.
6. **Given** une tâche marquée terminée, **When** l'utilisatrice annule cette complétion, **Then**
   la tâche redevient active et réapparaît dans les filtres "Toutes"/"Aujourd'hui"/"Importantes"
   le cas échéant.
7. **Given** une tâche active dont l'échéance était hier, **When** l'utilisatrice applique le
   filtre "Aujourd'hui", **Then** la tâche apparaît dans la liste, sans aucune marque d'alerte
   visuelle indiquant un retard.

---

### User Story 2 - Faire un Pomodoro (Priority: P1)

En tant qu'utilisatrice, je veux démarrer un Pomodoro (Focus, pause courte ou pause longue), le
mettre en pause, le reprendre, le réinitialiser ou passer à l'étape suivante, en l'associant
éventuellement à une tâche, afin de structurer mes périodes de concentration.

**Why this priority**: C'est le second pilier de l'application : sans lui, il n'y a ni temps réel
enregistré, ni focus quotidien à afficher sur le dashboard ou dans les statistiques.

**Independent Test**: Peut être testé seul en démarrant une session Focus par défaut (25 min), en la
mettant en pause puis en la reprenant, en la réinitialisant, et en passant manuellement à l'étape
suivante — le temps réel enregistré peut être vérifié indépendamment de toute tâche associée.

**Acceptance Scenarios**:

1. **Given** aucun Pomodoro en cours, **When** l'utilisatrice démarre un Pomodoro Focus associé à
   une tâche, **Then** un minuteur de 25 minutes (valeur par défaut) démarre et la session est liée
   à cette tâche.
2. **Given** un Pomodoro Focus en cours, **When** l'utilisatrice le met en pause puis le reprend,
   **Then** le minuteur reprend là où il s'était arrêté.
3. **Given** un Pomodoro Focus en cours depuis 10 minutes, **When** l'utilisatrice le réinitialise
   ou ferme l'application avant la fin, **Then** les 10 minutes écoulées sont tout de même
   enregistrées comme temps réel de la session.
4. **Given** un Pomodoro Focus terminé, **When** l'utilisatrice passe à l'étape suivante, **Then**
   le mode Pause courte (5 min par défaut) démarre.
5. **Given** un Pomodoro Focus réinitialisé avant son terme, **When** l'utilisatrice consulte le
   dashboard, **Then** le temps écoulé est ajouté au temps de focus du jour mais le "nombre de
   Pomodoros" et le compteur avant pause longue ne sont pas incrémentés.
6. **Given** trois Pomodoros Focus complétés aujourd'hui, **When** minuit passe, **Then** le
   compteur de Pomodoros avant pause longue repart de zéro pour la nouvelle journée.

---

### User Story 3 - Voir ma journée (Priority: P1)

En tant qu'utilisatrice, je veux un tableau de bord affichant le Pomodoro en cours, les tâches du
jour, ma progression pondérée, mon temps de focus, mes tâches terminées et le nombre de Pomodoros
réalisés, afin d'avoir une vue d'ensemble de ma journée en un coup d'œil.

**Why this priority**: C'est la vue de synthèse qui donne du sens aux données saisies dans les
tâches et les Pomodoros ; elle est consultée en premier à chaque ouverture de l'application.

**Independent Test**: Peut être testé en créant des tâches et des sessions Pomodoro pour la
journée en cours, puis en vérifiant que le dashboard reflète correctement chaque agrégat
(progression, temps de focus, tâches terminées, nombre de Pomodoros) sans naviguer ailleurs.

**Acceptance Scenarios**:

1. **Given** deux tâches prévues aujourd'hui dont une terminée, **When** l'utilisatrice ouvre le
   dashboard, **Then** la progression pondérée du jour est calculée et affichée en pourcentage.
2. **Given** trois Pomodoros Focus complétés aujourd'hui, **When** l'utilisatrice ouvre le
   dashboard, **Then** le nombre de Pomodoros et le temps de focus cumulé du jour sont affichés.
3. **Given** un Pomodoro actuellement en cours, **When** l'utilisatrice ouvre le dashboard,
   **Then** l'état du Pomodoro en cours (mode, tâche associée, temps restant) y est visible.
4. **Given** deux Pomodoros Focus complétés aujourd'hui et un seuil de 4 avant pause longue,
   **When** l'utilisatrice ouvre le dashboard, **Then** le nombre de Pomodoros restants avant la
   pause longue (2) et le type de la prochaine pause (courte) sont affichés, calculés par le
   serveur.
5. **Given** des saisies humeur/fatigue sur au moins un jour de la semaine en cours, **When**
   l'utilisatrice ouvre le dashboard, **Then** des cartes hebdomadaires affichent la moyenne
   d'humeur, de fatigue physique et de fatigue mentale calculées uniquement sur les jours
   renseignés ; si aucun jour de la semaine n'est renseigné, chaque carte affiche "aucune donnée".

---

### User Story 4 - Organiser par tags (Priority: P1)

En tant qu'utilisatrice, je veux classer mes tâches avec des tags par défaut (Écriture,
Administratif, Maison, Enseignement, Créatif, DIY, Recherche, Autre) ou des tags personnalisés,
afin de regrouper mon activité par domaine.

**Why this priority**: Les tags sont la clé de répartition utilisée par les statistiques
(donut par tag) ; ils doivent exister avant que la visualisation ait du sens.

**Independent Test**: Peut être testé en assignant un ou plusieurs tags par défaut à une tâche,
puis en créant un tag personnalisé et en l'assignant également, sans dépendre des visualisations.

**Acceptance Scenarios**:

1. **Given** une nouvelle tâche, **When** l'utilisatrice l'enregistre sans choisir de tag,
   **Then** le tag "Autre" lui est automatiquement assigné.
2. **Given** une tâche portant uniquement le tag "Autre", **When** l'utilisatrice lui assigne un
   autre tag, **Then** "Autre" est retiré automatiquement.
3. **Given** une tâche existante, **When** l'utilisatrice lui assigne plusieurs tags (par défaut
   et/ou personnalisés), **Then** tous les tags choisis sont associés à la tâche.
4. **Given** une tâche dont le seul tag est retiré, **When** l'utilisatrice confirme le retrait,
   **Then** le tag "Autre" est réassigné automatiquement à la tâche.
5. **Given** aucun tag "Voyage" n'existe, **When** l'utilisatrice crée ce tag personnalisé,
   **Then** il devient disponible pour toute tâche future.
6. **Given** le tag "Autre", **When** l'utilisatrice tente de le supprimer ou de le renommer,
   **Then** l'action est refusée car ce tag est protégé.

---

### User Story 5 - Suivre humeur et fatigue (Priority: P1)

En tant qu'utilisatrice, je veux enregistrer une fois par jour trois notes indépendantes (humeur,
fatigue physique, fatigue mentale) de 1 à 7, avec une explication de ce que signifient les
extrêmes 1 et 7, afin de suivre mon bien-être dans le temps.

**Why this priority**: C'est la donnée de bien-être qui distingue cette application d'un simple
gestionnaire de tâches ; elle alimente directement les courbes de tendance.

**Independent Test**: Peut être testé en saisissant les trois notes du jour et en vérifiant
qu'une seconde saisie le même jour modifie l'entrée existante plutôt que d'en créer une
nouvelle — indépendamment des tâches ou du Pomodoro.

**Acceptance Scenarios**:

1. **Given** aucune saisie n'existe pour aujourd'hui, **When** l'utilisatrice note humeur=5,
   fatigue physique=3, fatigue mentale=4, **Then** l'entrée du jour est enregistrée avec ces trois
   valeurs.
2. **Given** une saisie déjà faite aujourd'hui, **When** l'utilisatrice consulte l'écran de
   saisie, **Then** elle voit une explication de ce que représentent les valeurs extrêmes (1 et 7)
   pour chacune des trois échelles.
3. **Given** une saisie déjà faite aujourd'hui, **When** l'utilisatrice modifie une des trois
   notes, **Then** l'entrée du jour est mise à jour sans créer de doublon.

---

### User Story 6 - Visualiser (Priority: P1)

En tant qu'utilisatrice, je veux visualiser la répartition de mon temps par tag (donut,
aujourd'hui/semaine), mon focus quotidien de la semaine (histogramme lundi→dimanche), l'évolution
de mon humeur et de ma fatigue (courbes à 3 séries masquables) et mes tâches terminées par jour
(barres), afin de comprendre mes tendances.

**Why this priority**: Les visualisations transforment les données brutes des autres user stories
en informations exploitables ; elles dépendent des tâches, tags, Pomodoros et saisies
humeur/fatigue déjà enregistrés.

**Independent Test**: Peut être testé en générant des données sur plusieurs jours (tâches, tags,
sessions, saisies humeur/fatigue) puis en vérifiant que chaque graphique reflète ces données
correctement, indépendamment de l'écran où elles ont été saisies.

**Acceptance Scenarios**:

1. **Given** du temps enregistré sur plusieurs tags aujourd'hui, **When** l'utilisatrice consulte
   le donut "aujourd'hui", **Then** chaque tag représente le pourcentage exact de son temps sur le
   temps total du jour.
2. **Given** des sessions Focus réparties sur la semaine, **When** l'utilisatrice consulte
   l'histogramme hebdomadaire, **Then** chaque jour (lundi→dimanche) affiche son temps de focus
   cumulé, y compris les jours à 0.
3. **Given** des saisies humeur/fatigue sur plusieurs jours, **When** l'utilisatrice masque la
   série "fatigue mentale", **Then** seules les courbes humeur et fatigue physique restent
   visibles.
4. **Given** des tâches terminées sur plusieurs jours, **When** l'utilisatrice consulte le
   graphique en barres correspondant, **Then** chaque jour affiche son nombre de tâches terminées.

---

### User Story 7 - Ne rien perdre (Priority: P1)

En tant qu'utilisatrice, je veux que mes tâches, sessions, saisies d'humeur et paramètres soient
conservés localement sur mon appareil, afin de ne jamais perdre mes données entre deux
utilisations.

**Why this priority**: Sans persistance fiable, toutes les autres fonctionnalités perdent leur
valeur dès la fermeture de l'application.

**Independent Test**: Peut être testé en créant des données dans chaque autre user story, en
fermant complètement l'application, puis en la rouvrant pour vérifier que rien n'a été perdu.

**Acceptance Scenarios**:

1. **Given** des tâches, sessions, saisies humeur/fatigue et paramètres personnalisés existants,
   **When** l'utilisatrice ferme puis rouvre l'application, **Then** toutes ces données sont
   intactes, sans connexion à un compte.

---

### User Story 8 - Paramètres (Priority: P1)

En tant qu'utilisatrice, je veux pouvoir configurer les durées des modes Pomodoro, le nombre de
Pomodoros avant une pause longue, les coefficients de priorité et les sons/notifications, afin
d'adapter l'application à mes habitudes.

**Why this priority**: Ces réglages influencent directement les calculs (poids des tâches,
progression) et le comportement du Pomodoro utilisés par toutes les autres user stories ; ils
doivent être disponibles dès la V1 pour que les valeurs par défaut restent ajustables.

**Independent Test**: Peut être testé en modifiant chaque paramètre puis en vérifiant que le
comportement du Pomodoro et les calculs de progression utilisent bien les nouvelles valeurs.

**Acceptance Scenarios**:

1. **Given** les durées par défaut (25/5/15), **When** l'utilisatrice modifie la durée du mode
   Focus à 30 minutes, **Then** le prochain Pomodoro Focus démarré utilise cette nouvelle durée.
2. **Given** le coefficient par défaut de la priorité "critique" (2), **When** l'utilisatrice le
   modifie à 2,5, **Then** le poids des tâches critiques et la progression du jour recalculée en
   tiennent compte.
3. **Given** les notifications sonores activées par défaut, **When** l'utilisatrice les désactive,
   **Then** aucun son n'est joué à la fin d'un Pomodoro.

---

### Edge Cases

- Que se passe-t-il si l'utilisatrice tente une seconde saisie d'humeur/fatigue le même jour ?
  L'entrée existante du jour est modifiée, aucune entrée en double n'est créée (voir US5).
- Comment le système calcule-t-il le poids d'une tâche sans durée estimée renseignée ? Cette
  tâche contribue avec un poids de 0 à la progression pondérée du jour (ni au numérateur, ni au
  dénominateur autrement que par sa valeur nulle).
- Que se passe-t-il quand aucune tâche n'est prévue un jour donné (dénominateur de la progression
  = 0) ? La progression du jour est affichée à 0 %, jamais comme une erreur ou une valeur
  indéfinie.
- Que se passe-t-il quand le temps total enregistré pour une période est nul lors du calcul de la
  répartition par tag (dénominateur = 0) ? Chaque tag est affiché à 0 %, jamais comme une erreur.
- Que se passe-t-il pour la moyenne hebdomadaire humeur/fatigue quand aucun jour de la semaine n'a
  été renseigné ? Elle est affichée comme "aucune donnée", jamais comme 0.
- Que se passe-t-il si une tâche associée à une session Pomodoro passée est supprimée ? Le temps
  déjà enregistré pour cette session reste comptabilisé dans les statistiques globales et de tag ;
  seule l'association à la tâche supprimée est perdue.
- Que se passe-t-il si un tag personnalisé utilisé par des tâches existantes est supprimé ? Les
  statistiques déjà calculées pour les périodes passées restent inchangées ; les tâches qui
  n'avaient que ce tag basculent automatiquement sur "Autre" (voir US4).
- Que se passe-t-il si l'utilisatrice réinitialise un Pomodoro quelques secondes après l'avoir
  démarré ? Le temps réellement écoulé est tout de même enregistré en base comme une session
  interrompue, quelle que soit sa durée ; toutefois, s'il est inférieur à une minute, il n'entre
  pas dans le temps de focus total affiché (FR-012, règle des sessions de moins d'une minute).
  Dans tous les cas, cette session interrompue n'incrémente ni le "nombre de Pomodoros" ni le
  compteur avant pause longue (voir US2).
- Que se passe-t-il au changement de jour calendaire (minuit) pendant qu'un Pomodoro est en
  cours ? Le compteur de Pomodoros avant pause longue se réinitialise pour la nouvelle journée ;
  la session en cours continue normalement et son temps est comptabilisé sur la journée où elle
  se termine.
- Que se passe-t-il si l'utilisatrice tente de supprimer ou de renommer le tag "Autre" ? L'action
  est refusée : ce tag est protégé car il sert de valeur par défaut garantissant qu'aucune tâche
  n'est jamais sans tag.
- Comment le temps d'une session Focus sans tâche associée (jamais assignée, ou associée à une
  tâche depuis supprimée) doit-il apparaître dans la répartition par tag ? Cette session est
  comptabilisée intégralement sous le tag "Autre", au même titre qu'une tâche sans tag
  personnalisé (voir FR-031).
- Que se passe-t-il pour une tâche active ou en pause dont l'échéance est déjà passée ? Elle reste
  incluse dans le filtre "Aujourd'hui" et dans la progression pondérée du jour tant qu'elle n'est
  pas terminée, sans aucune marque d'alerte ni ton culpabilisant (principe de Bienveillance) — voir
  la définition de "tâche prévue aujourd'hui" en FR-006.

## Requirements *(mandatory)*

### Functional Requirements

**Gestion des tâches**

- **FR-001**: Le système DOIT permettre de créer une tâche avec un titre (obligatoire), une
  description (facultative), un ou plusieurs tags, une durée estimée (facultative), une priorité
  (basse/normale/haute/critique) et une date prévue/échéance (facultative).
- **FR-002**: Le système DOIT permettre de modifier n'importe quel attribut d'une tâche existante.
- **FR-003**: Le système DOIT permettre de supprimer une tâche.
- **FR-004**: Le système DOIT permettre de marquer une tâche comme terminée.
- **FR-005**: Le système DOIT permettre de mettre une tâche en pause, un statut distinct de
  "terminée" qui peut être repris ultérieurement.
- **FR-032**: Le système DOIT permettre d'annuler la complétion d'une tâche déjà marquée
  terminée, en la remettant au statut actif.
- **FR-006**: Le système DOIT permettre de filtrer les tâches par : Toutes, Aujourd'hui,
  Importantes, Terminées. Une tâche est considérée **"prévue aujourd'hui"** si elle n'est pas
  terminée et que son échéance est égale ou antérieure à la date du jour (une échéance passée
  reste incluse dans "aujourd'hui", affichée sans marque d'alerte ni ton punitif — principe de
  Bienveillance), OU si elle a été terminée aujourd'hui, quelle que soit son échéance ; une tâche
  sans échéance n'est jamais "prévue aujourd'hui" de ce seul fait, sauf si elle est terminée
  aujourd'hui. Cette définition sert à la fois de base au filtre "Aujourd'hui" (les tâches
  terminées y restant masquées par défaut, comme le prévoit FR-004) et au dénominateur de la
  progression pondérée du jour (FR-028).
- **FR-007**: Le filtre "Importantes" DOIT inclure les tâches de priorité haute ou critique.
- **FR-008**: Pour chaque tâche, le système DOIT afficher séparément la durée estimée, le temps
  réel passé et le temps restant.

**Pomodoro**

- **FR-009**: Le système DOIT proposer trois modes Pomodoro — Focus (25 min par défaut), Pause
  courte (5 min par défaut), Pause longue (15 min par défaut) — dont les durées sont modifiables
  dans les paramètres.
- **FR-010**: Le système DOIT permettre de démarrer, mettre en pause, reprendre, réinitialiser un
  Pomodoro, et de passer manuellement à l'étape suivante.
- **FR-011**: Le système DOIT permettre d'associer une session Pomodoro à une tâche.
- **FR-012**: Le système DOIT enregistrer le temps réellement écoulé d'une session Pomodoro même
  si celle-ci est interrompue avant son terme (pause prolongée, réinitialisation, fermeture de
  l'application). Cette durée DOIT être calculée par le serveur à partir des horodatages de début
  et de fin transmis par le front (jamais à partir d'une valeur de durée envoyée directement par
  le front), afin de garantir une source unique de vérité.
- **FR-033**: Le système DOIT distinguer une session Pomodoro menée à son terme d'une session
  interrompue ; seules les sessions Focus menées à leur terme DOIVENT incrémenter le "nombre de
  Pomodoros" et le compteur de Pomodoros avant pause longue, même si le temps d'une session
  interrompue reste comptabilisé dans le temps de focus total.
- **FR-037**: Le système DOIT réinitialiser le compteur de Pomodoros avant pause longue à chaque
  nouveau jour calendaire, de sorte que seuls les Pomodoros Focus complétés le jour même comptent
  pour déclencher la pause longue.
- **FR-038**: Le système DOIT calculer côté serveur, à partir du nombre de Pomodoros Focus
  terminés aujourd'hui et du seuil configuré (FR-025), le nombre de Pomodoros restants avant la
  prochaine pause longue ainsi que le type (courte ou longue) de la prochaine pause, et DOIT les
  exposer sur le tableau de bord (FR-013), afin qu'aucun de ces deux calculs ne soit dupliqué côté
  front.
- **FR-039**: Le système DOIT calculer et exposer sur le tableau de bord des moyennes
  hebdomadaires d'humeur, de fatigue physique et de fatigue mentale portant sur les jours
  renseignés de la semaine en cours (au sens de FR-030), affichées sous forme de cartes, avec
  "aucune donnée" lorsque aucun jour de la semaine n'a été renseigné.
- **FR-040**: Le système DOIT rejeter toute action contraire aux règles métier (renommage ou
  suppression du tag protégé, note humeur/fatigue hors de l'intervalle 1-7, etc.) au moyen d'un
  code d'erreur métier unique et cohérent sur l'ensemble de l'API, accompagné d'un message
  explicite et bienveillant (principe de Bienveillance), plutôt que d'un code différent par règle.

**Tableau de bord**

- **FR-013**: Le système DOIT fournir un tableau de bord journalier affichant : le Pomodoro en
  cours (le cas échéant), les tâches du jour, la progression pondérée du jour, le temps de focus
  cumulé du jour, le nombre de tâches terminées aujourd'hui, le nombre de Pomodoros Focus menés
  à leur terme aujourd'hui (les sessions interrompues ne sont pas comptées dans ce nombre, voir
  FR-033), le nombre de Pomodoros restants avant la prochaine pause longue et le type de la
  prochaine pause (FR-038), ainsi que les moyennes hebdomadaires d'humeur et de fatigue sur les
  jours renseignés de la semaine en cours (FR-039).

**Tags**

- **FR-014**: Le système DOIT fournir les tags par défaut suivants : Écriture, Administratif,
  Maison, Enseignement, Créatif, DIY, Recherche, Autre.
- **FR-015**: Le système DOIT permettre de créer des tags personnalisés en plus des tags par
  défaut.
- **FR-016**: Le système DOIT permettre d'assigner plusieurs tags à une même tâche.
- **FR-035**: Le système DOIT assigner automatiquement le tag "Autre" à toute tâche créée sans
  tag choisi, et DOIT le réassigner automatiquement dès que le dernier autre tag d'une tâche est
  retiré ; à l'inverse, le tag "Autre" DOIT être retiré automatiquement dès qu'un autre tag est
  ajouté à la tâche. Une tâche a ainsi toujours au moins un tag.
- **FR-036**: Le tag "Autre" est protégé : le système DOIT interdire sa suppression et son
  renommage. Lorsqu'un tag personnalisé est supprimé, toute tâche qui n'avait que ce tag DOIT
  basculer automatiquement sur "Autre".

**Humeur et fatigue**

- **FR-017**: Le système DOIT limiter la saisie humeur/fatigue à une entrée par jour calendaire,
  composée de trois notes indépendantes (humeur, fatigue physique, fatigue mentale) sur une
  échelle de 1 à 7.
- **FR-018**: Le système DOIT afficher, pour chacune des trois échelles, une explication de ce que
  représentent les valeurs extrêmes 1 et 7.
- **FR-019**: Le système DOIT permettre de modifier l'entrée du jour déjà enregistrée, le même
  jour calendaire, sans créer de doublon.

**Visualisations**

- **FR-020**: Le système DOIT afficher un graphique en donut de la répartition du temps par tag,
  pour les périodes "aujourd'hui" et "cette semaine".
- **FR-021**: Le système DOIT afficher un histogramme du temps de focus quotidien, du lundi au
  dimanche, pour la semaine en cours.
- **FR-022**: Le système DOIT afficher des courbes d'évolution pour l'humeur, la fatigue physique
  et la fatigue mentale, sous forme de trois séries indépendamment masquables.
- **FR-023**: Le système DOIT afficher un graphique en barres du nombre de tâches terminées par
  jour.

**Persistance**

- **FR-024**: Le système DOIT conserver localement, sur l'appareil, toutes les tâches, sessions
  Pomodoro, saisies humeur/fatigue et paramètres, et ces données DOIVENT persister après
  fermeture de l'application, sans compte utilisateur requis.

**Paramètres**

- **FR-025**: Le système DOIT permettre de configurer : la durée de chaque mode Pomodoro, le
  nombre de Pomodoros Focus avant le déclenchement d'une pause longue, les coefficients de
  priorité (basse/normale/haute/critique) et les préférences de son/notifications.
- **FR-026**: Le système DOIT appliquer par défaut les coefficients de priorité suivants tant que
  l'utilisatrice ne les modifie pas dans les paramètres : basse = 1, normale = 1,25, haute = 1,5,
  critique = 2.

**Règles de calcul**

- **FR-027**: Le système DOIT calculer le poids d'une tâche comme sa durée estimée multipliée par
  le coefficient de sa priorité.
- **FR-028**: Le système DOIT calculer la progression pondérée du jour comme (somme des poids des
  tâches terminées aujourd'hui ÷ somme des poids des tâches prévues aujourd'hui) × 100, et DOIT
  afficher 0 % — sans erreur — lorsque la somme des poids prévus est nulle.
- **FR-029**: Le système DOIT calculer le temps réel d'une tâche comme la somme des durées de
  toutes ses sessions Pomodoro enregistrées, et son temps restant comme max(0, durée estimée −
  temps réel).
- **FR-030**: Le système DOIT calculer les moyennes hebdomadaires d'humeur et de fatigue en
  n'utilisant que les jours ayant une entrée enregistrée, à l'exclusion des jours sans saisie.
- **FR-031**: Le système DOIT calculer, pour chaque période demandée (aujourd'hui ou cette
  semaine), le temps alloué à un tag en agrégeant **séance par séance** : chaque session Focus de
  la période est répartie également entre les tags courants de sa tâche associée, ou affectée
  intégralement au tag "Autre" si elle n'a pas de tâche associée (jamais assignée, ou tâche
  depuis supprimée) — jamais à partir du temps total cumulé d'une tâche. Le pourcentage par tag
  DOIT ensuite être calculé comme (temps alloué à ce tag ÷ temps total alloué sur la période) ×
  100, et DOIT afficher 0 % — sans erreur — lorsque le temps total alloué est nul.

### Key Entities

- **Tâche**: titre, description facultative, tags multiples (toujours au moins un, "Autre" par
  défaut), durée estimée facultative, priorité (basse/normale/haute/critique), date
  prévue/échéance facultative, statut (active, en pause, terminée — la complétion est réversible),
  temps réel et temps restant (dérivés des sessions Pomodoro associées).
- **Session Pomodoro**: mode (Focus, Pause courte, Pause longue), tâche associée (facultative pour
  les pauses ; une session Focus sans tâche associée compte sous le tag "Autre" dans la
  répartition par tag), horodatages de début et de fin, durée réellement écoulée (calculée par le
  serveur à partir de ces horodatages), indicateur d'achèvement (terminée normalement ou
  interrompue — seules les sessions Focus terminées normalement comptent dans le "nombre de
  Pomodoros" et le compteur avant pause longue).
- **Tag**: nom, origine (par défaut ou personnalisé), indicateur de protection ("Autre" est le
  seul tag protégé : ni supprimable, ni renommable).
- **Saisie humeur/fatigue**: date (une par jour), note d'humeur (1–7), note de fatigue physique
  (1–7), note de fatigue mentale (1–7).
- **Paramètres**: durées des trois modes Pomodoro, nombre de Pomodoros Focus avant pause longue,
  coefficients de priorité, préférences de son/notifications.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Une utilisatrice peut créer une nouvelle tâche complète (titre, priorité, durée
  estimée) en moins de 30 secondes.
- **SC-002**: La progression pondérée du jour est toujours affichée sur le tableau de bord sous
  forme d'un pourcentage valide, y compris les jours sans aucune tâche prévue (0 %, jamais une
  erreur).
- **SC-003**: Pour 100 % des tâches affichées, la durée estimée, le temps réel et le temps restant
  sont visibles simultanément et cohérents entre eux (temps restant = max(0, estimé − réel)).
- **SC-004**: Une utilisatrice peut compléter sa saisie humeur/fatigue du jour (3 notes) en moins
  de 20 secondes.
- **SC-005**: Les moyennes et courbes hebdomadaires d'humeur/fatigue n'intègrent jamais un jour
  sans saisie comme une valeur de 0.
- **SC-006**: Après fermeture puis réouverture complète de l'application, 100 % des tâches,
  sessions, saisies humeur/fatigue et paramètres précédemment enregistrés sont toujours présents,
  sans étape de connexion.
- **SC-007**: Depuis le seul tableau de bord, une utilisatrice peut connaître le nombre de
  Pomodoros complétés, le temps de focus cumulé du jour, et le nombre de Pomodoros restants avant
  la prochaine pause longue (calculé côté serveur), sans naviguer vers un autre écran ni effectuer
  de calcul elle-même.
- **SC-008**: La somme des pourcentages du donut de répartition par tag, pour une période donnée,
  totalise 100 % (à l'arrondi près) dès qu'au moins une minute de temps a été enregistrée sur
  cette période.

## Assumptions

- Application mono-utilisatrice, mono-appareil pour la V1 : aucune synchronisation entre
  appareils ni compte utilisateur n'est requise (conforme au principe Local-first).
- Valeur par défaut du nombre de Pomodoros Focus avant une pause longue : 4, modifiable dans les
  paramètres.
- Le contenu exact des textes d'explication des valeurs extrêmes (1 et 7) des échelles
  humeur/fatigue est un détail de contenu/rédaction, laissé à la conception de l'interface, tant
  qu'il respecte le principe de Bienveillance (ton encourageant, non culpabilisant).
- Aucune limite de rétention n'est imposée sur l'historique des tâches, sessions et saisies
  humeur/fatigue en V1 ; toutes les données passées restent consultables.
- Les notifications sonores/visuelles sont locales à l'application (pas de service de notification
  push distant requis).
- Hors périmètre V1 (explicitement exclu par la demande) : intégration YouTube, ambiances
  sonores, mécaniques de gamification, comparaisons entre semaines, et bilan hebdomadaire détaillé
  — ces éléments pourront être considérés pour une V2/V3.
- Toute notion de "jour calendaire" (rattachement d'une session à son jour, réinitialisation
  quotidienne du compteur de Pomodoros, moyennes hebdomadaires) s'entend dans le fuseau horaire
  fixe du poste de l'utilisatrice ; voir [plan.md](./plan.md) pour la valeur retenue.
