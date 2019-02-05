package com.deguffroy.adrien.projetphoto

import com.deguffroy.adrien.projetphoto.Utils.toLocaleStringDate
import com.deguffroy.adrien.projetphoto.Utils.toLocaleStringDateMedium
import com.google.firebase.Timestamp
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * Created by Adrien Deguffroy on 28/01/2019.
 */
class DateExtensionUnitTest {
    private lateinit var timestamp:Timestamp

    @Before
    fun setUp() {
        timestamp = Timestamp(1548680400,0)
    }

    @Test
    fun timestampToLocaleStringDate(){
        val date = timestamp.toDate()

        assertEquals("28/01/19" , date.toLocaleStringDate(Locale.FRANCE))
    }

    @Test
    fun timestampToLocaleStringDateMedium(){
        val date = timestamp.toDate()

        assertEquals("28/01/19 14:00:00" , date.toLocaleStringDateMedium(Locale.FRANCE))
    }
}