package com.tencao.nmd.party.entities

import com.tencao.nmd.core.util.EntityItemReflect
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.stats.StatList
import net.minecraft.world.World
import java.util.*

class EntityPartyItem: EntityItem {

    val owners: Set<UUID>
    val hasRolled: Boolean

    constructor(world: World, uuid: Set<UUID>, hasRolled: Boolean): super(world){
        this.setPickupDelay(40)
        this.owners = uuid
        this.hasRolled = hasRolled
    }

    constructor(world: World, x: Double, y: Double, z: Double, uuid: Set<UUID>, hasRolled: Boolean): super(world, x, y, z){
        this.setPickupDelay(40)
        this.owners = uuid
        this.hasRolled = hasRolled
    }

    constructor(world: World, x: Double, y: Double, z: Double, stack: ItemStack, uuid: Set<UUID>, hasRolled: Boolean): super(world, x, y, z, stack){
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
            val itemstack = this.item
            val item = itemstack.item
            val i = itemstack.count

            val hook = net.minecraftforge.event.ForgeEventFactory.onItemPickup(this, entityIn)
            if (hook < 0) return
            val clone = itemstack.copy()

            if (pickupDelay <= 0 && owners.contains(entityIn.uniqueID) && (hook == 1 || i <= 0 || entityIn.inventory.addItemStackToInventory(itemstack) || clone.count > this.item.count)) {
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
    }

}