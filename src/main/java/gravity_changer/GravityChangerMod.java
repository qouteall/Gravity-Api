package gravity_changer;

import gravity_changer.command.GravityCommand;
import gravity_changer.config.GravityChangerConfig;
import gravity_changer.item.ModItems;
import gravity_changer.util.GravityChannel;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GravityChangerMod implements ModInitializer {
    public static final String NAMESPACE = "gravity_changer";
    public static final Logger LOGGER = LogManager.getLogger(GravityChangerMod.class);
    
    public static ItemGroup GravityChangerGroup;
    
    public static ConfigHolder<GravityChangerConfig> configHolder;
    public static GravityChangerConfig config;
    
    @Override
    public void onInitialize() {
        ModItems.init();
        GravityChannel.initServer();

        AutoConfig.register(GravityChangerConfig.class, GsonConfigSerializer::new);
        configHolder = AutoConfig.getConfigHolder(GravityChangerConfig.class);
        config = configHolder.getConfig();

        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> GravityCommand.register(dispatcher)
        );
    
        GravityChangerGroup = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModItems.GRAVITY_CHANGER_UP))
            .entries((enabledFeatures, entries) -> {
                entries.add(new ItemStack(ModItems.GRAVITY_CHANGER_UP));
                entries.add(new ItemStack(ModItems.GRAVITY_CHANGER_DOWN));
                entries.add(new ItemStack(ModItems.GRAVITY_CHANGER_EAST));
                entries.add(new ItemStack(ModItems.GRAVITY_CHANGER_WEST));
                entries.add(new ItemStack(ModItems.GRAVITY_CHANGER_NORTH));
                entries.add(new ItemStack(ModItems.GRAVITY_CHANGER_SOUTH));
                
                entries.add(new ItemStack(ModItems.GRAVITY_CHANGER_UP_AOE));
                entries.add(new ItemStack(ModItems.GRAVITY_CHANGER_DOWN_AOE));
                entries.add(new ItemStack(ModItems.GRAVITY_CHANGER_EAST_AOE));
                entries.add(new ItemStack(ModItems.GRAVITY_CHANGER_WEST_AOE));
                entries.add(new ItemStack(ModItems.GRAVITY_CHANGER_NORTH_AOE));
                entries.add(new ItemStack(ModItems.GRAVITY_CHANGER_SOUTH_AOE));
            })
            .displayName(Text.translatable("itemGroup.gravity_changer"))
            .build();
    
        Registry.register(
            Registries.ITEM_GROUP, id("general"),
            GravityChangerGroup
        );
    }

    public static Identifier id(String path) {
        return new Identifier(NAMESPACE, path);
    }
}
