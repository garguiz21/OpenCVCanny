package es.ua.eps.opencvcany

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc


class MainActivity :  AppCompatActivity(), CvCameraViewListener2 {

    private val PERMISSION_REQUEST_CODE = 1001
    private lateinit var  mOpenCvCameraView: CameraBridgeViewBase
    private var isFiltering = false
    private var blurSize = 2
    private var blurImage: Mat? = null
    private var edgeSize = 3
    private var edgeImage: Mat? = null
    private var angleSize = 120
    private var angleImage: Mat? = null
    private var mFilter: Mat? = null
    private var mIsJavaCamera = true
    private var mItemSwitchCamera: MenuItem? = null

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Log.i("OCV", "OpenCV loaded successfully")
                    mOpenCvCameraView!!.enableView()
                }

                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CODE);
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main)

        mOpenCvCameraView = findViewById<CameraBridgeViewBase>(R.id.tutorial1_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        val startButton = findViewById<Button>(R.id.bStart)

        startButton.setOnClickListener {
            isFiltering = !isFiltering
            if(isFiltering)
            startButton.text = "Detener"
            else startButton.text = "Iniciar"
        }
        val sbBlur = findViewById<SeekBar>(R.id.sbBlur)
        sbBlur.progress = blurSize
        sbBlur.max = 5
        sbBlur.setOnSeekBarChangeListener (object :
            OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                blurSize = progress + 1
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Toast.makeText(
                    this@MainActivity, "Blur progress is :$blurSize",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        val sbEdge = findViewById<SeekBar>(R.id.sbEdgeGradient)
        sbEdge.max = 2
        sbEdge.setOnSeekBarChangeListener (object :
            OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                edgeSize = (progress*2) + 3
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Toast.makeText(
                    this@MainActivity, "Edge Gradient progress is :$edgeSize",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        val sbAngle = findViewById<SeekBar>(R.id.sbAngle)
        sbAngle.max = 451
        sbAngle.progress = angleSize
        sbAngle.setOnSeekBarChangeListener (object :
            OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                angleSize = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Toast.makeText(
                    this@MainActivity, "Angle progress is :$angleSize",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        mOpenCvCameraView.disableView();
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    companion object Opencv {
        private val  TAG = "MainActivity"
        init {
            if(OpenCVLoader.initDebug()){
                Log.d(TAG, "OPENCV instalado");
            } else {
                Log.d(TAG, "no se instaló")
            }
        }

    }


    override fun onCameraViewStarted(width: Int, height: Int) {
        if(mFilter == null)
            mFilter = Mat(width, height, CvType.CV_8UC2)
         if(blurImage == null)
            blurImage = Mat(width, height, CvType.CV_8UC2)
        if(edgeImage == null) {
            edgeImage = Mat(width, height, CvType.CV_8UC2)
        }

    }

    override fun onCameraViewStopped() {
        if(mFilter != null){
            mFilter?.release()
        }
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        if(isFiltering){
            Imgproc.blur(inputFrame?.gray(), blurImage, Size(blurSize.toDouble(),
                blurSize.toDouble()
            ))
            Imgproc.Canny(blurImage, mFilter, 0.0 + angleSize, 60.0 + angleSize, edgeSize)
            return mFilter ?: Mat()
        }else return inputFrame?.rgba() ?: Mat()

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                } else {
                    Toast.makeText(this, "Los permisos de camara son necesarios para el funcionamiento de la app", Toast.LENGTH_LONG).show()
                    finish()
                }
                return
            }

            else -> {
            }
        }
    }
}