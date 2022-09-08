package com.udacity

import android.app.DownloadManager
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityDetailBinding>(this,R.layout.activity_detail)
        val args = intent.extras!!
        val name = args[Constants.EXTRA_NAME] as String
        val status = args[Constants.EXTRA_STATUS] as String

        binding.lifecycleOwner = this
        binding.contentDetail.fileName = name
        if(status == "Success"){
            binding.contentDetail.fileStatusTextView.setTextColor(Color.GREEN)
        }else{
            binding.contentDetail.fileStatusTextView.setTextColor(Color.RED)

        }
        binding.contentDetail.status = status

    }

}
