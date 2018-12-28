package com.tencao.nmd.entities

import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.stats.StatList
import net.minecraft.world.World
import java.util.*

class EntityPartyItem: EntityItem {

    val owners: Set<UUID>

    constructor(world: World, uuid: Set<UUID>): super(world){
        owners = uuid
    }

    constructor(world: World, x: Double, y: Double, z: Double, uuid: Set<UUID>): super(world, x, y, z){
        owners = uuid
    }

    constructor(world: World, x: Double, y: Double, z: Double, stack: ItemStack, uuid: Set<UUID>): super(world, x, y, z, stack){
        owners = uuid
    }


    /**
     * Called by a player entity when they collide with an entity
     */
    override fun onCollideWithPlayer(entityIn: EntityPlayer) {
        if (!this.world.isRemote) {
            if (this.pickupDelay > 0) return
            val itemstack = this.item
            val item = itemstack.item
            val i = itemstack.count

            val hook = net.minecraftforge.event.ForgeEventFactory.onItemPickup(this, entityIn)
            if (hook < 0) return
            val clone = itemstack.copy()

            if (this.pickupDelay <= 0 && owners.contains(entityIn.uniqueID) && (lifespan - this.age <= 200 || this.owner == entityIn.name) && (hook == 1 || i <= 0 || entityIn.inventory.addItemStackToInventory(itemstack) || clone.count > this.item.count)) {
                clone.count = clone.count - this.item.count
                net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerItemPickupEvent(entityIn, this, clone)

                if (itemstack.isEmpty) {
                    entityIn.onItemPickup(this, i)
                    this.setDead()
                    itemstack.count = i
                }

                entityIn.addStat(StatList.getObjectsPickedUpStats(item), i)
            }
        }
    }

}