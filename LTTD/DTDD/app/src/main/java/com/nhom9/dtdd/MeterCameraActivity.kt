package com.nhom9.dtdd

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nhom9.dtdd.databinding.ActivityMeterCameraBinding

class MeterCameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMeterCameraBinding
    private var flashOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeterCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showState(State.CAMERA_LIVE)

        // 1. Màn Camera (Property 1=Photos)
        binding.btnBackFromCamera.setOnClickListener {
            finish()
        }

        binding.btnShutterCapture.setOnClickListener {
            // Theo prototype Figma: Bấm nút chụp (256:805) -> Mở màn Fail (256:877)
            showState(State.CAMERA_FAIL)
        }

        binding.btnCameraGallery.setOnClickListener {
            // Chọn ảnh từ thư viện -> mở màn AI đọc thành công (Photo 256:878)
            showState(State.CAMERA_SUCCESS)
        }

        binding.btnCameraFlash.setOnClickListener {
            flashOn = !flashOn
            val msg = if (flashOn) "Đã bật đèn flash" else "Đã tắt đèn flash"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // 2. Màn Cảnh báo bất thường (Fail 256:877)
        binding.btnBackFromFail.setOnClickListener {
            showState(State.CAMERA_LIVE)
        }

        binding.btnFailRetake.setOnClickListener {
            // Theo prototype Figma: Bấm "Chụp lại" ở màn Fail (256:865) -> Mở màn Photo (256:878)
            showState(State.CAMERA_SUCCESS)
        }

        binding.btnFailConfirmAnyway.setOnClickListener {
            // Theo prototype Figma: Bấm "Vẫn xác nhận 2.140" (256:867) -> Về màn Ghi chỉ số (107:30)
            val data = Intent().apply {
                putExtra("ELECTRIC_NEW", "2.140")
                putExtra("ELECTRIC_DIFF", "+638")
                putExtra("ROOM_NUMBER", "205")
            }
            setResult(Activity.RESULT_OK, data)
            finish()
        }

        // 3. Màn AI đọc thành công (Photo 256:878)
        binding.btnBackFromSuccess.setOnClickListener {
            showState(State.CAMERA_LIVE)
        }

        binding.btnSuccessRetake.setOnClickListener {
            // Theo prototype Figma: Bấm "Chụp lại" ở màn Photo (256:838) -> Quay về màn Camera (256:876)
            showState(State.CAMERA_LIVE)
        }

        binding.btnSuccessConfirm.setOnClickListener {
            // Theo prototype Figma: Bấm "Xác nhận và sang phòng 205" (256:836) -> Về màn Ghi chỉ số (107:30)
            val data = Intent().apply {
                putExtra("ELECTRIC_NEW", "1.568")
                putExtra("ELECTRIC_DIFF", "+86")
                putExtra("ROOM_NUMBER", "204")
            }
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    private enum class State {
        CAMERA_LIVE,
        CAMERA_FAIL,
        CAMERA_SUCCESS
    }

    private fun showState(state: State) {
        binding.layoutCameraLive.visibility = if (state == State.CAMERA_LIVE) View.VISIBLE else View.GONE
        binding.layoutCameraFail.visibility = if (state == State.CAMERA_FAIL) View.VISIBLE else View.GONE
        binding.layoutCameraSuccess.visibility = if (state == State.CAMERA_SUCCESS) View.VISIBLE else View.GONE
    }
}
