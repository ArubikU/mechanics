package dev.wuason.nms.nms_1_20_R2;

import dev.wuason.nms.wrappers.DataInfo;
import dev.wuason.nms.wrappers.VersionWrapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R2.CraftServer;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R2.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftInventoryView;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;


public class VersionWrapper_1_20_R2 implements VersionWrapper {
    public String getVersion(){
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        return craftServer.getServer().getServerVersion();
    }
    @Override
    public VersionWrapper.AnvilInventoryCustom createAnvilInventory(Player player, String title, InventoryHolder holder) {
        return new AnvilInventoryCustom(player, title, holder);
    }

    public class AnvilInventoryCustom implements VersionWrapper.AnvilInventoryCustom {
        private final AnvilInventory inventory;
        private final AnvilMenu anvilMenu;
        private final ServerPlayer serverPlayer;
        private final InventoryHolder holder;
        private final InventoryView inventoryView;

        public AnvilInventoryCustom(Player player, String title, InventoryHolder holder){
            serverPlayer = ((CraftPlayer)player).getHandle();
            //DEF VARS
            this.holder = holder;

            //CREATE INVENTORY
            int invId = serverPlayer.nextContainerCounter();
            AnvilMenu anvilMenu = new AnvilMenu(invId, serverPlayer.getInventory()){

                private CraftInventoryView bukkitEntity;

                @Override
                public CraftInventoryView getBukkitView() {
                    if (this.bukkitEntity != null) {
                        return this.bukkitEntity;
                    }

                    CraftInventory inventory = new CraftInventoryAnvil(access.getLocation(), this.inputSlots, this.resultSlots, this){
                        @Override
                        public InventoryHolder getHolder() {
                            return holder;
                        }

                    };
                    this.bukkitEntity = new CraftInventoryView(this.player.getBukkitEntity(), inventory, this);
                    return this.bukkitEntity;
                }

            };

            anvilMenu.setTitle(Component.literal(title));

            this.anvilMenu = anvilMenu;
            this.inventory = (AnvilInventory) anvilMenu.getBukkitView().getTopInventory();
            this.inventoryView = anvilMenu.getBukkitView();



        }

        @Override
        public void open(String title){
            ClientboundOpenScreenPacket packet = new ClientboundOpenScreenPacket(anvilMenu.containerId, MenuType.ANVIL,Component.literal(title));
            serverPlayer.connection.send(packet);
            serverPlayer.containerMenu = anvilMenu;
            serverPlayer.initMenu(anvilMenu);
        }
        @Override
        public void open(){
            ClientboundOpenScreenPacket packet = new ClientboundOpenScreenPacket(anvilMenu.containerId, MenuType.ANVIL,anvilMenu.getTitle());
            serverPlayer.connection.send(packet);
            serverPlayer.containerMenu = anvilMenu;
            serverPlayer.initMenu(anvilMenu);
        }
        @Override
        public void open(Player player){
            ServerPlayer srvPlayer = ((CraftPlayer)player).getHandle();
            if(player.equals(anvilMenu.getBukkitView().getPlayer())) open();
            int invId = srvPlayer.nextContainerCounter();
            AnvilMenu anvilMenu = new AnvilMenu(invId, serverPlayer.getInventory()){
                private CraftInventoryView bukkitEntity;
                @Override
                public CraftInventoryView getBukkitView() {
                    if (this.bukkitEntity != null) {
                        return this.bukkitEntity;
                    }
                    this.bukkitEntity = new CraftInventoryView(this.player.getBukkitEntity(), inventory, this);
                    return this.bukkitEntity;
                }
            };
            anvilMenu.setTitle(this.anvilMenu.getTitle());
            ClientboundOpenScreenPacket packet = new ClientboundOpenScreenPacket(invId, MenuType.ANVIL, anvilMenu.getTitle());
            srvPlayer.connection.send(packet);
            srvPlayer.containerMenu = anvilMenu;
            srvPlayer.initMenu(anvilMenu);
        }

        @Override
        public void setCheckReachable(boolean r){
            anvilMenu.checkReachable = r;
        }
        @Override
        public Object getAnvilMenuNMS(){
            return anvilMenu;
        }
        @Override
        public InventoryHolder getHolder() {
            return holder;
        }
        @Override
        public @NotNull AnvilInventory getInventory() {
            return inventory;
        }

        @Override
        public InventoryView getInventoryView() {
            return inventoryView;
        }

        @Override
        public void setMaxRepairCost(int cost){
            anvilMenu.maximumRepairCost = cost;
        }

        @Override
        public void setRepairItemCountCost(int cost){
            anvilMenu.repairItemCountCost = cost;
        }

        @Override
        public void setRenameText(String renameText){
            anvilMenu.itemName = renameText;
        }

        @Override
        public void setTitle(String title){
            anvilMenu.setTitle(Component.nullToEmpty(title));
        }
    }
    @Override
    public VersionWrapper.AnvilGui createAnvilGui(Player player, String title, ItemStack repairItem){
        return new AnvilGui(player, title, repairItem);
    }

    public class AnvilGui implements VersionWrapper.AnvilGui {
        private int invId = 0;
        private Player player;
        private Inventory inventory;
        private ServerPlayer serverPlayer;
        private AnvilMenu anvilMenu;
        private String title;
        private boolean blockClose = false;
        private boolean open = false;
        private ItemStack repairItem = null;

        public AnvilGui(Player player, String title, ItemStack repairItem){
            serverPlayer = ((CraftPlayer)player).getHandle();
            this.player = player;
            this.title = title;
            this.repairItem = repairItem;
        }
        @Override
        public AnvilInventory getAnvilInventory(){
            return (AnvilInventory) inventory;
        }
        @Override
        public Object getAnvilMenuNMS(){
            return anvilMenu;
        }
        @Override
        public void callCloseInventoryEvent(){
            CraftEventFactory.handleInventoryCloseEvent(serverPlayer);
        }
        @Override
        public void setDefMenu(){
            serverPlayer.containerMenu = serverPlayer.inventoryMenu;
        }
        @Override
        public void open(){
            open = true;

            invId = serverPlayer.nextContainerCounter();

            ContainerLevelAccess containerLevelAccess = ContainerLevelAccess.create(serverPlayer.getCommandSenderWorld(),new BlockPos(0,0,0));
            AnvilMenu anvilMenu = new AnvilMenu(invId, serverPlayer.getInventory(), containerLevelAccess);

            this.anvilMenu = anvilMenu;

            anvilMenu.setTitle(Component.literal(title));
            anvilMenu.repairItemCountCost = 0;
            anvilMenu.maximumRepairCost = 0;
            anvilMenu.checkReachable = false;

            inventory = anvilMenu.getBukkitView().getTopInventory();

            if(repairItem != null) inventory.setItem(0, repairItem);
            if(repairItem != null) inventory.setItem(1, repairItem);

            ClientboundOpenScreenPacket packet = new ClientboundOpenScreenPacket(invId, MenuType.ANVIL,Component.literal(title));
            serverPlayer.connection.send(packet);
            serverPlayer.containerMenu = anvilMenu;
            serverPlayer.initMenu(anvilMenu);
        }
        @Override
        public void close(){
            open = false;
            CraftEventFactory.handleInventoryCloseEvent(serverPlayer);
            serverPlayer.containerMenu = serverPlayer.inventoryMenu;
            ClientboundContainerClosePacket packet = new ClientboundContainerClosePacket(invId);
            serverPlayer.connection.send(packet);
        }

        @Override
        public int getInvId() {
            return invId;
        }
        @Override
        public Player getPlayer() {
            return player;
        }
        @Override
        public Inventory getInventory() {
            return inventory;
        }
        @Override
        public String getTitle() {
            return title;
        }
        @Override
        public void setTitle(String title) {
            this.title = title;
        }
        @Override
        public boolean isBlockClose() {
            return blockClose;
        }
        @Override
        public void setBlockClose(boolean blockClose) {
            this.blockClose = blockClose;
        }
        @Override
        public boolean isOpen() {
            return open;
        }
        @Override
        public void setOpen(boolean open) {
            this.open = open;
        }
        @Override
        public ItemStack getRepairItem() {
            return repairItem;
        }
        @Override
        public void setRepairItem(ItemStack repairItem) {
            this.repairItem = repairItem;
        }
    }
    @Override
    public void sendToast(Player player, ItemStack icon, String titleJson, ToastType toastType){
        ServerPlayer serverPlayer = ((CraftPlayer)player).getHandle();
        Optional<DisplayInfo> displayInfo = Optional.of(new DisplayInfo(net.minecraft.world.item.ItemStack.fromBukkitCopy(icon),Component.Serializer.fromJson(titleJson),Component.literal("."),null, FrameType.valueOf(toastType.toString()),true,false,true));
        AdvancementRewards advancementRewards = AdvancementRewards.EMPTY;
        Optional<ResourceLocation> id = Optional.of(new ResourceLocation("custom","custom"));
        Criterion<ImpossibleTrigger.TriggerInstance> impossibleTrigger = new Criterion<>(new ImpossibleTrigger(),new ImpossibleTrigger.TriggerInstance());
        HashMap<String, Criterion<?>> criteria = new HashMap<>(){{put("impossible", impossibleTrigger);}};
        String[][] requirements = {{"impossible"}};
        AdvancementRequirements advancementRequirements = new AdvancementRequirements( requirements );
        Advancement advancement = new Advancement(Optional.empty(),displayInfo,advancementRewards,criteria,advancementRequirements,false);
        Map<ResourceLocation, AdvancementProgress> advancementsToGrant = new HashMap<>();
        AdvancementProgress advancementProgress = new AdvancementProgress();
        advancementProgress.update(advancementRequirements);
        advancementProgress.getCriterion("impossible").grant();
        advancementsToGrant.put(id.get(), advancementProgress);
        ClientboundUpdateAdvancementsPacket packet = new ClientboundUpdateAdvancementsPacket(false, new ArrayList<>(){{add(new AdvancementHolder(id.get(),advancement));}},new HashSet<>(),advancementsToGrant);
        serverPlayer.connection.send(packet);
        ClientboundUpdateAdvancementsPacket packet2 = new ClientboundUpdateAdvancementsPacket(false, new ArrayList<>(),new HashSet<>(){{add(id.get());}},new HashMap<>());
        serverPlayer.connection.send(packet2);
    }

    @Override
    public void updateCurrentInventoryTitle(String jsonTitle, Player player){
        ServerPlayer serverPlayer = ((CraftPlayer)player).getHandle();
        MenuType<?> menuType = serverPlayer.containerMenu.getType();
        int invId = serverPlayer.containerMenu.containerId;
        ClientboundOpenScreenPacket packetOpen = new ClientboundOpenScreenPacket(invId,menuType,Component.Serializer.fromJson(jsonTitle));
        serverPlayer.connection.send(packetOpen);
        serverPlayer.initMenu(serverPlayer.containerMenu);
    }

    @Override
    public void openSing(Player player, String[] defLines, Consumer<String[]> onSend){
        if(defLines.length != 4) throw new IllegalArgumentException("The length of the lines must be 4");
        ServerPlayer serverPlayer = (ServerPlayer)((CraftPlayer)player).getHandle();
        Location loc = new Location(player.getLocation().getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockY() - 7, player.getLocation().getBlockZ());
        BlockPos blockPos = new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        SignBlockEntity signBlock = new SignBlockEntity(blockPos, null);
        SignText signText = new SignText();
        for(int i = 0; i < defLines.length; i++){
            if(defLines[i] == null) continue;
            signText.setMessage(i,Component.literal(defLines[i]));
        }
        signBlock.setText(signText, true);
        player.sendBlockChange(loc, Material.OAK_SIGN.createBlockData());
        serverPlayer.connection.send(signBlock.getUpdatePacket());
        serverPlayer.connection.send(new ClientboundOpenSignEditorPacket(blockPos, true));
        ChannelPipeline pipeline = serverPlayer.connection.connection.channel.pipeline();
        pipeline.addBefore("packet_handler", DataInfo.NAMESPACE_SIGN, new ChannelInboundHandlerAdapter()
                {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        if(msg instanceof ServerboundSignUpdatePacket){
                            ServerboundSignUpdatePacket packet = (ServerboundSignUpdatePacket) msg;
                            onSend.accept(packet.getLines());
                            player.sendBlockChange(loc, loc.getBlock().getBlockData());
                            pipeline.remove(DataInfo.NAMESPACE_SIGN);
                        }
                        super.channelRead(ctx, msg);
                    }
                }
        );
    }
    @Override
    public void openSing(Player player, Consumer<String[]> onSend){
        openSing(player, new String[4], onSend);
    }
}
