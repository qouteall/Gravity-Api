package gravity_changer;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import gravity_changer.config.GravityChangerConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class GravityChangerComponents implements EntityComponentInitializer, WorldComponentInitializer {
    
    public static final ResourceLocation DATA_COMPONENT_ID =
        new ResourceLocation("gravity_changer", "gravity_data");
    
    public static final ComponentKey<GravityComponent> GRAVITY_COMP_KEY =
        ComponentRegistry.getOrCreate(DATA_COMPONENT_ID, GravityComponent.class);
    
    public static final ResourceLocation DIMENSION_DATA_ID =
        new ResourceLocation("gravity_changer", "dimension_data");
    
    public static final ComponentKey<DimensionGravityDataComponent> DIMENSION_COMP_KEY =
        ComponentRegistry.getOrCreate(DIMENSION_DATA_ID, DimensionGravityDataComponent.class);
    
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(
            GRAVITY_COMP_KEY, GravityComponent::new,
            new RespawnCopyStrategy<GravityComponent>() {
                @Override
                public void copyForRespawn(GravityComponent from, GravityComponent to, boolean lossless, boolean keepInventory, boolean sameCharacter) {
                    if (lossless || !GravityChangerMod.config.resetGravityOnRespawn) {
                        RespawnCopyStrategy.copy(from, to);
                    }
                }
            }
        );
        registry.registerFor(Entity.class, GRAVITY_COMP_KEY, GravityComponent::new);
    }
    
    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(DIMENSION_COMP_KEY, DimensionGravityDataComponent::new);
    }
}
