package com.deguffroy.adrien.projetphoto.Controllers.Activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.deguffroy.adrien.projetphoto.Controllers.Fragments.ModerationFragment
import com.deguffroy.adrien.projetphoto.R
import com.deguffroy.adrien.projetphoto.Views.ModerationSlideAdapter
import kotlinx.android.synthetic.main.activity_moderation.*

class ModerationActivity : BaseActivity() {

    private lateinit var adapter: ModerationSlideAdapter

    companion object {
        private const val DOCUMENT_ID = "DOCUMENT_ID"
        private const val DOCUMENT_TYPE = "DOCUMENT_TYPE"
        fun newInstance(context: Context, document_id:ArrayList<String>, documentType:String) = Intent(context, ModerationActivity::class.java).apply {
            putStringArrayListExtra(DOCUMENT_ID, document_id)
            putExtra(DOCUMENT_TYPE, documentType)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moderation)

        this.configureViewPager()
    }

    private fun configureViewPager(){
        this.adapter = ModerationSlideAdapter(supportFragmentManager, intent.getStringArrayListExtra(DOCUMENT_ID), intent.getStringExtra(DOCUMENT_TYPE) )
        activity_moderation_pager.adapter = adapter
    }

    fun moveToNext(position:Int){
        adapter.deletePage(position)
        if (adapter.count == 0) finish() // NOTHING ELSE TO SHOW CLOSE THIS ACTIVITY
    }
}
