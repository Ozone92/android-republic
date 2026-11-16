# King of Republic

Jeu Android inspiré de *King of Tokyo*, transposé dans une élection présidentielle française. Un joueur humain affronte trois bots : chacun incarne un candidat et tente d'entrer à l'Élysée, d'y rester et d'atteindre 20 votes avant les autres.

Projet du module IPAN2 (EPITA) · Kotlin · Android 10+ (API 29) · architecture MVVM.

## Équipe

| Membre | GitHub | Contributions principales |
|---|---|---|
| Sunny Soyer | [SunnyBara](https://github.com/SunnyBara) | Choix du candidat, boutique, icônes des dés et des stats, portraits, fiche joueur, sons et répliques |
| Romain Vidal | [Ozone92](https://github.com/Ozone92) | Navigation, accueil, écran de jeu et boucle de partie, cartes influence, écran de fin |
| François Duguay | [Francoisduguay](https://github.com/Francoisduguay) | Les 30 cartes pouvoir et leurs effets, achats des bots |
| Corentin Dupaigne | [corentin-dupaigne](https://github.com/corentin-dupaigne) | Inventaire des composants, design system, corrections de la logique, aide et clarté du jeu, interface finale |
| Jesy Crouvisier | [Jesy1711](https://github.com/Jesy1711) | |

## Gestion de projet

Le suivi se fait sur GitHub :

- **[Issues](https://github.com/Ozone92/android-republic/issues?q=is%3Aissue)** : une issue par fonctionnalité ou bug, avec un responsable.
- **[Pull requests](https://github.com/Ozone92/android-republic/pulls?q=is%3Apr)** : chaque PR indique l'issue qu'elle traite (« Issue liée : #N » ou « Closes #N »).
- **[Milestones](https://github.com/Ozone92/android-republic/milestones)** : le planning, en quatre étapes datées.

| Milestone | Échéance | Contenu |
|---|---|---|
| 1 · Maquette et navigation | 20 oct. 2026 | Figma, nav-graph, accueil, page de jeu |
| 2 · Cœur du jeu | 28 oct. 2026 | Choix du candidat, boutique, cartes influence, transitions, écran de fin, inventaire |
| 3 · Design system et ergonomie | 8 nov. 2026 | Design system, icônes, mode paysage, portraits, fiche joueur |
| 4 · Contenu et finitions | 16 nov. 2026 | Cartes pouvoir, sons et répliques, aide, clarté du jeu, interface finale |

## Règles en bref

- Chaque candidat commence avec **0 vote, 10 de popularité, 0 € et 0 d'influence**. On gagne en atteignant **20 votes**, ou en restant **le dernier candidat populaire** (popularité à 0 = éliminé).
- À son tour, on lance **6 dés**, avec 2 relances. Entre deux lancers, on touche les dés à garder.
  - **Trois 1, 2 ou 3 identiques ou plus** : autant de votes que le chiffre, +1 par dé en plus. Trois 1 donnent aussi +2 d'influence, trois 2 donnent +1.
  - **Argent** : +1 € par dé. **Popularité** : +1 par dé, sauf à l'Élysée. **Dégât** : −1 de popularité aux adversaires.
- **L'Élysée** : si elle est vide, on y entre (+1 vote), puis on gagne +2 votes au début de chaque tour. Celui qui y est frappe tous les autres, mais tous les autres le frappent. Quand on l'attaque, il peut rester ou céder sa place à l'attaquant.
- **Cartes influence** : à 3, 6 et 10 d'influence, on choisit une carte (bonus immédiat ou permanent).
- **Cartes pouvoir** : en fin de tour, on dépense son argent dans une boutique de 3 cartes, qu'on peut renouveler pour 2 €.

Ces règles sont aussi expliquées dans l'application : un écran « Comment jouer » de 5 pages s'affiche à la première partie et reste accessible depuis l'accueil et le bouton ? en jeu. Pendant la partie, une phrase au-dessus des dés indique quoi faire.

## Contenu du jeu

- **9 candidats** jouables, avec portrait et répliques vocales (`domain/Character.kt`).
- **40 cartes programmables** :
  - **30 cartes pouvoir** (`repository/PowerCards.kt`) : 13 *Action* à effet immédiat et 17 *Pouvoir* permanentes. Leurs effets sont décrits par la classe scellée `PowerEffect` (`repository/PowerCard.kt`) et appliqués par `domain/PowerLogic.kt`.
  - **10 cartes influence** (`repository/InfluenceCard.kt`), réparties sur 3 paliers, appliquées par `domain/InfluenceLogic.kt` et `GameViewModel`.
- **3 bots** : ils choisissent leurs dés (`GameLogic.chooseBotLockedDice`), leurs achats (`PowerLogic.chooseBotShopAction`) et décident de quitter l'Élysée sous la moitié de leur popularité maximale.

## Architecture

L'application suit le modèle **MVVM** : l'interface observe l'état exposé par les ViewModels et ne contient aucune règle de jeu.

```
republic/app/src/main/java/com/epita/republic/
├── domain/        Règles pures, sans dépendance Android
│                  GameLogic, PowerLogic, PowerShop, InfluenceLogic, DiceFace, Character…
├── repository/    Données du jeu : Player, PowerCard / PowerCards, InfluenceCard
├── views/         Une Activity, des Fragments et leurs ViewModels
│   ├── home/        MainActivity, HomeFragment
│   ├── character/   Choix du candidat
│   ├── game/        GameFragment, GameViewModel, PlayerCard, boutique, fiches joueur
│   ├── ending/      Écran de fin et classement
│   ├── help/        Écran « Comment jouer »
│   └── common/      Vues personnalisées (hémicycles) et utilitaires
└── audio/         Sons et répliques (GameSoundPlayer)
```

**Choix principaux**

- **Une Activity, plusieurs Fragments** reliés par le Navigation Component (`res/navigation/nav_graph.xml`). Les transitions et le passage d'arguments sont déclarés à un seul endroit.
- **`GameViewModel` porte toute la partie** : une coroutine dans `viewModelScope` déroule la boucle de jeu (dés, résolution, influence, Élysée, boutique, tour suivant). L'état est exposé en `LiveData`. La partie survit donc aux changements de configuration.
- **Attente du joueur sans callbacks** : quand c'est au tour de l'humain, la boucle se suspend sur `SignalController.await()` et reprend quand l'interface appelle `sendNotify()`. Pour un bot, un simple `delay` remplace l'attente. Humains et bots suivent ainsi exactement le même code.
- **Événements ponctuels** (sons, résumé du lancer) émis par des `Channel` exposés en `Flow`, pour qu'ils ne soient pas rejoués à la rotation.
- **Cartes pouvoir déclaratives** : une carte est une donnée (nom, coût, effet). Ajouter une carte revient à ajouter une ligne dans `PowerCards.kt`, sans toucher à la boucle de jeu.
- **Interface en Views XML et Material 3** : `ConstraintLayout`, `MaterialCardView`, indicateurs de progression Material, `DialogFragment`. Deux vues dessinées sur `Canvas` (`HemicycleBackgroundView` et `HemicycleArcsView`) pour les fonds.
- **Design system** : couleurs, typographie, espacements et composants définis dans `res/values` (thèmes clair et sombre, attributs `color*` propres au jeu), documentés dans `docs/design-system.pdf`.
- **Plusieurs tailles d'écran** : l'application est en paysage, avec des dimensions adaptées aux tablettes dans `values-sw600dp`.

## Documentation

| Fichier | Contenu |
|---|---|
| [`docs/inventaire.pdf`](docs/inventaire.pdf) | Inventaire des composants : plateau, fiches candidat, dés, les 40 cartes, écrans d'aide |
| [`docs/inventaire-illustre.pdf`](docs/inventaire-illustre.pdf) | Même inventaire, avec les portraits des candidats |
| [`docs/design-system.pdf`](docs/design-system.pdf) | Design system : tokens, couleurs, typographie, composants ([version HTML](docs/design-system.html)) |
| [`republic/docs/game-sounds.md`](republic/docs/game-sounds.md) | Sons du jeu et leurs déclencheurs |

## Compiler et lancer

Prérequis : Android Studio récent (JDK 17 ou plus), SDK Android 37, un appareil ou un émulateur sous Android 10 (API 29) ou plus.

1. Ouvrir le dossier `republic/` dans Android Studio et laisser Gradle se synchroniser.
2. Choisir un appareil et lancer la configuration `app`.

En ligne de commande :

```bash
cd republic
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
