package com.tencao.nmd.entities

import be.bluexin.saomclib.party.PlayerInfo
import be.bluexin.saomclib.party.playerInfo
import com.tencao.nmd.LootSettingsEnum
import com.tencao.nmd.api.ILootSettings
import com.tencao.nmd.data.SimpleEntityItem
import com.tencao.nmd.util.EntityItemReflect
import com.tencao.nmd.util.LootHelper
import com.tencao.nmd.util.PartyHelper
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.stats.StatList
import net.minecraft.world.World
import java.util.*
/*
class EntityPartyItem: EntityItem {

    val owners: List<PlayerInfo>
    val hasRolled: Boolean
    val lootSetting: ILootSettings

    constructor(world: World, uuid: List<PlayerInfo>, hasRolled: Boolean): super(world){
        this.setPickupDelay(40)
        this.owners = uuid
        this.hasRolled = hasRolled
    }

    constructor(world: World, x: Double, y: Double, z: Double, uuid: List<PlayerInfo>, hasRolled: Boolean): super(world, x, y, z){
        this.setPickupDelay(40)
        this.owners = uuid
        this.hasRolled = hasRolled
    }

    constructor(world: World, x: Double, y: Double, z: Double, stack: ItemStack, uuid: List<PlayerInfo>, hasRolled: Boolean): super(world, x, y, z, stack){
        this.setPickupDelay(40)
        this.owners = uuid
        this.hasRolled = hasRolled
    }


    /**
     * Called by a player entity when they collide with an entity
     */
    override fun onCollideWithPlayer(entityIn: EntityPlayer) {
        if (!this.world.isRemote) {
            val pickupDelay = EntityItemReflect.getPickupDelay(this)
            if (pickupDelay > 0) return

            if (pickupDelay <= 0) {
                if (hasRolled){
                    addStackToPlayer(entityIn)
                }
                else if (owners.contains(entityIn.playerInfo())){
                    if (PartyHelper.isValidParty(entityIn)) {
                        LootHelper.handleLoot(SimpleEntityItem(this), owners)
                        this.setDead()
                    } else {
                        addStackToPlayer(entityIn)
                    }
                }
            }
        }
    }

    fun addStackToPlayer(entityIn: EntityPlayer){
        val itemstack = this.item
        val item = itemstack.item
        val i = itemstack.count
        val hook = net.minecraftforge.event.ForgeEventFactory.onItemPickup(this, entityIn)
        if (hook < 0) return
        val clone = itemstack.copy()
        if ((hook == 1 || i <= 0 || entityIn.inventory.addItemStackToInventory(itemstack) || clone.count > this.item.count)) {
            clone.count = clone.count - this.item.count
            net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerItemPickupEvent(entityIn, this, clone)

            if (itemstack.isEmpty) {
                entityIn.onItemPickup(this, i)
                this.setDead()
                itemstack.count = i
            }

            entityIn.addStat(StatList.getObjectsPickedUpStats(item)!!, i)
        }
    }
}*/