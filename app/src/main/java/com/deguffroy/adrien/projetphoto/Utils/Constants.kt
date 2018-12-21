package com.deguffroy.adrien.projetphoto.Utils

import android.Manifest

/**
 * Created by Adrien Deguffroy on 23/11/2018.
 */

// PERMISSIONS
const val PERM_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
const val PERM_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
const val PERM_CAMERA = Manifest.permission.CAMERA

const val MAP_FRAGMENT_DEFAULT_ZOOM = 4F

// CONSTANT FOR DATA
const val RC_TAKE_PHOTO = 100
const val RC_PERM_LOCATION = 200

const val MAX_NUMBER_IMAGE_DELETE = 2

const val SIGN_OUT_TASK = 10

const val URI_EXTRA_NAME = "URI"
const val UID_EXTRA_NAME = "UID"

// FRAGMENT TAG
const val FRAGMENT_TAG_KEY = "FRAGMENT_TAG_KEY"

const val FRAGMENT_HOME = "HOME"
const val FRAGMENT_MAP = "MAP"
const val FRAGMENT_MY_PIC = "MY_PIC"
const val FRAGMENT_PROFILE = "PROFILE"
const val FRAGMENT_DETAIL = "DETAIL"
