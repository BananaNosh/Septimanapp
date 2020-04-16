package com.nobodysapps.septimanapp

import android.content.Context
import com.nobodysapps.septimanapp.model.*
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class EatingHabitTest {

    @Mock
    private lateinit var mockContext: Context

    private val eatingHabit1 = EatingHabit(listOf("gluten", "milch"))

    private val vegetarian = Vegetarian(emptyList())

    private val vegan = Vegan(listOf("gluten"))

    @Test
    fun testInformation() {
        `when`(mockContext.getString(R.string.eating_habit_allergens))
            .thenReturn("Allergene")
        `when`(mockContext.getString(R.string.eating_habit_vegetarian))
            .thenReturn("Vegetarier")
        `when`(mockContext.getString(R.string.eating_habit_vegan))
            .thenReturn("Veganer")


        assertEquals("Allergene: gluten, milch", eatingHabit1.information(mockContext))
        assertEquals("Vegetarier", vegetarian.information(mockContext))
        assertEquals("Allergene: gluten - Veganer", vegan.information(mockContext))
    }

    @Test
    fun serializablePair() {
        val pair = eatingHabit1.toSerializablePair()
        val loaded = EatingHabit.fromSerializablePair(pair)
        assertEquals(eatingHabit1.javaClass, loaded.javaClass)
        assertEquals(eatingHabit1.allergens, loaded.allergens)
        val pair2 = vegetarian.toSerializablePair()
        val loaded2 = EatingHabit.fromSerializablePair(pair2)
        assertEquals(vegetarian.javaClass, loaded2.javaClass)
        assertEquals(vegetarian.allergens, loaded2.allergens)
        val pair3 = vegan.toSerializablePair()
        val loaded3 = EatingHabit.fromSerializablePair(pair3)
        assertEquals(vegan.javaClass, loaded3.javaClass)
        assertEquals(vegan.allergens, loaded3.allergens)
    }
}