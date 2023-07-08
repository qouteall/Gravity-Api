# GravityChanger

This is a fork of FusionFlux's [Gravity API](https://github.com/Fusion-Flux/Gravity-Api) for Fabric.
Gravity API is originally a fork of Gaider10's [Gravity Changer](https://github.com/Gaider10/GravityChanger).

Maintaining the fork because Immersive Portals mod depends on the Gravity changing mod, 
and Fabric mod cannot depend on a Quilt mod.

This Gravity Changer mod is not identical to Fusion's Gravity API.
**The two mods cannot be used interchangeably.**

Its gravity plating functionality is based on contents from [AmethystGravity](https://modrinth.com/mod/amethyst-gravity) by CyborgCabbage.

### Future development goal

* Explore the possibility of using world-coordinate velocity instead of entity-local velocity to simplify future development. Do aggressive rewrite to MC collision code and use Lithium's collision code which is faster.
* Utilize vanilla effect system to replace the current gravity list thing and gravity inversion. This will also make gravity potion easier to implement. The effects could also be used for gravity strength changing.
* Let the user mod to do its verification logic. No need to put verification inside Gravity Changer. (Immersive Portals does its teleportation verification outside of gravity changer.) This will reduce overall complexity.
