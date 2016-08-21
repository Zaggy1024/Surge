package net.epoxide.surge.features.renderculling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.epoxide.surge.asm.mappings.Mapping;
import net.epoxide.surge.command.CommandSurgeWrapper;
import net.epoxide.surge.features.Feature;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FeatureGroupRenderCulling extends Feature {
    
    private static String CLASS_RENDER_MANAGER;
    private static Mapping METHOD_DO_RENDER_ENTITY;
    private static int cullThreshold;
    
    /**
     * Whether or not entitie rendering should be culled. Allows for entity rendering to be
     * culled.
     */
    private static boolean shouldCull = true;
    
    private static final Map<EntityLivingBase, List<EntityLivingBase>> parentMap = new WeakHashMap<>();
    private static List<EntityLivingBase> cullList = new ArrayList<>();
    
    @Override
    public void onInit () {
        
        CommandSurgeWrapper.addCommand(new CommandGroupRenderCulling());
    }
    
    /**
     * Toggles render culling on/off. Will also reset culling maps.
     */
    public static void toggleRenderCull () {
        
        shouldCull = !shouldCull;
        
        // TODO remove
        for (final EntityLivingBase entityLivingBase : parentMap.keySet()) {
            
            entityLivingBase.setCustomNameTag("");
            entityLivingBase.setAlwaysRenderNameTag(false);
        }
        
        parentMap.clear();
    }
    
    /**
     * Checks if mass render culling should be enabled.
     *
     * @return Whether or not mass culling should happen.
     */
    public static boolean shouldRenderCull () {
        
        return shouldCull;
    }
    
    /**
     * Custom render event hook that is fired long before forge's. Allows for culling of fire
     * rendering and the rendering on non living entities.
     *
     * @param entity The entity being rendered.
     * @return Whether or not the entity should render.
     */
    public static boolean shouldRender (Entity entity) {
        
        if (shouldCull) {
            
            if (entity instanceof EntityPlayer || !(entity instanceof EntityLivingBase))
                return true;

            final EntityLivingBase living = (EntityLivingBase) entity;
            if (cullList.contains(living))
                return false;
            else if (parentMap.containsKey(living)) {
                final List<EntityLivingBase> entityList = living.getEntityWorld().getEntitiesWithinAABB(living.getClass(), living.getEntityBoundingBox());
                
                entityList.remove(living);
                
                final List<EntityLivingBase> childMap = parentMap.get(living);
                cullList.removeAll(childMap);
                
                childMap.clear();
                if (entityList.size() > cullThreshold) {
                    childMap.addAll(entityList);
                    cullList.addAll(entityList);
                    living.setCustomNameTag("Culled: " + entityList.size());
                    living.setAlwaysRenderNameTag(true);
                }
                else
                    parentMap.remove(living);

            }
            else if (!parentMap.containsKey(living)) {
                final List<EntityLivingBase> entityList = living.getEntityWorld().getEntitiesWithinAABB(living.getClass(), living.getEntityBoundingBox());
                
                entityList.remove(living);
                
                final List<EntityLivingBase> childMap = new ArrayList<>();
                
                if (entityList.size() > cullThreshold) {
                    childMap.addAll(entityList);
                    cullList.addAll(entityList);
                    living.setCustomNameTag("Culled: " + entityList.size());
                    living.setAlwaysRenderNameTag(true);
                    parentMap.put(living, childMap);
                }
            }
        }
        return true;
    }
    
    @Override
    public void readNBT (NBTTagCompound nbt) {
        
        shouldCull = nbt.getBoolean("shouldCull");
    }
    
    @Override
    public void writeNBT (NBTTagCompound nbt) {
        
        nbt.setBoolean("shouldCull", shouldCull);
    }
    
    @Override
    public boolean usesEvents () {
        
        return true;
    }
}