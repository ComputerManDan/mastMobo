package com.google.mediapipe.examples.poselandmarker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.lang.Math.toDegrees
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()
    private var textPaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    init {
        initPaints()
    }

    fun clear() {
        results = null
        pointPaint.reset()
        linePaint.reset()
        textPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color = ContextCompat.getColor(context!!, R.color.mp_color_primary)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL

        textPaint.color = Color.WHITE
        textPaint.textSize = 40f
        textPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { poseLandmarkerResult ->

            val landmarks = poseLandmarkerResult.landmarks()

            if (landmarks.isNotEmpty() && landmarks[0].isNotEmpty()) {
                val nose = landmarks[0][0]
                val leftEye = landmarks[0][1]
                val rightEye = landmarks[0][4]
                val leftShoulder = landmarks[0][11]
                val rightShoulder = landmarks[0][12]
                val leftElbow = landmarks[0][13]
                val rightElbow = landmarks[0][14]
                val leftWrist = landmarks[0][15]
                val rightWrist = landmarks[0][16]
                val leftHip = landmarks[0][23]
                val rightHip = landmarks[0][24]
                val leftKnee = landmarks[0][25]
                val rightKnee = landmarks[0][26]
                val leftAnkle = landmarks[0][27]
                val rightAnkle = landmarks[0][28]
                val leftHeel = landmarks[0][29]
                val rightHeel = landmarks[0][30]
                val leftFootIndex = landmarks[0][31]
                val rightFootIndex = landmarks[0][32]

                // Calculate head angles
                val averageEyeY = (leftEye.y() + rightEye.y()) / 2
                val frontViewAngleRadians = Math.atan2((nose.y() - averageEyeY).toDouble(),
                    (nose.z() - (leftEye.z() + rightEye.z()) / 2).toDouble()
                )
                val frontViewAngleDegrees = Math.toDegrees(frontViewAngleRadians)
                val averageShoulderY = (leftShoulder.y() + rightShoulder.y()) / 2
                val sideViewAngleRadians = Math.atan2((nose.y() - averageShoulderY).toDouble(),
                    (nose.x() - (leftShoulder.x() + rightShoulder.x()) / 2).toDouble()
                )
                val sideViewAngleDegrees = Math.toDegrees(sideViewAngleRadians)
                val adjustedFrontViewAngle = frontViewAngleDegrees - 120
                val adjustedSideViewAngle = sideViewAngleDegrees + 80

                // Shoulder uprightness
                val shoulderUprightness = calculateAngle(leftHip, leftShoulder, rightShoulder)

                // Elbows and Wrists
                val leftElbowAngle = calculateAngle(leftWrist, leftElbow, leftShoulder)
                val rightElbowAngle = calculateAngle(rightWrist, rightElbow, rightShoulder)
                val leftWristAngle = calculateAngle(leftElbow, leftWrist, leftShoulder)
                val rightWristAngle = calculateAngle(rightElbow, rightWrist, rightShoulder)

                // Combined wrist and elbow angles
                val combinedWristAngle = Math.abs(Math.min(leftWristAngle, rightWristAngle) - 140)
                var combinedElbowAngle = Math.min(leftElbowAngle, rightElbowAngle)
                combinedElbowAngle = if (combinedElbowAngle < 0) 0.0 else combinedElbowAngle

                // Hips and Knees
                val hipKneeAdjustment = (40 * (20 - Math.abs(adjustedSideViewAngle)) / 20)
                val leftHipAngle = calculateAngle(leftShoulder, leftHip, leftKnee)
                val rightHipAngle = calculateAngle(rightShoulder, rightHip, rightKnee)
                var combinedHipAngle = (leftHipAngle + rightHipAngle) / 2
                if (Math.abs(adjustedSideViewAngle) < 20) combinedHipAngle -= hipKneeAdjustment

                val leftKneeAngle = calculateAngle(leftHip, leftKnee, leftHeel)
                val rightKneeAngle = calculateAngle(rightHip, rightKnee, rightHeel)
                var combinedKneeAngle = (leftKneeAngle + rightKneeAngle) / 2
                if (Math.abs(adjustedSideViewAngle) < 20) combinedKneeAngle -= hipKneeAdjustment

                combinedHipAngle = if (combinedHipAngle < 0) 0.0 else combinedHipAngle
                combinedKneeAngle = if (combinedKneeAngle < 0) 0.0 else combinedKneeAngle

                // Ankle and Feet
                val leftAnkleAngle = calculateAngle(leftKnee, leftAnkle, leftHeel)
                val rightAnkleAngle = calculateAngle(rightKnee, rightAnkle, rightHeel)
                val combinedAnkleAngle = (leftAnkleAngle + rightAnkleAngle) / 2

                val leftFootAngle = calculateAngle(leftKnee, leftHeel, leftFootIndex)
                val rightFootAngle = calculateAngle(rightKnee, rightHeel, rightFootIndex)
                val combinedFootAngle = (leftFootAngle + rightFootAngle) / 2

                // Angles dictionary
                val angles = mapOf(
                    11 to shoulderUprightness,
                    12 to shoulderUprightness,
                    13 to leftElbowAngle,
                    14 to rightElbowAngle,
                    15 to leftWristAngle,
                    16 to rightWristAngle,
                    23 to combinedHipAngle,
                    24 to combinedHipAngle,
                    25 to combinedKneeAngle,
                    26 to combinedKneeAngle,
                    27 to combinedAnkleAngle,
                    28 to combinedAnkleAngle,
                    31 to combinedFootAngle,
                    32 to combinedFootAngle
                )

                // Skeleton pairs for lines
                val skeletonPairs = listOf(
                    Pair(11, 13), Pair(13, 15), Pair(12, 14), Pair(14, 16),
                    Pair(11, 12), Pair(23, 24), Pair(11, 23), Pair(12, 24),
                    Pair(23, 25), Pair(25, 27), Pair(24, 26), Pair(26, 28),
                    Pair(27, 31), Pair(28, 32), Pair(29, 31), Pair(30, 32)
                )

                // Set up paint for circles
                val circlePaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
                val circleBorderPaint = Paint().apply { color = Color.BLACK; strokeWidth = 5f; style = Paint.Style.STROKE }
                val angleTextPaint = Paint().apply { color = Color.BLACK; textSize = 50f; textAlign = Paint.Align.CENTER }

                // Draw skeleton lines
                for (pair in skeletonPairs) {
                    val startLandmark = landmarks[0][pair.first]
                    val endLandmark = landmarks[0][pair.second]
                    canvas.drawLine(
                        startLandmark.x() * imageWidth * scaleFactor,
                        startLandmark.y() * imageHeight * scaleFactor,
                        endLandmark.x() * imageWidth * scaleFactor,
                        endLandmark.y() * imageHeight * scaleFactor,
                        linePaint
                    )
                }

                // Draw circles for joints, larger circles for joints with angles
                for ((index, landmark) in landmarks[0].withIndex()) {
                    val x = landmark.x() * imageWidth * scaleFactor
                    val y = landmark.y() * imageHeight * scaleFactor
                    val circleRadius = if (angles.containsKey(index)) 80f else 20f  // Larger circle if angle exists

                    // Draw the white circle and its black border
                    canvas.drawCircle(x, y, circleRadius, circlePaint)
                    canvas.drawCircle(x, y, circleRadius, circleBorderPaint)

                    // Draw the angle text inside the circle, if it has an angle
                    angles[index]?.let { angle ->
                        canvas.drawText(
                            "%.1f°".format(angle),
                            x, y + 15,  // Adjust text position
                            angleTextPaint
                        )
                    }
                }
            }
        }
    }






    private fun calculateAngle(A: NormalizedLandmark, B: NormalizedLandmark, C: NormalizedLandmark): Double {
        val AB = floatArrayOf(B.x() - A.x(), B.y() - A.y(), B.z() - A.z())
        val BC = floatArrayOf(C.x() - B.x(), C.y() - B.y(), C.z() - B.z())
        val dotProduct = AB[0] * BC[0] + AB[1] * BC[1] + AB[2] * BC[2]
        val magnitudeAB = sqrt(AB[0] * AB[0] + AB[1] * AB[1] + AB[2] * AB[2])
        val magnitudeBC = sqrt(BC[0] * BC[0] + BC[1] * BC[1] + BC[2] * BC[2])
        val angleRadians = acos(dotProduct / (magnitudeAB * magnitudeBC))
        return toDegrees(angleRadians.toDouble())
    }

    fun setResults(
        poseLandmarkerResults: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = poseLandmarkerResults
        this.imageHeight = imageHeight
        this.imageWidth = imageWidth
        scaleFactor = when (runningMode) {
            RunningMode.IMAGE, RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 12F
    }
}
