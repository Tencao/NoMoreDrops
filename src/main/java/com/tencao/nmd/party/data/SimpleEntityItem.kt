package com.tencao.nmd.party.data

import be.bluexin.saomclib.party.IParty
import com.tencao.nmd.core.data.SimpleStack
import com.tencao.nmd.party.entities.EntityPartyItem
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

data class SimpleEntityItem(val simpleStack: SimpleStack, val pos: BlockPos, val world: World) {

    constructor(itemStack: ItemStack, pos: BlockPos, world: World): this(SimpleStack(itemStack), pos, world)

    constructor(entityItem: EntityItem): this(SimpleStack(entityItem.item.copy()), BlockPos(entityItem), entityItem.world)

    fun spawnEntityItem(){
        world.spawnEntity(EntityItem(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), toStack()))
    }

    /**
     * Used to drop the loot item while still assigning the loot to players
     * @param hasRolled = If true, item has already been rolled.
     */
    fun spawnEntityPartyItem(party: IParty, hasRolled: Boolean){
        world.spawnEntity(EntityPartyItem(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), toStack(), party.members.asSequence().map { player -> player.uniqueID }.toHashSet(), hasRolled))
    }

    fun toStack(): ItemStack {
        return simpleStack.stack
    }

    fun shrink(){
        simpleStack.reduce()
    }

    fun count(): Int{
        return simpleStack.count
    }

    fun isEmpty(): Boolean{
        return simpleStack.isEmpty()
    }

    /**
     * Returns the squared distance to the entity.
     */
    fun getDistanceSq(entityIn: Entity): Double {
        val d0 = this.pos.x - entityIn.posX
        val d1 = this.pos.y - entityIn.posY
        val d2 = this.pos.z - entityIn.posZ
        return d0 * d0 + d1 * d1 + d2 * d2
    }
}