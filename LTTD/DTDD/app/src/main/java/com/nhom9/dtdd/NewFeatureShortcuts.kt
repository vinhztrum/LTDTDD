package com.nhom9.dtdd

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** Additive shortcuts: original home XML and existing child views remain untouched. */
object NewFeatureShortcuts {
    fun install(activity: Activity, root: View, role: String) {
        val id = if (role == "tenant") R.id.screenTenantHome else R.id.screenOverview
        val scroll = root.findViewById<ScrollView>(id) ?: return
        val column = scroll.getChildAt(0) as? LinearLayout ?: return
        val density = activity.resources.displayMetrics.density
        fun dp(n: Int) = (n * density + .5f).toInt()
        fun background(color: Int, radius: Int): GradientDrawable = GradientDrawable().apply {
            setColor(color); cornerRadius = dp(radius).toFloat()
        }
        fun text(value: String, size: Float, bold: Boolean = false, color: Int = Color.parseColor("#0F4C47")) = TextView(activity).apply {
            this.text = value; textSize = size; setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
        }
        val section = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(16), dp(15), dp(16))
            background = background(Color.WHITE, 18)
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(16) }
        }
        section.addView(text(if (role == "tenant") "TIỆN ÍCH DÀNH CHO BẠN" else "QUẢN LÝ MỞ RỘNG", 17f, true))
        section.addView(text("Chạm để khám phá các chức năng mới", 12f, false, Color.parseColor("#7D918B")).apply {
            setPadding(0, dp(4), 0, dp(12))
        })
        data class Shortcut(val icon: String, val name: String, val detail: String, val page: String)
        val shortcuts = if (role == "tenant") listOf(
            Shortcut("📦", "Tủ nhận hàng", "Danh sách, mã nhận, xác nhận", "parcel_list"),
            Shortcut("🌱", "Bảng xếp hạng xanh", "Điểm xanh và thành tích", "ranking"),
            Shortcut("🏠", "Lịch sử thuê trọ", "Các phòng từng thuê", "rental_history"),
            Shortcut("🧾", "Lịch sử trả tiền trọ", "Biên nhận theo tháng", "payment_history"),
            Shortcut("🛏", "Đồ dùng bàn giao", "Xem và xác nhận tài sản", "handover_tenant")
        ) else listOf(
            Shortcut("📦", "Quản lý bưu kiện", "Theo dõi và xác nhận giao", "parcel_list"),
            Shortcut("🌱", "Bảng xếp hạng xanh", "Xem thành tích các phòng", "ranking"),
            Shortcut("🛏", "Kiểm kê bàn giao", "Kiểm tra tình trạng tài sản", "inventory_rooms")
        )
        shortcuts.forEach { shortcut ->
            val line = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(11), dp(12), dp(11), dp(12)); background = background(Color.parseColor("#F0F7F4"), 13)
            }
            line.addView(text(shortcut.icon, 24f), LinearLayout.LayoutParams(dp(43), -2))
            val labels = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
            labels.addView(text(shortcut.name, 14f, true))
            labels.addView(text(shortcut.detail, 11f, false, Color.parseColor("#738782")))
            line.addView(labels, LinearLayout.LayoutParams(0, -2, 1f))
            line.addView(text("›", 26f, true))
            line.setOnClickListener {
                activity.startActivity(Intent(activity, NewFeatureActivity::class.java)
                    .putExtra("role", role).putExtra("page", shortcut.page))
            }
            section.addView(line)
            section.addView(View(activity), LinearLayout.LayoutParams(1, dp(8)))
        }
        column.addView(section, minOf(1, column.childCount))
    }
}
