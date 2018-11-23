package com.deguffroy.adrien.projetphoto.Controllers.Activities

import android.os.Bundle
import android.util.Log
import com.deguffroy.adrien.projetphoto.R
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.GeoQuery
import org.imperiumlabs.geofirestore.GeoQueryDataEventListener
import java.lang.ClassCastException
import java.lang.Exception
import java.lang.NullPointerException
import java.util.*

class MainActivity : BaseActivity(), GeoQueryDataEventListener {

    private val geoPointCenter = GeoPoint(50.3663336,3.5577161999999998)
    private val geoRadius:Double = 100.0 // Kilometers Radius

    private val picturesList = arrayListOf<String>()

    private lateinit var geoFirestore:GeoFirestore
    private lateinit var geoQuery: GeoQuery

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.configureBottomView()
        this.retrieveData()
        //this.populateDB()
    }

    private fun populateDB(){
        val db = FirebaseFirestore.getInstance()
        val picturesCollection = db.collection("pictures")

        this.geoFirestore = GeoFirestore(picturesCollection)
        geoFirestore.setLocation("aQ81kFMkJxVeuYObtsPG", GeoPoint(50.3659507, 3.5568468000000166))
        geoFirestore.setLocation("5PEE40TEgCWnAHGaWXb6", GeoPoint(40.7127753, -74.0059728))
        geoFirestore.setLocation("Dum9Z60Ce1MWdOfqV6fv", GeoPoint(45.764043, 4.835658999999964))
        geoFirestore.setLocation("DzCziWiEtWKLc9JkgY6W", GeoPoint(43.296482, 5.369779999999992))
        geoFirestore.setLocation("Fv7zY3Ra09t8uI05nhzI", GeoPoint(50.3102428, 3.5797181000000364))
        geoFirestore.setLocation("mRr6oF85L29qte8zLiQv", GeoPoint(48.856614, 2.3522219000000177))
        geoFirestore.setLocation("rK45U2j9vo1R5BX0b3Qe", GeoPoint(49.9849752, 3.4451979000000392))
        geoFirestore.setLocation("vBHlS0TcSkRArVZuj0YA", GeoPoint(50.3519847, 3.519403000000011))
    }

    private fun retrieveData(){
        Log.e("retrieveData","Enter")
        val db = FirebaseFirestore.getInstance()
        val picturesCollection = db.collection("pictures")

        this.geoFirestore = GeoFirestore(picturesCollection)



        this.geoQuery = geoFirestore.queryAtLocation(geoPointCenter,geoRadius)
        this.geoQuery.removeAllListeners()
        this.geoQuery.addGeoQueryDataEventListener(this)
    }

    override fun onGeoQueryReady() {
        Log.e("onGeoQueryReady","Enter, list size : ${picturesList.size}")
        this.geoQuery.removeGeoQueryEventListener(this)

        (0 until picturesList.size).forEach{
            Log.e("onGeoQueryReady","Data : ${picturesList[it]}")
        }
    }

    override fun onDocumentExited(p0: DocumentSnapshot?) {
        Log.e("onDocumentExited","Enter")
    }

    override fun onDocumentChanged(p0: DocumentSnapshot?, p1: GeoPoint?) {
        Log.e("onDocumentChanged","Enter")
    }

    override fun onDocumentEntered(p0: DocumentSnapshot?, p1: GeoPoint?) {
        Log.e("onDocumentEntered","Enter, data : ${p0?.data}")
     try {
         val data:Map<String,Any>? = p0?.data
         val description:String = data?.get("desc") as String
         if (description != null) picturesList.add(description)
     }catch (e:NullPointerException){
         Log.e("DocumentEntered", " Error : ${e.localizedMessage}")
     }catch (e:ClassCastException){
         Log.e("DocumentEntered", " Error : ${e.localizedMessage}")
     }
    }

    override fun onDocumentMoved(p0: DocumentSnapshot?, p1: GeoPoint?) {
        Log.e("onDocumentMoved","Enter")
    }

    override fun onGeoQueryError(p0: Exception?) {
     Log.e("GEO_QUERY","Error : ${p0?.localizedMessage}")
    }
}
