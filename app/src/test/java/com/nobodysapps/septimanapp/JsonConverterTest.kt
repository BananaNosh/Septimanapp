package com.nobodysapps.septimanapp

import com.alamkanak.weekview.WeekViewEvent
import com.nobodysapps.septimanapp.dependencyInjection.TestComponent
import com.nobodysapps.septimanapp.model.Horarium
import com.nobodysapps.septimanapp.model.storage.JsonConverter
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import javax.inject.Inject


class JsonConverterTest {
    //    @Inject lateinit
    val jsonConverter: JsonConverter = JsonConverter()

    private val events: List<WeekViewEvent> = listOf(
        WeekViewEvent("id1", "ev1", 2019, 8, 15, 15, 15, 2019, 8, 15, 15, 45),
        WeekViewEvent("id2", "ev2", 2019, 8, 15, 15, 45, 2019, 8, 15, 16, 45),
        WeekViewEvent("id3", "ev3", 2019, 8, 16, 15, 15, 2019, 8, 16, 15, 45),
        WeekViewEvent("id4", "ev4", 2019, 8, 16, 15, 55, 2019, 8, 16, 17, 15)
    )

    @Test
    fun testSave() {
        val horarium = Horarium(events)
        val json = jsonConverter.toJson(horarium)
        print(json)
        assertEquals(
            "{\"events\":[{\"mId\":\"id1\",\"mStartTime\":{\"year\":2019,\"month\":7,\"dayOfMonth\":15,\"hourOfDay\":15,\"minute\":15},\"mEndTime\":{\"year\":2019,\"month\":7,\"dayOfMonth\":15,\"hourOfDay\":15,\"minute\":45},\"mName\":\"ev1\",\"mColor\":0,\"mAllDay\":false},{\"mId\":\"id2\",\"mStartTime\":{\"year\":2019,\"month\":7,\"dayOfMonth\":15,\"hourOfDay\":15,\"minute\":45},\"mEndTime\":{\"year\":2019,\"month\":7,\"dayOfMonth\":15,\"hourOfDay\":16,\"minute\":45},\"mName\":\"ev2\",\"mColor\":0,\"mAllDay\":false},{\"mId\":\"id3\",\"mStartTime\":{\"year\":2019,\"month\":7,\"dayOfMonth\":16,\"hourOfDay\":15,\"minute\":15},\"mEndTime\":{\"year\":2019,\"month\":7,\"dayOfMonth\":16,\"hourOfDay\":15,\"minute\":45},\"mName\":\"ev3\",\"mColor\":0,\"mAllDay\":false},{\"mId\":\"id4\",\"mStartTime\":{\"year\":2019,\"month\":7,\"dayOfMonth\":16,\"hourOfDay\":15,\"minute\":55},\"mEndTime\":{\"year\":2019,\"month\":7,\"dayOfMonth\":16,\"hourOfDay\":17,\"minute\":15},\"mName\":\"ev4\",\"mColor\":0,\"mAllDay\":false}]}",
            json
        )
    }

    @Test
    fun testLoad() {
        val horarium = Horarium(events)
        var json = jsonConverter.toJson(horarium)
        var loaded: Horarium = jsonConverter.fromJson(json, Horarium::class.java)
        assertEquals(horarium, loaded)

        json = "{\"events\":[{\"mId\":\"id1\",\"mStartTime\":{\"year\":2019,\"month\":7,\"dayOfMonth\":15,\"hourOfDay\":15,\"minute\":15},\"mEndTime\":{\"year\":2019,\"month\":7,\"dayOfMonth\":15,\"hourOfDay\":15,\"minute\":45},\"mName\":\"ev1\",\"mColor\":0,\"mAllDay\":false}"
        loaded = jsonConverter.fromJson(json, Horarium::class.java)
        assertEquals(horarium, loaded)
    }
}
