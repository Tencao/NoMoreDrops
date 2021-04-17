package com.tencao.nmd.data

import be.bluexin.saomclib.party.IPartyData
import be.bluexin.saomclib.party.PlayerInfo
import be.bluexin.saomclib.party.playerInfo
import com.teamwizardry.librarianlib.features.helpers.setNBTBoolean
import com.teamwizardry.librarianlib.features.helpers.setNBTInt
import com.teamwizardry.librarianlib.features.helpers.setNBTList
import com.tencao.nmd.DropRarityEnum
import com.tencao.nmd.api.IRarity
import com.tencao.nmd.registry.LootRegistry
import com.tencao.nmd.util.Constants
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*

data class SimpleEntityItem(val simpleStack: SimpleStack, val pos: BlockPos, val world: World, var rarity: IRarity = DropRarityEnum.COMMON) {

    constructor(itemStack: ItemStack, pos: BlockPos, world: World): this(SimpleStack(itemStack), pos, world)

    constructor(entityItem: EntityItem): this(SimpleStack(entityItem.item.copy()), BlockPos(entityItem), entityItem.world)

    /**
     * Used to spawn an item as a regular EntityItem, which will be ignored
     * by the auto loot system
     */
    fun spawnEntityItem(){
        val stack = toStack()
        stack.setNBTBoolean(Constants.ignoreData, true)
        world.spawnEntity(EntityItem(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), stack))
    }

    /**
     * Used to drop the loot item while still assigning the loot to players
     * @param hasRolled = If true, item has already been rolled.
     */
    fun spawnEntityPartyItem(party: IPartyData, hasRolled: Boolean){
        world.spawnEntity(setupData(party.getMembers().toList(), hasRolled))

        //world.spawnEntity(EntityPartyItem(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), toStack(), party.membersInfo.filter { it.player != null }.map { it.uuid }, hasRolled))
    }

    /**
     * Used to drop the loot item while still assigning the loot to players
     * @param hasRolled = If true, item has already been rolled.
     */
    fun spawnEntityPartyItem(playerInfo: PlayerInfo, hasRolled: Boolean){
        world.spawnEntity(setupData(listOf(playerInfo), hasRolled))

        //world.spawnEntity(EntityPartyItem(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), toStack(), sequenceOf(uuid), hasRolled))
    }

    /**
     * Used to drop the loot item while still assigning the loot to players
     * @param hasRolled = If true, item has already been rolled.
     */
    fun spawnEntityPartyItem(party: List<PlayerInfo>, hasRolled: Boolean){
        world.spawnEntity(setupData(party, hasRolled))
        //world.spawnEntity(EntityPartyItem(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), toStack(), party, hasRolled))
    }

    /**
     * Used to drop the loot item while still assigning the loot to players
     * @param hasRolled = If true, item has already been rolled.
     */
    fun spawnEntityPartyItem(party: Set<PlayerInfo>, hasRolled: Boolean){
        world.spawnEntity(setupData(party.toList(), hasRolled))
        //world.spawnEntity(EntityPartyItem(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), toStack(), party.asSequence(), hasRolled))
    }

    /**
     * Used to drop the loot item while still assigning the loot to players
     * @param hasRolled = If true, item has already been rolled.
     */
    fun spawnEntityPartyItem(player: EntityPlayer, hasRolled: Boolean){
        world.spawnEntity(setupData(listOf(player.playerInfo()), hasRolled))
        //world.spawnEntity(EntityPartyItem(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), toStack(), sequenceOf(player.uniqueID), hasRolled))
    }

    private fun setupData(party: List<PlayerInfo>, hasRolled: Boolean): EntityItem{
        val stack = toStack()
        if (!hasRolled) {
            val tagList = NBTTagList()
            party.forEach { player ->
                val tag = NBTTagCompound()
                tag.setString(Constants.uuid, player.uuidString)
                tagList.appendTag(tag)
            }
            stack.setNBTList(Constants.partyData, tagList)
            stack.setNBTInt(Constants.rarity, LootRegistry.registeredRarity.indexOf(rarity))
        }
        stack.setNBTBoolean(Constants.ignoreData, true)
        return EntityItem(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), stack)
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