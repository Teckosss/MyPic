package com.deguffroy.adrien.projetphoto

import com.deguffroy.adrien.projetphoto.Models.Picture
import com.deguffroy.adrien.projetphoto.Models.User
import com.google.firebase.Timestamp
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Created by Adrien Deguffroy on 28/01/2019.
 */
class PictureModelUnitTest {
    private lateinit var user: User
    private lateinit var newUser: User
    private lateinit var picture: Picture

    @Before
    fun setUp(){
        user = User("123456","Adrien", null, true, true)
        newUser = User("987456","TestUser", "picture", false, false)

        picture = Picture(user,
            true,
            true,
            "url.com",
            Timestamp(1548680400,0),
            "ImageTest",
            0,
            1,
            2,
            "123")
    }

    @Test
    fun getPicture(){
       assertEquals(user, picture.userSender)
       assertEquals(true, picture.isPublic)
       assertEquals(true, picture.isVerificationDone)
       assertEquals("url.com", picture.urlImage)
       assertEquals( Timestamp(1548680400,0), picture.dateCreated)
       assertEquals("ImageTest", picture.description)
       assertEquals(0, picture.views)
       assertEquals(1, picture.likes)
       assertEquals(2, picture.comments)
       assertEquals("123", picture.documentId)
    }

    @Test
    fun setPicture(){
        picture.userSender = newUser
        picture.isPublic = false
        picture.isVerificationDone = false
        picture.urlImage = "url2.fr"
        picture.dateCreated = Timestamp(1525974820,0)
        picture.description = "setNewDescription"
        picture.views = 10
        picture.likes = 15
        picture.comments = 20
        picture.documentId = "555"

        assertEquals(newUser, picture.userSender)
        assertEquals(false, picture.isPublic)
        assertEquals(false, picture.isVerificationDone)
        assertEquals("url2.fr", picture.urlImage)
        assertEquals( Timestamp(1525974820,0), picture.dateCreated)
        assertEquals("setNewDescription", picture.description)
        assertEquals(10, picture.views!!)
        assertEquals(15, picture.likes!!)
        assertEquals(20, picture.comments!!)
        assertEquals("555", picture.documentId)
    }
}