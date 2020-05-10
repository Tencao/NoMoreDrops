package com.tencao.nmd.registry

import com.tencao.nmd.DropRarityEnum
import com.tencao.nmd.NMDCore
import com.tencao.nmd.api.IRarity
import com.tencao.nmd.data.SimpleStack
import com.tencao.nmd.util.CustomRarity
import com.tencao.nmd.util.LootTableReflect
import com.tencao.nmd.util.ModHelper
import net.minecraft.entity.EntityList
import net.minecraft.entity.EntityLiving
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.math.MathHelper
import net.minecraft.util.text.TextComponentString
import net.minecraft.world.World
import net.minecraft.world.storage.loot.*
import net.minecraft.world.storage.loot.conditions.LootCondition
import net.minecraft.world.storage.loot.conditions.RandomChance
import net.minecraft.world.storage.loot.conditions.RandomChanceWithLooting
import net.minecraft.world.storage.loot.functions.LootFunction
import net.minecraft.world.storage.loot.functions.SetCount
import net.minecraft.world.storage.loot.functions.SetMetadata
import java.io.File
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.math.roundToInt

object LootTableMapper {

    private val lootDropCache: HashSet<LootCacheEntry> = hashSetOf()
    lateinit var manager: LootTableManager

    fun generateCache(world: World) {
        val saveHandler = world.saveHandler
        manager = LootTableManager(File(File(saveHandler.worldDirectory, "data"), "loot_tables"))
        EntityList.getEntityNameList().forEach { resource ->
            val mob = EntityList.newEntity(EntityList.getClass(resource), world)
            try {
                if (mob is EntityLiving) {
                    generateMobChances(manager.getLootTableFromLocation(LootTableReflect.getLootTable(mob)))
                }
                NMDCore.LOGGER.debug("Loot table for $resource cached")
            } catch (e: Exception){
                NMDCore.LOGGER.debug("No loot table found for $resource")
                return@forEach
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun generateMobChances(lootTables: LootTable?) {
        if (lootTables == null) return

        val lootPools: List<LootPool> = LootTableReflect.getPools(lootTables)

        lootPools.forEach { pool ->
            val lootEntries = LootTableReflect.getLootEntries(pool)
            val totalWeight = lootEntries.asSequence().sumBy { entry -> entry.getEffectiveWeight(0f) }.toFloat()
            val poolConditions = LootTableReflect.getPoolConditions(pool)
            lootEntries.asSequence()
                    .filter{ entry -> entry is LootEntryItem }.map{ entry -> entry as LootEntryItem }
                    .map{ entry -> LootCacheEntry(LootTableReflect.getItem(entry), 0, entry.getEffectiveWeight(0f) / totalWeight, LootTableReflect.getConditions(entry), LootTableReflect.getFunctions(entry)) }
                    .map{ drop -> drop.addLootConditions(poolConditions) }
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
            lootEntries.asSequence()
                    .filter{ entry -> entry is LootEntryTable }.map{ entry -> entry as LootEntryTable }
                    .forEach{ entry -> generateMobChances(manager.getLootTableFromLocation(LootTableReflect.getTable(entry))) }
        }
    }

    fun getRarity(stack: ItemStack): IRarity{
        if (stack.getSubCompound("display") != null){
            val hasEffect = stack.isItemEnchanted
            return CustomRarity(TextComponentString(stack.displayName).style.color, hasEffect)
        }
        return lootDropCache.firstOrNull { it.test(stack) }?.rarity?: DropRarityEnum.UNKNOWN
    }

    fun setRarity(stack: ItemStack, rarity: IRarity){
        lootDropCache.add(LootCacheEntry(SimpleStack(stack), rarity))
    }
}

object LootConditionHelper {
    fun applyCondition(condition: LootCondition, lootCacheEntry: LootCacheEntry) {
        if (condition is RandomChance) {
            lootCacheEntry.chance = LootTableReflect.getChance(condition)
        } else if (condition is RandomChanceWithLooting) {
            lootCacheEntry.chance = LootTableReflect.getChance(condition)
        }
    }

    fun applyFunction(lootFunction: LootFunction, lootCacheEntry: LootCacheEntry) {
        if (lootFunction is SetCount) {
            val countRange = LootTableReflect.getCountRange(lootFunction)
            lootCacheEntry.min = MathHelper.floor(countRange.min)
            if (lootCacheEntry.min < 0) lootCacheEntry.min = 0
            lootCacheEntry.max = MathHelper.floor(countRange.max)
        } else if (lootFunction is SetMetadata)
            lootCacheEntry.stack.meta = (MathHelper.floor(LootTableReflect.getMetaRange(lootFunction).min))
    }
}

class LootCacheEntry(val stack: SimpleStack, var rarity: IRarity?): Predicate<ItemStack>, Comparable<LootCacheEntry>{
    var min = 1
    var max = 1
    var chance: Float = 0f

    constructor(item: Item, metadata: Int, chance: Float, conditions: Array<LootCondition>, functions: Array<LootFunction>):this(SimpleStack(item, metadata), DropRarityEnum.UNKNOWN){
        this.chance = chance
        conditions.forEach { LootConditionHelper.applyCondition(it, this) }
        functions.forEach { LootConditionHelper.applyFunction(it, this) }
        setRarity()
    }


    fun toStack(): ItemStack {
        return stack.stack
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

    fun setRarity() {
        val chance = getDropChance()
        rarity = when {
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
            (other.getDropChance() - getDropChance()).roundToInt()
        else return -1
    }
}