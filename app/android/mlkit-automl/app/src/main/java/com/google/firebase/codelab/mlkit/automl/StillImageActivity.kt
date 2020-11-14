package com.google.firebase.codelab.mlkit.automl


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.google.firebase.ml.common.FirebaseMLException
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_still_image.avi
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class StillImageActivity : BaseActivity() {

  private var currentPhotoFile: File? = null

  private var classifier: ImageClassifier? = null

  private val mp: MediaPlayer
    get() = MediaPlayer.create(this, R.raw.dinar)

  private val notifi: MediaPlayer
    get() = MediaPlayer.create(this, R.raw.notifi)

  private val mpp: MediaPlayer
    get() = MediaPlayer.create(this, R.raw.dinar500)

  private val mppp: MediaPlayer
    get() = MediaPlayer.create(this, R.raw.dinar2000)

  private val again: MediaPlayer
    get() = MediaPlayer.create(this, R.raw.again)


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_still_image)

    avi.smoothToShow()


    try {
      classifier = ImageClassifier(this)
    } catch (e: FirebaseMLException) {

    }

  }

  override fun onResume() {
    super.onResume()
    val r = Runnable {
      notifi.start()
      takePhoto()
    }
    Handler().postDelayed(r, 4000)
  }

  override fun onDestroy() {
    mp.run {
      stop()
      release()
    }
    mpp.run {
      stop()
      release()
    }
    notifi.run {
      stop()
      release()
    }
    classifier?.close()
    super.onDestroy()
  }
  
  @SuppressLint("SimpleDateFormat")
  @Throws(IOException::class)
  private fun createImageFile(): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val storageDir = cacheDir
    return createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
      currentPhotoFile = this
    }
  }



  @SuppressLint("MissingSuperCall")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (resultCode != Activity.RESULT_OK) return

    when (requestCode) {
      REQUEST_IMAGE_CAPTURE -> {
        FirebaseVisionImage.fromFilePath(this, Uri.fromFile(currentPhotoFile)).also {
          classifyImage(it.bitmap)
        }
      }
    }
  }

  /** Run image classification on the given [Bitmap] */

  private fun classifyImage(bitmap: Bitmap) {
    if (classifier == null) {
      again.start()
      return
    }

    // Classify image.
    classifier?.classifyFrame(bitmap)?.
      addOnCompleteListener { task ->
        if (task.isSuccessful) {
          if (task.result.toString().contains("dinar")) {
            if (task.result.toString().contains("dinar500")) {
              mpp.start()
            } else {
              if (task.result.toString().contains("dinar2000")) {
                mppp.start()
              } else {
                mp.start()
              }
            }
          }
          else{
            again.start()
          }
        }
      }
  }


  private fun takePhoto() {
    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
      takePictureIntent.putExtra("android.intent.extra.quickCapture", true)
      takePictureIntent.resolveActivity(packageManager)?.also {
        val photoFile: File? = try {
          createImageFile()
        } catch (e: IOException) {
          null
        }
        // Continue only if the File was successfully created.
        photoFile?.also {
          val photoURI: Uri = FileProvider.getUriForFile(
            this,
            "com.google.firebase.codelab.mlkit.automl.fileprovider",
            it
          )
          takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
          startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
      }
    }
  }

  companion object {

    private const val REQUEST_IMAGE_CAPTURE = 1

  }
}
