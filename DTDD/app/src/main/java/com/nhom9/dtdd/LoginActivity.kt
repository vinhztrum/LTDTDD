package com.nhom9.dtdd


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.nhom9.dtdd.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Xử lý khi bấm nút Tab "Chủ trọ"
        binding.tabLandlord.setOnClickListener {
            // Đổi màu hiển thị Tab
            binding.tabLandlord.setBackgroundColor(Color.WHITE)
            binding.tabLandlord.setTextColor(Color.parseColor("#154B40"))

            binding.tabTenant.setBackgroundColor(Color.parseColor("#E0E0E0"))
            binding.tabTenant.setTextColor(Color.parseColor("#757575"))

            // Ẩn form Khách thuê, Hiện form Chủ trọ
            binding.layoutTenantForm.visibility = View.GONE
            binding.layoutLandlordForm.visibility = View.VISIBLE
        }

        // 2. Xử lý khi bấm nút Tab "Khách thuê"
        binding.tabTenant.setOnClickListener {
            // Đổi màu hiển thị Tab
            binding.tabTenant.setBackgroundColor(Color.WHITE)
            binding.tabTenant.setTextColor(Color.parseColor("#154B40"))

            binding.tabLandlord.setBackgroundColor(Color.parseColor("#E0E0E0"))
            binding.tabLandlord.setTextColor(Color.parseColor("#757575"))

            // Ẩn form Chủ trọ, Hiện form Khách thuê
            binding.layoutLandlordForm.visibility = View.GONE
            binding.layoutTenantForm.visibility = View.VISIBLE
        }

        // 3. Xử lý khi bấm nút "Đăng nhập" của KHÁCH THUÊ
        binding.btnLoginTenant.setOnClickListener {
            // Chuyển sang màn hình Trang chủ Khách thuê
            val intent = Intent(this, HomeTenantActivity::class.java)
            startActivity(intent)
        }

        // 4. Xử lý khi bấm nút "Đăng nhập" của CHỦ TRỌ
        binding.btnLoginLandlord.setOnClickListener {
            // Chuyển sang màn hình Trang chủ Chủ trọ
            val intent = Intent(this, HomeLandlordActivity::class.java)
            startActivity(intent)
        }
    }
}