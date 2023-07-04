package gravity_changer;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class GravityChangerComponents implements EntityComponentInitializer, WorldComponentInitializer {
    
    public static final ResourceLocation DATA_COMPONENT_ID =
        new ResourceLocation("gravity_changer", "gravity_data");
    
    public static final ComponentKey<GravityDirectionComponent> GRAVITY_COMP_KEY =
        ComponentRegistry.getOrCreate(DATA_COMPONENT_ID, GravityDirectionComponent.class);
    
    public static final ResourceLocation DIMENSION_DATA_ID =
        new ResourceLocation("gravity_changer", "dimension_data");
    
    public static final ComponentKey<GravityDimensionStrengthComponent> DIMENSION_COMP_KEY =
        ComponentRegistry.getOrCreate(DIMENSION_DATA_ID, GravityDimensionStrengthComponent.class);
    
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(Entity.class, GRAVITY_COMP_KEY, GravityDirectionComponent::new);
    }
    
    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(DIMENSION_COMP_KEY, GravityDimensionStrengthComponent::new);
    }
}
