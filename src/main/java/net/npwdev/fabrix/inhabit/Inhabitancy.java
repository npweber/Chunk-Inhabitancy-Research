package net.npwdev.fabrix.inhabit;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Inhabitancy.MODID)
public class Inhabitancy {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "inhabitancy";

    private static long serverTime;
    private static long serverNanoMoment;

    private static boolean shouldCaptureItem;
    private static boolean shouldCaptureTime = true;
    private static boolean shouldRecordKeepAlive;

    public Inhabitancy() {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public void onItemToss(ItemTossEvent itemTossEvent) {
        //if
        shouldCaptureItem = true;
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

    }

    public static Logger getLogger(){
        return LogManager.getLogger();
    }

    public static boolean shouldCapture() {
        return shouldCaptureItem;
    }

    public static boolean shouldRecordKeepAlive() {
        return shouldRecordKeepAlive;
    }

    public static void determineLeftSeedBits(float yRot) {
        shouldCaptureItem = false;
        yRot /= 360.0f;
        yRot *= 1 << 24;
        long leftBits = (long) yRot << 24;
        getLogger().debug("{}", leftBits);
    }

    public static void trackServerTime(long time) {
        if(shouldCaptureTime)
            serverTime = time;
        else if(serverNanoMoment != 0L) {
            shouldRecordKeepAlive = false;
            serverNanoMoment -= (time - serverTime) * 50_000_000;
        }
    }

    public static void stopTrackingServerTime(){
        shouldCaptureTime = false;
    }

    public static void captureServerNanoMoment(long keepAlive){
        serverNanoMoment = keepAlive;
    }
}
