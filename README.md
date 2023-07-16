# GravityChanger

This is a fork of FusionFlux's [Gravity API](https://github.com/Fusion-Flux/Gravity-Api) for Fabric.
Gravity API is originally a fork of Gaider10's [Gravity Changer](https://github.com/Gaider10/GravityChanger).

Maintaining the fork because Immersive Portals mod depends on the Gravity changing mod, 
and Fabric mod cannot depend on a Quilt mod.

This Gravity Changer mod is not identical to Fusion's Gravity API.
**The two mods cannot be used interchangeably.**

### Added features

* Gravity effects and potions.
* Gravity anchor. Changes gravity when held. (Some resources come from [AmethystGravity](https://modrinth.com/mod/amethyst-gravity) by CyborgCabbage)
* Gravity plating. Generates gravity field. Allows adjusting effect range. (Some code and resources come from AmethystGravity)

The gravity potions, anchors and plating are not obtainable from normal survival.

### How to use the API

#### Add dependency

Add this into `repositories`
```
maven { url 'https://jitpack.io' }
```

Add this into `dependencies`
```
modImplementation("com.github.qouteall:GravityChanger:v1.0.2-mc1.20.1")
```

### Future development goal

* Explore the possibility of using world-coordinate velocity (although it requires more mixin it's easier to debug and maintain) instead of entity-local velocity to simplify future development. Do aggressive rewrite (simpler and faster than many mixins) to MC collision code and use Lithium's collision code which is faster.
