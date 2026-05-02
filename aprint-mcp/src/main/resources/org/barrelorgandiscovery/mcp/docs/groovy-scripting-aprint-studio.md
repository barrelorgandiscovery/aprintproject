# Scripting Groovy — APrint Studio

Guide d’orientation pour écrire des scripts Groovy dans la console APrint, les **quickscripts** (`.aprintbookgroovyscript`) et les scripts d’instrument. Le code s’appuie sur l’API Java d’APrint, accessible depuis Groovy.

## Documentation en ligne (site officiel)

- Vue d’ensemble scripting : `https://www.barrel-organ-discovery.org/site/doc/2020/product/scripting/scripting/`
- Premiers pas / quickscripts : `https://www.barrel-organ-discovery.org/site/doc/2020/product/scripting/scripting_first_steps_quickscripts/`
- Transformation de carton : `https://www.barrel-organ-discovery.org/site/doc/2020/product/scripting/TransformerCarton/`
- Javadoc complète du produit : `https://www.barrel-organ-discovery.org/site/doc/2020/javadoc/`

## Variables prédéfinies dans la console (carton ouvert)

| Variable | Rôle |
|----------|------|
| `virtualbook` | Instance `VirtualBook` du carton courant |
| `pianoroll` | Composant piano-roll (`JPianoRollComponent`) — sélection, édition visuelle |
| `currentinstrument` | `Instrument` utilisé pour ce carton |
| `toolbarspanel` | Panneau des barres d’outils |
| `services` | Accès application (`APrintNG` / services généraux) : dépôt, ouverture de livres, etc. |

En Groovy vous pouvez aussi utiliser les imports Java habituels et les classes du paquet `org.barrelorgandiscovery.*`.

### Références utiles (Javadoc)

- `VirtualBook` : `org.barrelorgandiscovery.virtualbook.VirtualBook`
- `Scale` (gamme) : `org.barrelorgandiscovery.scale.Scale`
- Piano-roll : `org.barrelorgandiscovery.gui.ainstrument.pianoroll.JPianoRollComponent`
- Définition de pistes : `org.barrelorgandiscovery.scale.TrackDef` et sous-classes (percussions, registres, etc.)

## Dépôt : gammes et instruments

`services.getRepository()` retourne un `Repository2`, qui regroupe la gestion des **instruments** et des **gammes** (`InstrumentManager` + `ScaleManager`).

- **Liste / chargement** : `listInstruments()`, `getInstrument(String)`, `listScales()`, `getScale(String)` — signatures exactes selon `InstrumentManager` / `ScaleManager` sur `Repository2`.
- **Enregistrement** : `saveInstrument(Instrument)` et `saveScale(Scale)` (hérités par `Repository2`).
- **Lecture seule** : si `repository.isReadOnly()` est vrai, les sauvegardes échouent (dépôt intégré ou non modifiable). Configurez un répertoire utilisateur accessible en écriture dans les préférences.

Exemple d’idée (à adapter au contexte réel, gestion d’erreurs, EDT) :

```groovy
def repo = services.getRepository()
assert !repo.isReadOnly() : "Dépôt non modifiable"
// ins = ... construit ou chargé puis modifié
// repo.saveInstrument(ins)
// scale = ... 
// repo.saveScale(scale)
```

Pour des éditions complexes (registres, images, tuyaux), l’UI **éditeur d’instrument** / **éditeur de gamme** reste souvent le plus sûr ; le script automatise ensuite chargement, copie, ajustements.

## Helpers Groovy fournis avec le produit

Paquet **`groovy.aprint.transform`** (inclus dans l’application) :

- **`ScaleHelper`** — navigation dans une gamme : pistes par registre (`melody`, `bass`, …), notes, correspondance pistes / registres.
- **`TransformHelper`** — construire une transposition / correspondance entre deux gammes (`LinearTransposition`), mapper des pistes, appeler `transform(virtualbook)`.

Exemple minimal :

```groovy
import groovy.aprint.transform.ScaleHelper
import groovy.aprint.transform.TransformHelper

def src = new ScaleHelper(scale: virtualbook.scale)
def dst = new ScaleHelper(scale: services.getRepository().getScale("Nom de la gamme cible"))
def th = new TransformHelper(src.scale, dst.scale)
// th.map(...) puis résultat = th.transform(virtualbook)
```

Autres paquets utiles : `groovy.aprint.tools` (choix de fichiers / dossiers), `groovy.aprint.midi` (aide MIDI).

## « Éléments » du carton : trous, notes, sélection

- Le carton est un ensemble de **trous** (`Hole`) rattachés à des **pistes** et des **temps**.
- `virtualbook.getHolesCopy()` ou itération sur les trous selon l’API `VirtualBook`.
- Pour sélectionner ou filtrer depuis l’UI : API du **`pianoroll`** (méthodes de sélection — voir Javadoc `JPianoRollComponent`).
- Les **percussions** se distinguent via la définition de piste et le code MIDI général associé (`TrackDef` / classes spécialisées percussion).

## Quickscripts et console

- Les **quickscripts** automatisent des actions sur le carton (souvent avec `virtualbook` / `pianoroll`).
- Extension : `.aprintbookgroovyscript` (ouverture depuis APrint comme script Groovy).
- La **console script** partage le même type de binding (`services`, etc. selon le contexte d’ouverture).

## Pistes pour créer une gamme ou un instrument

1. **Gamme** : modèle `Scale`, nombre de pistes, définitions `TrackDef`, nom — construire ou dupliquer une gamme existante puis `saveScale` sur un dépôt writable.
2. **Instrument** : modèle `Instrument` lié à une `Scale`, jeux de registres / tuyaux / ressources sonores — souvent en partant d’un instrument existant (`getInstrument`) qu’on clone en mémoire avant `saveInstrument` sous un nouveau nom.
3. Vérifier la cohérence **nombre de pistes** gamme / carton lors des imports ou transpositions (messages d’erreur ou dry-run côté outils MCP d’import si utilisés).

## Ressource MCP

Ce texte est aussi exposé en ressource **`aprint://docs/groovy-scripting.md`** par le serveur MCP APrint (même contenu que l’outil `get_groovy_scripting_documentation`).
