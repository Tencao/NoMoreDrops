package com.tencao.nmd.util

import net.minecraft.client.gui.GuiScreen
import net.minecraft.entity.EntityLiving
import net.minecraft.world.storage.loot.LootPool
import net.minecraft.world.storage.loot.LootTable
import net.minecraftforge.fml.relauncher.ReflectionHelper
import java.lang.reflect.Field
import java.lang.reflect.Method

object ReflectHelper {

    val getLootTable: Method = ReflectionHelper.findMethod(EntityLiving::class.java, "getLootTable", "func_184647_J")
    val getPools: Field = ReflectionHelper.findField(LootTable::class.java, "field_186466_c", "pools")
    val getLootEntries: Field = ReflectionHelper.findField(LootPool::class.java, "field_186453_a", "lootEntries")
    val getPoolConditions: Field = ReflectionHelper.findField(LootPool::class.java, "field_186454_b", "poolConditions")
    val getTouchValue: Field = ReflectionHelper.findField(GuiScreen::class.java, "field_146298_h", "touchValue")
}