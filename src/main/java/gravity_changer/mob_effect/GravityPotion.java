package gravity_changer.mob_effect;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;

public class GravityPotion {
    public static Potion STRENGTH_DECR_POTION_0 = new Potion(
        new MobEffectInstance(
            GravityStrengthMobEffect.DECREASE, 9600, 0
        )
    );
    
    public static Potion STRENGTH_DECR_POTION_1 = new Potion(
        new MobEffectInstance(
            GravityStrengthMobEffect.DECREASE, 9600, 1
        )
    );
    
    public static Potion STRENGTH_INCR_POTION_0 = new Potion(
        new MobEffectInstance(
            GravityStrengthMobEffect.INCREASE, 9600, 0
        )
    );
    
    public static Potion STRENGTH_INCR_POTION_1 = new Potion(
        new MobEffectInstance(
            GravityStrengthMobEffect.INCREASE, 9600, 1
        )
    );
    
    public static void init() {
        Registry.register(
            BuiltInRegistries.POTION,
            new ResourceLocation("gravity_changer:gravity_decr_0"),
            STRENGTH_DECR_POTION_0
        );
        
        Registry.register(
            BuiltInRegistries.POTION,
            new ResourceLocation("gravity_changer:gravity_decr_1"),
            STRENGTH_DECR_POTION_1
        );
        
        Registry.register(
            BuiltInRegistries.POTION,
            new ResourceLocation("gravity_changer:gravity_incr_0"),
            STRENGTH_INCR_POTION_0
        );
        
        Registry.register(
            BuiltInRegistries.POTION,
            new ResourceLocation("gravity_changer:gravity_incr_1"),
            STRENGTH_INCR_POTION_1
        );
    }
}
