
package com.nhom9.dtdd

/**
 * Tài khoản Admin chỉ dùng để demo.
 * Không sử dụng mật khẩu hardcode khi triển khai thực tế.
 */
object AdminDemoAuth {

    private const val DEMO_EMAIL = "admin@nhatroxanh.vn"
    private const val DEMO_PASSWORD = "Admin@123"

    fun verify(email: String, password: String): Boolean {
        return email.equals(DEMO_EMAIL, ignoreCase = true) &&
                password == DEMO_PASSWORD
    }
}
