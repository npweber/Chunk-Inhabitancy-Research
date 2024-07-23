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

import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Inhabitancy.MOD_ID)
public class Inhabitancy {

    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "inhabitancy";

    private static long serverTick;
    private static long serverDownTick;
    private static long serverUpNano;
    private static long yRotLeftSeedBits;
    private static long serverUptimeTicks;

    private static boolean shouldDetermineLeftSeedBits;
    private static boolean shouldRecordServerDownTick = true;
    private static boolean shouldDetermineServerUpNano;

    public Inhabitancy() {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public void onItemToss(ItemTossEvent itemTossEvent) {
        shouldDetermineLeftSeedBits = true;
        if(serverUpNano != 0 && serverUptimeTicks != 0) {
            getLogger().info("All parameters to determine initial seed are found. Calling determine_initial_seed.py...");
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    callDetermineInitialSeed(serverUpNano, yRotLeftSeedBits, serverUptimeTicks);
                }
            }, 500L);
        }
        else {
            getLogger().warn("Need serverUpNano and serverUptimeTicks in order to determine initial seed.");
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

    }

    public static Logger getLogger(){
        return LogManager.getLogger();
    }

    public static void determineLeftSeedBits(float yRot) {
        if(shouldDetermineLeftSeedBits) {
            getLogger().info("Received yRot: {}. Determining yRotLeftSeedBits...", yRot);
            shouldDetermineLeftSeedBits = false;
            yRot /= 360.0f;
            yRot *= 1 << 24;
            yRotLeftSeedBits = (long) yRot << 24;
            getLogger().info("yRotLeftSeedBits: {}", yRotLeftSeedBits);
        }
    }

    public static void trackServerTick(long tick) {
        if(shouldRecordServerDownTick)
            serverDownTick = tick;
        else
            serverTick = tick;
    }

    public static void stopWaitingForServerDown(){
        getLogger().info("Disconnected after server down. Waiting for rejoin to determine serverUpNano.");
        shouldRecordServerDownTick = false;
        shouldDetermineServerUpNano = true;
    }

    public static void determineServerUpNano(long keepAlive){
        getLogger().info("Received keepAlive: {}", keepAlive);
        if(shouldDetermineServerUpNano) {
            shouldDetermineServerUpNano = false;
            keepAlive *= 1_000_000;
            serverUptimeTicks = serverTick - serverDownTick;
            serverUpNano = keepAlive - (serverUptimeTicks * 50_000_000);
            getLogger().info("Found serverUptimeTicks: {} and serverUpNano: {}.", serverUptimeTicks, serverUpNano);
        }
    }

    private void callDetermineInitialSeed(long upNano, long yRotSeed, long uptimeTicks) {
        try {
            URL pyScriptPath = getClass().getResource("determine_initial_seed.py");
            assert pyScriptPath != null;
            String command = "python /c start python %s %d %d %d".formatted(pyScriptPath.getPath(), upNano, yRotSeed, uptimeTicks);
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
