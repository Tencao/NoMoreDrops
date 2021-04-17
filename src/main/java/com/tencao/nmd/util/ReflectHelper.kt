package com.tencao.nmd.util

import net.minecraft.client.gui.GuiScreen
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.Item
import net.minecraft.util.CombatEntry
import net.minecraft.util.CombatTracker
import net.minecraft.util.ResourceLocation
import net.minecraft.world.storage.loot.*
import net.minecraft.world.storage.loot.conditions.LootCondition
import net.minecraft.world.storage.loot.conditions.RandomChance
import net.minecraft.world.storage.loot.conditions.RandomChanceWithLooting
import net.minecraft.world.storage.loot.functions.LootFunction
import net.minecraft.world.storage.loot.functions.SetCount
import net.minecraft.world.storage.loot.functions.SetMetadata
import net.minecraftforge.fml.relauncher.ReflectionHelper
import java.lang.reflect.Field
import java.lang.reflect.Method

@Suppress("UNCHECKED_CAST")
object EntityLivingReflect {

    val getCombatEntries: Field = ReflectionHelper.findField(CombatTracker::class.java, "combatEntries", "field_94556_a")
    val getLastCombatTime: Field = ReflectionHelper.findField(CombatTracker::class.java, "lastDamageTime", "field_94555_c")

    init {
        getCombatEntries.isAccessible = true
        getLastCombatTime.isAccessible = true
    }
}

@Suppress("UNCHECKED_CAST")
fun CombatTracker.getCombatEntries() = EntityLivingReflect.getCombatEntries.get(this) as MutableList<CombatEntry>

@Suppress("UNCHECKED_CAST")
fun CombatTracker.getLastCombatTime() = EntityLivingReflect.getLastCombatTime.get(this) as Int

@Suppress("UNCHECKED_CAST")
fun CombatTracker.setLastCombatTime(value: Int) = EntityLivingReflect.getLastCombatTime.set(this, value)

@Suppress("UNCHECKED_CAST")
object LootTableReflect {

    private val getLootTable: Field = ReflectionHelper.findField(EntityLiving::class.java, "deathLootTable", "func_184647_J")
    private val getPools: Field = ReflectionHelper.findField(LootTable::class.java, "pools", "field_186466_c")
    private val getLootEntries: Field = ReflectionHelper.findField(LootPool::class.java, "lootEntries", "field_186453_a")
    private val getPoolConditions: Field = ReflectionHelper.findField(LootPool::class.java, "poolConditions", "field_186454_b")
    private val getFunctions: Field = ReflectionHelper.findField(LootEntryItem::class.java, "functions", "field_186369_b")
    private val getConditions: Field = ReflectionHelper.findField(LootEntry::class.java, "conditions", "field_186366_e")
    private val getTable: Field = ReflectionHelper.findField(LootEntryTable::class.java, "table", "field_186371_a")
    private val getItem: Field = ReflectionHelper.findField(LootEntryItem::class.java, "item", "field_186368_a")
    private val getMetaRange: Field = ReflectionHelper.findField(SetMetadata::class.java, "metaRange", "field_186573_b")
    private val getCountRange: Field = ReflectionHelper.findField(SetCount::class.java, "countRange", "field_186568_a")
    private val getChance: Field = ReflectionHelper.findField(RandomChance::class.java, "chance", "field_186630_a")
    private val getChanceWithLooting: Field = ReflectionHelper.findField(RandomChanceWithLooting::class.java, "chance", "field_186627_a")

    fun getLootTable(instance: EntityLivingBase): ResourceLocation { return getLootTable[instance] as ResourceLocation }

    fun getPools(instance: LootTable): List<LootPool> { return getPools[instance] as List<LootPool> }

    fun getLootEntries(instance: LootPool): List<LootEntry> { return getLootEntries[instance] as List<LootEntry> }

    fun getPoolConditions(instance: LootPool): List<LootCondition> { return getPoolConditions[instance] as List<LootCondition> }

    fun getFunctions(instance: LootEntryItem): Array<LootFunction> { return getFunctions[instance] as Array<LootFunction> }

    fun getConditions(instance: LootEntry): Array<LootCondition> { return getConditions[instance] as Array<LootCondition> }

    fun getTable(instance: LootEntryTable): ResourceLocation { return getTable[instance] as ResourceLocation }

    fun getItem(instance: LootEntryItem): Item{ return getItem[instance] as Item }

    fun getMetaRange(instance: SetMetadata): RandomValueRange { return getMetaRange[instance] as RandomValueRange }

    fun getCountRange(instance: SetCount): RandomValueRange { return getCountRange[instance] as RandomValueRange }

    fun getChance(instance: RandomChance): Float { return getChance[instance] as Float }

    fun getChance(instance: RandomChanceWithLooting): Float { return getChanceWithLooting[instance] as Float }
}

@Suppress("UNCHECKED_CAST")
object EntityItemReflect {

    private val getPickupDelay: Field = ReflectionHelper.findField(EntityItem::class.java, "pickupDelay", "field_145804_b")

    fun getPickupDelay(instance: EntityItem): Int { return getPickupDelay[instance] as Int }
}

@Suppress("UNCHECKED_CAST")
object GUIScreenReflect {

    private val getTouchValue: Field = ReflectionHelper.findField(GuiScreen::class.java, "touchValue", "field_146298_h")

    fun getTouchValue(instance: GuiScreen): Int { return getTouchValue[instance] as Int }
}
