package com.tencao.nmd.data

import be.bluexin.saomclib.party.IParty
import com.tencao.nmd.api.IRarity
import com.tencao.nmd.api.ISpecialLootSettings
import com.tencao.nmd.drops.LootRegistry
import com.tencao.nmd.entities.EntityPartyItem
import io.netty.buffer.ByteBuf
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.PacketBuffer
import net.minecraft.world.World
import java.util.*
import java.util.function.Predicate
import kotlin.random.Random

/**
 * This LootObject is designed for server side as the tickTime can hold the full world total tick
 * @param entityItem The stack being rolled on
 * @param tickTime The time the loot expires
 * @param rollID The unique identifier for the roll
 * @param lootSetting The Loot Settings used
 * @param serverCache The cache associated with the drop
 */
data class ServerLootObject(val entityItem: SimpleEntityItem, val party: IParty, var tickTime: Long, val rollID: UUID, val lootSetting: ISpecialLootSettings, val serverCache: Any?){

    fun areConditionsMet(): Boolean{
        return lootSetting.areConditionsMet(serverCache)
    }

    fun handleLoot(){
        lootSetting.handleLoot(entityItem, party, serverCache)?.let {cache ->
            LootRegistry.updateServerCache(lootSetting, party, cache)
        }
        LootRegistry.lootdrops.remove(this)
    }

    fun processClientData(player: EntityPlayer, clientCache: Any?){
        lootSetting.processClientCache(player, clientCache, serverCache)
        if (areConditionsMet()) {
            handleLoot()
        }
    }
}

/**
 * This is a loot drop specifically for clients where tick time is expected to be below Int.MAX
 * @param stack The stack being rolled on
 * @param rollID The unique identifier for the roll
 * @param tickTime The time the loot expires
 * @param rarity The rarity of the item dropped
 * @param clientCache The cache for the loot type
 */
data class ClientLootObject(val stack: SimpleStack, val rollID: UUID, var tickTime: Int, val rarity: IRarity, val lootSetting: ISpecialLootSettings, var clientCache: Any?){

    // Cached render position, recalculated everytime an element is added or removed
    var x: Int = 0
    var y: Int = 0

    fun setXY(x: Int, y: Int){
        this.x = x
        this.y = y
    }
}

/**
 * The Roll Data for loot rolls
 * @param uuid The players unique id
 * @param roll The Loot Roll
 */
data class RollData(val uuid: UUID, var roll: Float = 0f): Predicate<EntityPlayer> {

    fun roll(type: Int){
        when (type){
            1 -> greedRoll()
            2 -> needRoll()
            else -> passRoll()
        }

    }

    private fun needRoll(){
        roll = Random.nextInt(1, 100).toFloat()
    }

    private fun greedRoll(){
        roll = Random.nextFloat()
    }

    private fun passRoll(){
        roll = -1f
    }

    override fun test(player: EntityPlayer): Boolean {
        return uuid == player.uniqueID
    }
}


/**
 * Simple stack data sent and stored on the client
 */
data class SimpleStack(val id: Int, var count: Int, var meta: Int, var nbt: NBTTagCompound): Predicate<ItemStack> {

    constructor(stack: ItemStack): this(Item.REGISTRY.getIDForObject(stack.item), stack.count, stack.itemDamage, stack.serializeNBT())

    constructor(item: Item, meta: Int): this(Item.REGISTRY.getIDForObject(item), 1, meta, NBTTagCompound())

    fun toStack(): ItemStack{
        return if (isEmpty()) ItemStack.EMPTY else ItemStack(getItem(), count, meta, nbt)
    }

    fun isEmpty(): Boolean{
        return count <= 0
    }

    fun getItem(): Item {
        return Item.REGISTRY.getObjectById(id)?: Items.AIR
    }

    override fun test(t: ItemStack): Boolean {
        return ItemStack.areItemStacksEqual(t, toStack())
    }

    fun toBytes(buf: ByteBuf) {
        buf.writeInt(id)
        buf.writeInt(count)
        buf.writeInt(meta)
        PacketBuffer(buf).writeCompoundTag(nbt)
    }

    companion object {
        fun fromBytes(buf: ByteBuf): SimpleStack {
            return SimpleStack(buf.readInt(), buf.readInt(), buf.readInt(), PacketBuffer(buf).readCompoundTag()!!)
        }
    }
}


data class SimpleEntityItem(val simpleStack: SimpleStack, val x: Double, val y: Double, val z: Double, val world: World) {

    constructor(itemStack: ItemStack, x: Double, y: Double, z:Double, world: World): this(SimpleStack(itemStack), x, y, z, world)

    constructor(entityItem: EntityItem): this(SimpleStack(entityItem.item.copy()), entityItem.posX, entityItem.posY, entityItem.posZ, entityItem.world)

    fun spawnEntityItem(){
        world.spawnEntity(EntityItem(world, x, y, z, toStack()))
    }

    /**
     * Used to drop the loot item while still assigning the loot to players
     */
    fun spawnEntityPartyItem(party: IParty){
        world.spawnEntity(EntityPartyItem(world, x, y, z, toStack(), party.members.asSequence().map { player -> player.uniqueID }.toHashSet()))
    }

    fun toStack(): ItemStack {
        return simpleStack.toStack()
    }

    fun shrink(){
        simpleStack.count--
    }

    fun count(): Int{
        return simpleStack.count
    }

    fun isEmpty(): Boolean{
        return count() > 0
    }

    /**
     * Returns the squared distance to the entity.
     */
    fun getDistanceSq(entityIn: Entity): Double {
        val d0 = this.x - entityIn.posX
        val d1 = this.y - entityIn.posY
        val d2 = this.z - entityIn.posZ
        return d0 * d0 + d1 * d1 + d2 * d2
    }
}