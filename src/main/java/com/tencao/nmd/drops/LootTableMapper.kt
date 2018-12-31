package com.tencao.nmd.drops

import com.tencao.nmd.NMDCore
import com.tencao.nmd.api.DropRarityEnum
import com.tencao.nmd.api.IRarity
import com.tencao.nmd.data.SimpleStack
import net.minecraft.entity.EntityList
import net.minecraft.entity.EntityLiving
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import net.minecraft.world.storage.loot.*
import net.minecraft.world.storage.loot.conditions.LootCondition
import net.minecraft.world.storage.loot.conditions.RandomChance
import net.minecraft.world.storage.loot.conditions.RandomChanceWithLooting
import net.minecraft.world.storage.loot.functions.LootFunction
import net.minecraft.world.storage.loot.functions.SetCount
import net.minecraft.world.storage.loot.functions.SetMetadata
import net.minecraftforge.fml.relauncher.ReflectionHelper
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.function.Consumer
import java.util.function.Predicate

object LootTableMapper {

    private val pools = ReflectionHelper.findField(LootTable::class.java, "field_186365_d", "pools")
    private val lootEntires: Field = ReflectionHelper.findField(LootPool::class.java, "field_186453_a", "lootEntries")
    private val poolConditions: Field = ReflectionHelper.findField(LootPool::class.java, "field_186454_b", "poolConditions")
    private val getLootTable: Method = ReflectionHelper.findMethod(EntityLiving::class.java, "getLootTable", "func_184647_J")
    private val lootDropCache: HashSet<LootCacheEntry> = hashSetOf()
    lateinit var manager: LootTableManager

    fun generateCache(world: World) {
        val saveHandler = world.saveHandler
        manager = LootTableManager(File(File(saveHandler.worldDirectory, "data"), "loot_tables"))
        EntityList.getEntityNameList().forEach { resource ->
            val mob = EntityList.newEntity(EntityList.getClass(resource), world)
            try {
                val lootTable: ResourceLocation = getLootTable.invoke(mob) as ResourceLocation
                if (mob is EntityLiving)
                    generateMobChances(manager.getLootTableFromLocation(lootTable))
                NMDCore.LOGGER.info("Loot table for $resource cached")
            } catch (e: Exception){
                NMDCore.LOGGER.info("No loot table found for $resource")
                return@forEach
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun generateMobChances(lootTables: LootTable?) {
        if (lootTables == null) return

        val lootPools: List<LootPool> = pools.get(lootTables) as List<LootPool>

        lootPools.forEach { pool ->
            val totalWeight = (lootEntires.get(pool) as List<LootEntry>).asSequence().sumBy { entry -> entry.getEffectiveWeight(0f) }.toFloat()
            val poolConditions = poolConditions.get(pool)
            (lootEntires.get(pool) as List<LootEntry>).asSequence()
                    .filter{ entry -> entry is LootEntryItem }.map{ entry -> entry as LootEntryItem }
                    .map{ entry -> LootCacheEntry(entry.item, 0, entry.getEffectiveWeight(0f) / totalWeight, entry.conditions, entry.functions) }
                    .map{ drop -> drop.addLootConditions(poolConditions as List<LootCondition>) }
                    .forEach{ drop ->
                        val search = lootDropCache.asSequence().firstOrNull { it.test(drop.toStack()) }
                        if (search != null) {
                            if (search > drop) {
                                lootDropCache.remove(search)
                                lootDropCache.add(drop)
                            }
                        }else {
                            lootDropCache.add(drop)
                        }
                    }
            (lootEntires.get(pool) as List<LootEntry>).asSequence()
                    .filter{ entry -> entry is LootEntryTable }.map{ entry -> entry as LootEntryTable }
                    .forEach{ entry -> generateMobChances(manager.getLootTableFromLocation(entry.table)) }
        }
    }

    fun getRarity(stack: ItemStack): IRarity{
        return lootDropCache.firstOrNull { it.test(stack) }?.getRarity()?: DropRarityEnum.UNKNOWN
    }
}

object LootConditionHelper {
    fun applyCondition(condition: LootCondition, lootCacheEntry: LootCacheEntry) {
        if (condition is RandomChance) {
            lootCacheEntry.chance = condition.chance
        } else if (condition is RandomChanceWithLooting) {
            lootCacheEntry.chance = condition.chance
        }
    }

    fun applyFunction(lootFunction: LootFunction, lootCacheEntry: LootCacheEntry) {
        if (lootFunction is SetCount) {
            lootCacheEntry.min = MathHelper.floor(lootFunction.countRange.min)
            if (lootCacheEntry.min < 0) lootCacheEntry.min = 0
            lootCacheEntry.max = MathHelper.floor(lootFunction.countRange.max)
        } else if (lootFunction is SetMetadata)
            lootCacheEntry.stack.meta = (MathHelper.floor(lootFunction.metaRange.min))
    }
}

class LootCacheEntry(val stack: SimpleStack, var chance: Float): Predicate<ItemStack>, Comparable<LootCacheEntry>{
    var min = 1
    var max = 1

    constructor(item: Item, metadata: Int, chance: Float, conditions: Array<LootCondition>, functions: Array<LootFunction>):this(SimpleStack(item, metadata), chance){
        conditions.forEach { LootConditionHelper.applyCondition(it, this) }
        functions.forEach { LootConditionHelper.applyFunction(it, this) }
    }


    fun toStack(): ItemStack {
        return stack.toStack()
    }

    fun addLootConditions(lootFunctions: Collection<LootCondition>): LootCacheEntry {
        lootFunctions.forEach(Consumer<LootCondition> { this.addLootCondition(it) })
        return this
    }

    fun addLootCondition(condition: LootCondition): LootCacheEntry {
        LootConditionHelper.applyCondition(condition, this)
        return this
    }

    fun getDropChance(): Float {
        if (min == 0) return this.chance * (max / (max + 1).toFloat()) * 100
        return this.chance * 100
    }

    fun getRarity(): IRarity {
        val chance = getDropChance()
        return when {
            chance <= 5 -> DropRarityEnum.EPIC
            chance <= 20 -> DropRarityEnum.RARE
            chance <= 50 -> DropRarityEnum.UNCOMMON
            else -> DropRarityEnum.COMMON
        }
    }

    fun getFormattedChance(): String{
        return if (chance < 1f) " (" + formatChance() + "%)" else ""
    }

    private fun formatChance(): String {
        val chance = this.chance * 100
        return if (chance < 10) String.format("%.1f", chance) else String.format("%2d", chance.toInt())
    }

    override fun test(t: ItemStack): Boolean {
        return ItemStack.areItemsEqual(t, toStack())
    }

    override fun compareTo(other: LootCacheEntry): Int {
        return if (ItemStack.areItemsEqual(toStack(), other.toStack()))
            Math.round(other.getDropChance() - getDropChance())
        else return -1
    }
}