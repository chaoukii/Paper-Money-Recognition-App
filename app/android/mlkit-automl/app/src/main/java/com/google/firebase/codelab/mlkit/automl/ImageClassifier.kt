package com.google.firebase.codelab.mlkit.automl

import android.content.Context
import android.graphics.Bitmap
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.ml.common.FirebaseMLException
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions
import java.io.IOException
import java.util.Locale
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel


class ImageClassifier
@Throws(FirebaseMLException::class)
internal constructor(context: Context) {

  private val labeler: FirebaseVisionImageLabeler?

  init {

    val localModel = FirebaseAutoMLLocalModel.Builder()
      .setAssetFilePath(LOCAL_MODEL_PATH)
      .build()
    val options = FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localModel).setConfidenceThreshold(0.65f).build()
    labeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options)
  }


  internal fun classifyFrame(bitmap: Bitmap): Task<String> {
    if (labeler == null) {

      val completionSource = TaskCompletionSource<String>()
      return completionSource.task
    }
    val image = FirebaseVisionImage.fromBitmap(bitmap)

    return labeler.processImage(image).continueWith {
        task ->
      val labelProbList = task.result
      var textToShow = " "
      textToShow = if (labelProbList.isNullOrEmpty())
        "No Result"
      else
        printTopKLabels(labelProbList)

      textToShow
    }
  }

  internal fun close() {
    try {
      labeler?.close()
    } catch (e: IOException) {
    }

  }


  private val printTopKLabels: (List<FirebaseVisionImageLabel>) -> String = {
    it.joinToString(
            separator = "\n",
            limit = 3
    ) { label ->
      String.format(Locale.getDefault(), "Label: %s, Confidence: %4.2f", label.text, label.confidence)
    }
  }

  companion object {

    private const val LOCAL_MODEL_PATH = "automl/manifest.json"

  }
}
