package me.loloed.bot.api.util.fake;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class FakeServerPlayer extends LivingEntity {
    private final FakePlayer serverPlayer;

    protected FakeServerPlayer(FakePlayer serverPlayer) {
        super(EntityType.ZOMBIE, serverPlayer.serverLevel());
        this.serverPlayer = serverPlayer;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return serverPlayer.getArmorSlots();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
        return serverPlayer.getItemBySlot(equipmentSlot);
    }

    @Override
    public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        serverPlayer.setItemSlot(equipmentSlot, itemStack);
    }

    @Override
    public HumanoidArm getMainArm() {
        return serverPlayer.getMainArm();
    }

    public void update() {
        setPose(serverPlayer.getPose());
        setPos(serverPlayer.position());
    }
}
