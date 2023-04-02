package com.example.motionpositionsensors

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.view.View
import android.widget.TextView
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private var mSensorManager: SensorManager? = null

    // sensors
    private var mSensorLinearAcceleration: Sensor? = null

    private var tvLineX: TextView? = null
    private var tvLineY: TextView? = null
    private var tvLineZ: TextView? = null

    private var tvSpeedX: TextView? = null
    private var tvDisX: TextView? = null

    private var tvSpeedY: TextView? = null
    private var tvDisY: TextView? = null

    private var tvSpeedZ: TextView? = null
    private var tvDisZ: TextView? = null

    private var tvTotD: TextView? = null


    // Sensor's values
    private var line = FloatArray(3)

    private var nowAccX = 0F  //Float 타입임
    private var recentSpeedX:Float = 0F //A
    private var nowSpeedX:Float = 0F  //B
    private var nowDistanceX:Float = 0F
    private var distanceX:Float = 0F //이동거리

    private var nowAccY = 0F  //Float 타입임
    private var recentSpeedY:Float = 0F //A
    private var nowSpeedY:Float = 0F  //B
    private var nowDistanceY:Float = 0F
    private var distanceY:Float = 0F //이동거리

    private var nowAccZ = 0F  //Float 타입임
    private var recentSpeedZ:Float = 0F //A
    private var nowSpeedZ:Float = 0F  //B
    private var nowDistanceZ:Float = 0F
    private var distanceZ:Float = 0F //이동거리


    private var totalD:Float = 0F //이동거리

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Identify the sensors that are on a device
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Assign the textViews
        tvLineX = findViewById<View>(R.id.label_lineX) as TextView
        tvLineY = findViewById<View>(R.id.label_lineY) as TextView
        tvLineZ = findViewById<View>(R.id.label_lineZ) as TextView

        tvSpeedX = findViewById<View>(R.id.label_speedX) as TextView
        tvDisX = findViewById<View>(R.id.label_disX) as TextView

        tvSpeedY = findViewById<View>(R.id.label_speedY) as TextView
        tvDisY = findViewById<View>(R.id.label_disY) as TextView

        tvSpeedZ = findViewById<View>(R.id.label_speedZ) as TextView
        tvDisZ = findViewById<View>(R.id.label_disZ) as TextView

        tvTotD = findViewById<View>(R.id.label_totalDis) as TextView

        // sensors connection
        mSensorLinearAcceleration = mSensorManager!!.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        // Check if all sensors are available
        val sensor_error = resources.getString(R.string.error_no_sensor)

        if (mSensorLinearAcceleration == null) {
            tvLineX!!.text = sensor_error
            tvLineY!!.text = sensor_error
            tvLineZ!!.text = sensor_error

            tvSpeedX!!.text = sensor_error
            tvDisX!!.text = sensor_error

            tvSpeedY!!.text = sensor_error
            tvDisY!!.text = sensor_error

            tvSpeedZ!!.text = sensor_error
            tvDisZ!!.text = sensor_error

            tvTotD!!.text = sensor_error
        }
    }

    override fun onStart() {
        super.onStart()
        if (mSensorLinearAcceleration != null) { mSensorManager!!.registerListener(this, mSensorLinearAcceleration, SensorManager.SENSOR_DELAY_NORMAL) }

        handler.post(handlerTask)
    }

    override fun onStop() {
        super.onStop()
        // Stop listening the sensors
        mSensorManager!!.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        // Get sensors data when values changed
        val sensorType = event.sensor.type
        when (sensorType) {

            Sensor.TYPE_LINEAR_ACCELERATION -> {
                line = event.values
                tvLineX!!.text = resources.getString(R.string.label_lineX, line[0])
                tvLineY!!.text = resources.getString(R.string.label_lineY, line[1])
                tvLineZ!!.text = resources.getString(R.string.label_lineZ, line[2])

                nowAccX = round(line[0]*100)/100
                nowAccY = round(line[1]*100)/100
                nowAccZ = round(line[2]*100)/100
            }
            else -> { }
        }

    }

    private fun getDistanceX() {
        nowSpeedX = recentSpeedX+(nowAccX/100)
        nowDistanceX = ((nowSpeedX+recentSpeedX)/2)/100
        distanceX += nowDistanceX
        recentSpeedX = nowSpeedX

        nowSpeedY = recentSpeedY+(nowAccY/100)
        nowDistanceY = ((nowSpeedY+recentSpeedY)/2)/100
        distanceY += nowDistanceY
        recentSpeedY = nowSpeedY

        nowSpeedZ = recentSpeedZ+(nowAccZ/100)
        nowDistanceZ = ((nowSpeedZ+recentSpeedZ)/2)/100
        distanceZ += nowDistanceZ
        recentSpeedZ = nowSpeedZ

        totalD += sqrt((nowDistanceX).pow(2)+(nowDistanceY).pow(2)+(nowDistanceZ).pow(2))

    }


    val handler = Handler()

    val millisTime = 10  //1000=1초에 한번씩 실행
    private val handlerTask = object : Runnable {
        override fun run() {
            getDistanceX()
            tvSpeedX!!.text = resources.getString(R.string.label_speedX, nowSpeedX)
            tvDisX!!.text = resources.getString(R.string.label_disX, distanceX)

            tvSpeedY!!.text = resources.getString(R.string.label_speedY, nowSpeedY)
            tvDisY!!.text = resources.getString(R.string.label_disY, distanceY)

            tvSpeedZ!!.text = resources.getString(R.string.label_speedZ, nowSpeedZ)
            tvDisZ!!.text = resources.getString(R.string.label_disZ, distanceZ)


            tvTotD!!.text = resources.getString(R.string.label_totalDis, totalD)

            handler.postDelayed(this, millisTime.toLong()) // millisTiem 이후 다시
        }
    }


    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    /*
    fun computeOrientation() {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrix(rotationMatrix, null, acc, mag)

        val orientationAngles = FloatArray(3)
        var radian = SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // Convert angles from radians to degree
        val angles = FloatArray(3)
        angles[0] = (radian[0].toDouble() * 180 / 3.14).toFloat()
        angles[1] = (radian[1].toDouble() * 180 / 3.14).toFloat()
        angles[2] = (radian[2].toDouble() * 180 / 3.14).toFloat()

        tvAzimuth!!.text = resources.getString(R.string.label_azimuth, angles[0])
        tvPitch!!.text = resources.getString(R.string.label_pitch, angles[1])
        tvRoll!!.text = resources.getString(R.string.label_roll, angles[2])
    }
     */
}
