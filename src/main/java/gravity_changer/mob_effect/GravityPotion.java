package gravity_changer.mob_effect;

import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;

import java.util.EnumMap;

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
    
    public static final EnumMap<Direction, Potion> DIR_POTIONS = new EnumMap<>(Direction.class);
    
    static {
        for (Direction direction : Direction.values()) {
            Potion potion = new Potion(
                new MobEffectInstance(
                    GravityDirectionMobEffect.EFFECT_MAP.get(direction), 9600, 1
                )
            );
            DIR_POTIONS.put(direction, potion);
        }
    }
    
    public static ResourceLocation getPotionId(Direction direction) {
        return switch (direction) {
            case DOWN -> new ResourceLocation("gravity_changer:gravity_down_0");
            case UP -> new ResourceLocation("gravity_changer:gravity_up_0");
            case NORTH -> new ResourceLocation("gravity_changer:gravity_north_0");
            case SOUTH -> new ResourceLocation("gravity_changer:gravity_south_0");
            case WEST -> new ResourceLocation("gravity_changer:gravity_west_0");
            case EAST -> new ResourceLocation("gravity_changer:gravity_east_0");
        };
    }
    
    public static final Potion[] ALL = new Potion[]{
        STRENGTH_DECR_POTION_0,
        STRENGTH_DECR_POTION_1,
        STRENGTH_INCR_POTION_0,
        STRENGTH_INCR_POTION_1,
        DIR_POTIONS.get(Direction.DOWN),
        DIR_POTIONS.get(Direction.UP),
        DIR_POTIONS.get(Direction.NORTH),
        DIR_POTIONS.get(Direction.SOUTH),
        DIR_POTIONS.get(Direction.WEST),
        DIR_POTIONS.get(Direction.EAST)
    };
    
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
    
        for (Direction direction : Direction.values()) {
            Potion potion = DIR_POTIONS.get(direction);
            Registry.register(
                BuiltInRegistries.POTION,
                getPotionId(direction),
                potion
            );
        }
    }
}
