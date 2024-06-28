package net.npwdev.npweber.rng;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.Timer;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(RNG.MODID)
public class RNG {

    private static final Timer timer = new Timer();
    private static final BlockPos DECTECTION_BLOCKPOS = new BlockPos(150, 64, -292);
    private static final BlockPos CRACK_BLOCKPOS = new BlockPos(148, 65, -292);

    private static long serverDaytime = -1;
    private static long serverLevelUptime;
    private static boolean shouldStoreDaytime = true;
    private static boolean hasCalibratedNanoTime;

    private boolean testing;
    private long zombieSpawnMoment;
    private static boolean shouldCaptureItem;
    private static long itemYRotSeedLeftBits;
    private static long twoSeededDouble;

    // Define mod id in a common place for everything to reference
    public static final String MODID = "rng";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "rng" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "rng" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a new Block with the id "rng:example_block", combining the namespace and path
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
    // Creates a new BlockItem with the id "rng:example_block", combining the namespace and path
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block", () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));

    // Creates a new food item with the id "examplemod:example_id", nutrition 1 and saturation 2
    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEat().nutrition(1).saturationMod(2f).build())));

    // Creates a creative tab with the id "examplemod:example_tab" for the example item, that is placed after the combat tab
    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
            output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
            }).build());

    public RNG() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
        forgeEventBus.register(this);
        forgeEventBus.addListener(this::onInteract);
        forgeEventBus.addListener(this::onSpawn);
        forgeEventBus.addListener(this::onBlockDrop);
        serverDaytime = -1;
        shouldCaptureItem = true;

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(EXAMPLE_BLOCK_ITEM);
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent pie) {
//        float diff = pie.getLevel().getCurrentDifficultyAt(pie.getLevel().getChunk(187, -676).getPos().getWorldPosition()).getEffectiveDifficulty();
//        LOGGER.info(String.valueOf(diff));

        if(pie.getHand().equals(InteractionHand.MAIN_HAND)) {
            BlockPos clickedPos = pie.getPos();
            if (!testing && clickedPos.equals(DECTECTION_BLOCKPOS)) {
                testing = true;
                sendPlayerMessage("Now running the experiment.");
            }
        }
    }

    @SubscribeEvent
    public void onSpawn(MobSpawnEvent mse) {
        if(testing) {
            if (mse.getEntity() instanceof Zombie zombie) {
                if (zombie.canBreakDoors() && zombieSpawnMoment == 0) {
                    zombieSpawnMoment = System.nanoTime();
                    sendPlayerMessage("Zombie that can break doors spawned at: (%d; %f %f %f)".formatted(zombieSpawnMoment, zombie.getX(), zombie.getY(), zombie.getZ()));
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockDrop(BlockEvent.BreakEvent blockBreakEvent) {
        if(testing) {
            if (zombieSpawnMoment != 0L) {
                shouldCaptureItem = true;
                testing = false;
                zombieSpawnMoment = 0L;
            }
        }
    }

    private static long calculateServerLevelUptime(long loginServerTickCount) {
        return loginServerTickCount - serverDaytime;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static boolean shouldCaptureItem() {
        return shouldCaptureItem;
    }

    public static void disableItemCapture() {
        shouldCaptureItem = false;
    }

    public static void calculateItemYRotSeedLeftBits(float itemYRot) {
        itemYRotSeedLeftBits = (long)((itemYRot / 360.0F) * (1 << 24)) << 24;
        getLogger().info("itemYRot: {}, itemYRotSeedLeftBits: {}", itemYRot, itemYRotSeedLeftBits);
    }

    public static void getDaytime(long serverDaytime) {
        if(shouldStoreDaytime)
            RNG.serverDaytime = serverDaytime;
        else
            serverLevelUptime = calculateServerLevelUptime(serverDaytime);
    }

    public static void disableDaytimeStore() {
        shouldStoreDaytime = false;
    }

    public static boolean hasCalibratedNanoTime() {
        return hasCalibratedNanoTime;
    }

    public static void calibrateNanoTime(long serverKeepAlive) {
        serverKeepAlive = serverKeepAlive * 1_000_000;

        long serverLevelStartNanoTime;
        if(serverLevelUptime != 0L) {
            serverLevelStartNanoTime = serverKeepAlive - (serverLevelUptime * 50_000_000L);
            getLogger().info("serverLevelStartNanoTime: {}, serverCurrentNanoTime: {}", serverLevelStartNanoTime, serverKeepAlive);
        }

        hasCalibratedNanoTime = true;
    }

    public static void sendPlayerMessage(String message) {
        if(Minecraft.getInstance().player != null)
            Minecraft.getInstance().player.connection.sendChat(message);
    }
}
