---
name: GeckoLib sound keyframe fix
description: Dans les setSoundKeyframeHandler GeckoLib (client-side), utiliser world.playSound sans PlayerEntity
type: feedback
---

Dans les `setSoundKeyframeHandler` de GeckoLib, le handler tourne côté client. Il faut utiliser la surcharge **sans** `PlayerEntity` :

```java
this.getWorld().playSound(x, y, z, sound, category, vol, pitch, false);
```

Et non pas :

```java
this.getWorld().playSound(null, x, y, z, sound, category, vol, pitch); // ← ne fonctionne pas côté client
```

**Why:** La surcharge avec `PlayerEntity except` est conçue pour le serveur (envoi de paquets). Sur `ClientWorld`, elle ne joue pas correctement le son.

**How to apply:** Toutes les entités GeckoLib qui utilisent `setSoundKeyframeHandler` doivent utiliser cette forme.
