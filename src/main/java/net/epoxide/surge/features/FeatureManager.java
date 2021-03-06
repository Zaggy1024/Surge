package net.epoxide.surge.features;

import java.util.ArrayList;
import java.util.List;

import net.epoxide.surge.features.animation.FeatureDisableAnimation;
import net.epoxide.surge.features.gpucloud.FeatureGPUClouds;
import net.epoxide.surge.features.hideplayers.FeatureHidePlayer;
import net.epoxide.surge.features.loadtime.FeatureLoadTimes;
import net.epoxide.surge.features.redstonetoggle.FeatureRedstoneFix;
import net.epoxide.surge.features.renderculling.FeatureGroupRenderCulling;
import net.epoxide.surge.handler.ConfigurationHandler;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;

public class FeatureManager {
    
    /**
     * List of all registered features.
     */
    public static final List<Feature> FEATURES = new ArrayList<>();
    
    /**
     * List of all transformer features.
     */
    public static final List<Feature> TRANSFORMERS = new ArrayList<>();
    
    /**
     * This method is called before any mods have had a chance to initialize. Constructors
     * should take care not to reference any actual game code.
     */
    public static void initFeatures () {
        
        if (FMLLaunchHandler.side() == Side.CLIENT) {
            
            //Disabled for now// registerFeature(new FeatureGroupRenderCulling(), "Group Render Culling", "Cuts down on the amount of entities rendered, when they are bunched together.");
            registerFeature(new FeatureHidePlayer(), "Hide Players", "Command to disable the rendering of other players on the client.");
            registerFeature(new FeatureRedstoneFix(), "Redstone Toggle Fix", "Fixes a memory leak with toggle state of redstone torches. MC-101233");
            registerFeature(new FeatureGPUClouds(), "Cloud Rendering", "Switches the RenderGlobal to render clouds using GPU to render.");
            registerFeature(new FeatureDisableAnimation(), "Disable Animation", "Allows the animation of block/item textures to be disabled.");
        }
        
        registerFeature(new FeatureLoadTimes(), "Load Time Analysis", "Records the load time of all mods being loaded.");
    }
    
    /**
     * Registers a new feature with the feature manager. This will automatically create an
     * entry in the configuration file to enable/disable this feature. If the feature has been
     * disabled, it will not be registered. This will also handle event bus subscriptions.
     *
     * @param feature The feature being registered.
     * @param name The name of the feature.
     * @param description A short description of the feature.
     */
    private static void registerFeature (Feature feature, String name, String description) {
        
        feature.enabled = ConfigurationHandler.isFeatureEnabled(feature, name, description);
        
        if (feature.enabled) {
            
            feature.configName = name.toLowerCase().replace(' ', '_');
            FEATURES.add(feature);
            
            if (feature.isTransformer())
                TRANSFORMERS.add(feature);
        }
    }
}
