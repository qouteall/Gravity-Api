package gravity_changer.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class EntityTags {
    public static final TagKey<EntityType<?>> ALLOWED_NON_LIVING = TagKey.create(
        BuiltInRegistries.ENTITY_TYPE.key(),
        new ResourceLocation("gravity_changer", "allowed_non_living")
    );
    
    public static boolean canChangeGravity(Entity entity) {
        if (entity instanceof LivingEntity) {
            return true;
        }
        
        return entity.getType().builtInRegistryHolder().is(ALLOWED_NON_LIVING);
    }
    
    public static boolean allowGravityTransformationInRendering(Entity entity) {
        return true;
    }
}
