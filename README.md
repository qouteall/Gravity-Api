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

### Commands

This mod's commands are different to Gravity API's. These are the commands in the latest version of this mod:

`/gravity set_base_direction <direction> [entities]` sets the base gravity direction. (The base direction can be overridden by other things including effects, gravity anchor and gravity plating). Without `[entities]` argument it will target the command sender (the same applies to all commands). Examples: `/gravity set_base_direction up`   `/gravity set_base_direction up @e[type=!minecraft:player]`

`/gravity set_base_strength <strength> [entities]` sets the base gravity strength. The strength effects will multiply on the base strength (instead of overriding it). Examples: `/gravity set_base_strength 0.5`  `/gravity set_base_strength 0.5 @e`

`/gravity view` shows the base gravity direction and strength of the command sender.

`/gravity reset [entities]` reset the base gravity direction and strength. 

`/gravity randomize_base_direction [entities]` sets the base direction as a random direction.

`/gravity set_relative_base_direction <relativeDirection> [entities]` sets the gravity direction as a direction relative to the entity's viewing direction. The `<relativeDirection>` can be `forward`, `backward`, `left`, `right`, `up` or `down`.

`/gravity set_dimension_gravity_strength <strength>` sets the dimensional gravity strength for the current dimension. 

`/gravity view_dimension_info` shows the dimensional gravity strength for the current dimension.


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

