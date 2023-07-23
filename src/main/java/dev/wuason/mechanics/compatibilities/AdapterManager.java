package dev.wuason.mechanics.compatibilities;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.compatibilities.plugins.customitems.CustomItemsImpl;
import dev.wuason.mechanics.compatibilities.plugins.executableblocks.ExecutableBlocksImpl;
import dev.wuason.mechanics.compatibilities.plugins.executableitems.ExecutableItemsImpl;
import dev.wuason.mechanics.compatibilities.plugins.itemsadder.ItemsAdderImpl;
import dev.wuason.mechanics.compatibilities.plugins.mmoitems.MMOItemsImpl;
import dev.wuason.mechanics.compatibilities.plugins.mythic.MythicImpl;
import dev.wuason.mechanics.compatibilities.plugins.oraxen.OraxenImpl;
import dev.wuason.mechanics.compatibilities.plugins.storagemechanic.StorageMechanicImpl;
import dev.wuason.mechanics.utils.AdventureUtils;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AdapterManager {

    private Mechanics core;

    private static HashMap<String,Implementation> types = new HashMap<>(){{

        put("MC",null);

    }};

    public AdapterManager(Mechanics core){
        this.core = core;
        load();
    }
    public void load(){
        Implementation[] implementations = {new VanillaImpl(), new StorageMechanicImpl(), new OraxenImpl(), new MythicImpl(), new MMOItemsImpl(), new ItemsAdderImpl(), new ExecutableItemsImpl(), new ExecutableBlocksImpl(), new CustomItemsImpl()};
        AdventureUtils.sendMessagePluginConsole(" <aqua>Implementations:");
        AdventureUtils.sendMessagePluginConsole(" <yellow>" + types.keySet().stream().toList());
    }

    public ItemStack getItemStack(String itemID){
        String type = itemID.substring(0,itemID.indexOf(":"));
        itemID = itemID.substring(itemID.indexOf(":") + 1);
        Implementation impl = types.get(type.toUpperCase(Locale.ENGLISH));
        if(impl == null) return null;
        return impl.getAdapterItem(itemID);
    }

    public String getAdapterID(ItemStack itemStack){
        if(itemStack == null) return null;
        String id = "mc:" + itemStack.getType().toString().toLowerCase();
        for(Implementation impl : types.values()){
            String itemId = impl.getAdapterID(itemStack);
            if(itemId != null) id = itemId;
        }
        return id;
    }

    public String getAdapterID(Block block){
        if(block == null) return null;
        String id = "mc:" + block.getType().toString().toLowerCase();
        for(Implementation impl : types.values()){
            String itemId = impl.getAdapterID(block);
            if(itemId != null) id = itemId;
        }
        return id;
    }
    public boolean existAdapterID(String id){
        for(Implementation impl : types.values()){
            if(impl.existItemAdapter(id)) return true;
        }
        return false;
    }

    public List<ItemStack> getItemsStack(List<String> i){
        List<ItemStack> itemStacks = new ArrayList<>();
        for(String str : i){
            itemStacks.add(getItemStack(str));
        }
        return itemStacks;
    }

    public boolean isItemsValid(List<String> i){
        for(String str : i){
            if(!isItemValid(str)) return false;
        }
        return true;
    }

    public boolean isItemValid(String itemID){

        String type = itemID.substring(0,itemID.indexOf(":"));
        itemID = itemID.substring(itemID.indexOf(":") + 1);


        return false;
    }

    public static HashMap<String, Implementation> getTypes() {
        return types;
    }
}