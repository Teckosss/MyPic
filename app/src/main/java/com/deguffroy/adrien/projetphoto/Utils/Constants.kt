package com.deguffroy.adrien.projetphoto.Utils

import android.Manifest

/**
 * Created by Adrien Deguffroy on 23/11/2018.
 */

object Constants{

    // PERMISSIONS
    const val PERM_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    const val PERM_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    const val PERM_CAMERA = Manifest.permission.CAMERA

    const val MAP_FRAGMENT_DEFAULT_ZOOM = 8F

    // CONSTANT FOR DATA
    const val RC_TAKE_PHOTO = 100
    const val RC_PERM_LOCATION = 200

    const val SIGN_OUT_TASK = 10
}