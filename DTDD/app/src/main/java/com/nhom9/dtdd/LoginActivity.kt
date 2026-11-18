package com.nhom9.dtdd

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.nhom9.dtdd.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MotelRepository.init(this)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val initialTab = intent.getStringExtra("INITIAL_TAB")

        when (initialTab) {
            "TENANT" -> selectTenantTab()
            "ADMIN" -> selectAdminTab()
            else -> selectLandlordTab()
        }


        // Chuyển sang Tab "Chủ trọ" (Property 1=Logion-c)
        binding.tabLandlord.setOnClickListener {
            selectLandlordTab()
        }

        binding.tabAdmin.setOnClickListener {
            selectAdminTab()
        }

// Xử lý đăng nhập Admin
        binding.btnLoginAdmin.setOnClickListener {

            val email = binding.edtEmailAdmin.text.toString().trim()
            val password = binding.edtPasswordAdmin.text.toString()

            when {
                email.isBlank() -> {
                    binding.edtEmailAdmin.error = "Vui lòng nhập email"
                }

                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    binding.edtEmailAdmin.error = "Email không hợp lệ"
                }

                password.isBlank() -> {
                    binding.edtPasswordAdmin.error = "Vui lòng nhập mật khẩu"
                }

                AdminDemoAuth.verify(email, password) -> {
                    val intent = Intent(
                        this,
                        AdminHomeActivity::class.java
                    )

                    startActivity(intent)
                    finish()
                }

                else -> {
                    binding.edtPasswordAdmin.text?.clear()

                    binding.edtPasswordAdmin.error =
                        "Email hoặc mật khẩu Admin không đúng"

                    Toast.makeText(
                        this,
                        "Đăng nhập Admin thất bại",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        // Chuyển sang Tab "Khách thuê" (Property 1=Login)
        binding.tabTenant.setOnClickListener {
            selectTenantTab()
        }

        // Gợi ý dưới cùng "Khách mới? Nhập mã phòng do chủ trọ cấp" -> mở Tab Khách thuê
        binding.tvSwitchToTenantHint.setOnClickListener {
            selectTenantTab()
        }

        // Nút Đăng nhập của CHỦ TRỌ -> Mở HomeLandlordActivity
        binding.btnLoginLandlord.setOnClickListener {
            val phone = binding.edtPhoneLandlord.text.toString().trim()
            if (phone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, HomeLandlordActivity::class.java)
            startActivity(intent)
        }

        // Nút Đăng nhập của KHÁCH THUÊ -> Mở HomeTenantActivity với mã phòng tương ứng
        binding.btnLoginTenant.setOnClickListener {
            val roomCode = binding.edtRoomCode.text.toString().trim().ifEmpty { "SP203" }
            val intent = Intent(this, HomeTenantActivity::class.java).apply {
                putExtra("ROOM_CODE", roomCode)
            }
            startActivity(intent)
        }

        // Các nút phụ trợ
        binding.btnOtpSms.setOnClickListener {
            val phone = binding.edtPhoneLandlord.text.toString().trim()
            Toast.makeText(this, "Đã gửi mã OTP qua SMS tới $phone", Toast.LENGTH_SHORT).show()
        }

        binding.tvForgotPassLandlord.setOnClickListener {
            Toast.makeText(this, "Vui lòng chọn 'Nhận mã OTP qua SMS' để khôi phục mật khẩu", Toast.LENGTH_SHORT).show()
        }

        binding.btnForgotPassTenant.setOnClickListener {
            Toast.makeText(this, "Vui lòng liên hệ chủ trọ (0912 345 678) để cấp lại mật khẩu phòng", Toast.LENGTH_SHORT).show()
        }
    }


    private fun selectLandlordTab() {
        selectTab("LANDLORD")
    }

    private fun selectTenantTab() {
        selectTab("TENANT")
    }

    private fun selectAdminTab() {
        selectTab("ADMIN")
    }

    private fun selectTab(tab: String) {

        // Đưa tất cả tab về trạng thái chưa chọn
        val tabs = listOf(
            binding.tabLandlord,
            binding.tabTenant,
            binding.tabAdmin
        )

        tabs.forEach { item ->
            item.background = null
            item.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.text_secondary
                )
            )
        }

        // Xác định tab đang được chọn
        val current = when (tab) {
            "TENANT" -> binding.tabTenant
            "ADMIN" -> binding.tabAdmin
            else -> binding.tabLandlord
        }

        // Đổi màu tab được chọn
        current.setBackgroundResource(
            R.drawable.bg_tab_active
        )

        current.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.primary_green
            )
        )

        // Hiển thị đúng form
        binding.layoutLandlordForm.visibility =
            if (tab == "LANDLORD") View.VISIBLE else View.GONE

        binding.layoutTenantForm.visibility =
            if (tab == "TENANT") View.VISIBLE else View.GONE

        binding.layoutAdminForm.visibility =
            if (tab == "ADMIN") View.VISIBLE else View.GONE
    }

}
