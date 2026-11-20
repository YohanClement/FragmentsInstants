# Les Fragments d'Instants Constitution

## Core Principles

### I. Bienveillance
Aucune logique culpabilisante ou punitive n'est autorisée dans l'application : la notion de
"série perdue" (streak brisé) est INTERDITE, le rouge alarmant NE DOIT PAS être utilisé dans
l'interface, et tout message adressé à l'utilisateur DOIT être formulé de façon encourageante,
jamais culpabilisante.
Rationale : l'application accompagne le suivi d'humeur et de fatigue de l'utilisateur ; une
expérience punitive irait à l'encontre du bien-être qu'elle vise à soutenir.

### II. Local-first
Toutes les données utilisateur DOIVENT être stockées sur l'appareil et DOIVENT persister après
fermeture de l'application. Aucun compte utilisateur ni connexion à un service distant n'est
requis pour utiliser l'application.
Rationale : garantit la confidentialité des données personnelles (humeur, fatigue) et
l'autonomie complète de l'utilisateur vis-à-vis de tout service tiers.

### III. Données brutes d'abord
Chaque session datée et chaque saisie d'humeur DOIT être conservée telle quelle, sans perte
d'information. Les statistiques (moyennes, tendances, progressions) DOIVENT toujours être
recalculées à la volée à partir des données brutes ; elles NE DOIVENT JAMAIS être stockées comme
des totaux persistants.
Rationale : évite toute dérive entre les données stockées et les statistiques affichées, et
permet de faire évoluer les formules de calcul sans migration de données.

### IV. Exactitude des calculs
Les formules définies dans le cahier des charges DOIVENT être implémentées sous forme de
fonctions pures, séparées de l'interface utilisateur, et DOIVENT être couvertes par des tests
unitaires.
Rationale : garantit la fiabilité des statistiques affichées et permet de vérifier
indépendamment la logique métier sans dépendre du rendu de l'interface.

### V. Gestion des données manquantes
Un jour non renseigné NE DOIT JAMAIS être compté comme une valeur nulle (zéro) dans le calcul
des moyennes d'humeur ou de fatigue. Toute division par zéro dans les calculs de variation ou de
progression DOIT être explicitement gérée pour éviter erreurs et résultats aberrants.
Rationale : un jour vide reflète une absence de donnée, pas une mesure ; confondre les deux
fausserait les statistiques affichées à l'utilisateur.

### VI. Direction artistique
L'application adopte un mode sombre à l'esthétique botanique et féerique. La palette de couleurs
imposée, avec les noms et rôles fixés par le cahier des charges, est :
- Héliotrope #DF73FF : accent principal ;
- Doré ancien #B08D57 : détails, bordures et éléments importants ;
- Prune #4B2142 : panneaux et fonds secondaires ;
- Vert forêt #234236 : bien-être, nature et couleur secondaire ;
- Ivoire #F2E8D5 : texte clair et éléments lumineux.

Le fond principal de l'interface DOIT être presque noir ou prune très foncé ; la valeur retenue
est Nuit #1A053B. Tous les textes DOIVENT respecter un niveau de contraste AA pour rester
lisibles, et aucune couleur rouge alarmante NE DOIT être ajoutée à la palette (principe I).
Rationale : assure une identité visuelle cohérente, fidèle au cahier des charges de la
commanditaire, et un niveau d'accessibilité minimal garanti.

### VII. Simplicité
Le développement DOIT prioriser la livraison complète de la V1 avant toute considération de
fonctionnalité V2 ou V3.
Rationale : évite la dispersion de l'effort et garantit qu'un produit fonctionnel et cohérent
est livré avant d'étendre la portée du projet.

## Governance

Les documents du projet sont hiérarchisés ainsi, du plus prioritaire au moins prioritaire :
1. le cahier des charges de la commanditaire
   (`pilotage/Les_Fragments_d_Instants_Cahier_des_Charges.pdf`) ;
2. cette constitution ;
3. les spécifications (`spec.md`, `plan.md`, `data-model.md`, contrats, `tasks.md`).

En cas de conflit, le document le plus prioritaire l'emporte. Tout conflit détecté DOIT être
signalé, et le document le moins prioritaire DOIT être mis à jour pour s'aligner ; un conflit NE
DOIT JAMAIS être tranché en silence. Exception : ce que le cahier des charges classe lui-même en
V2 ou V3 reste hors de la V1 (principe VII).

Cette constitution prévaut sur toute autre pratique ou convention du projet qui n'est pas listée
ci-dessus. Toute décision de conception ou d'implémentation en contradiction avec un principe
ci-dessus DOIT être explicitement justifiée ou révisée.

Toute modification de cette constitution (ajout, suppression ou reformulation d'un principe)
DOIT être documentée dans un Sync Impact Report en tête de ce fichier et DOIT suivre le
versionnage sémantique suivant :
- MAJEUR : suppression ou redéfinition d'un principe de façon incompatible avec les versions
  précédentes.
- MINEUR : ajout d'un nouveau principe ou extension substantielle d'un principe existant.
- CORRECTIF : clarifications, reformulations ou corrections n'altérant pas le sens des principes.

Chaque revue de code ou de conception DOIT vérifier la conformité aux principes ci-dessus.

**Version**: 1.1.0 | **Ratified**: 2026-09-28 | **Last Amended**: 2026-11-20
