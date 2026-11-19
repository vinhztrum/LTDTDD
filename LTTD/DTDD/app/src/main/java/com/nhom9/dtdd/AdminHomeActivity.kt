package com.nhom9.dtdd
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.Locale
class AdminHomeActivity : AppCompatActivity() {
    private val green = Color.rgb(9, 91, 79)
    private val darkGreen = Color.rgb(8, 65, 61)
    private val mint = Color.rgb(226, 245, 237)
    private val bg = Color.rgb(245, 248, 247)
    private val ink = Color.rgb(23, 55, 48)
    private val muted = Color.rgb(99, 123, 115)
    private val border = Color.rgb(222, 235, 229)
    private val orange = Color.rgb(184, 101, 45)

    private var current = "dashboard"
    private var userId = "U002"
    private var propertyId = "NT001"
    private var roomId = ""
    private var maintenanceId = ""
    private var editing = false

    private lateinit var root: LinearLayout
    private lateinit var body: LinearLayout

    private val money =
        NumberFormat.getIntegerInstance(Locale("vi", "VN"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AdminStore.init(this)
        MotelRepository.init(this)

        window.statusBarColor = darkGreen
        window.navigationBarColor = Color.BLACK

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (current == "dashboard") {
                        finish()
                    } else {
                        show(parentRoute(current))
                    }
                }
            }
        )

        show("dashboard")
    }
    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun shape(
        color: Int,
        radius: Int = 16,
        stroke: Int = Color.TRANSPARENT
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (stroke != Color.TRANSPARENT) {
                setStroke(dp(1), stroke)
            }
        }
    }

    private fun layout(horizontal: Boolean = false) =
        LinearLayout(this).apply {
            orientation = if (horizontal) {
                LinearLayout.HORIZONTAL
            } else {
                LinearLayout.VERTICAL
            }
        }

    private fun text(
        value: String,
        size: Float = 16f,
        bold: Boolean = false,
        color: Int = ink
    ): TextView {
        return TextView(this).apply {
            this.text = value
            textSize = size
            setTextColor(color)

            if (bold) {
                setTypeface(null, Typeface.BOLD)
            }

            includeFontPadding = true
            setLineSpacing(dp(2).toFloat(), 1.05f)
        }
    }

    private fun card(
        parent: LinearLayout,
        padding: Int = 16
    ): LinearLayout {
        val c = layout().apply {
            background = shape(Color.WHITE, 18, border)
            elevation = dp(1).toFloat()
            setPadding(
                dp(padding),
                dp(padding),
                dp(padding),
                dp(padding)
            )
        }

        parent.addView(
            c,
            LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = dp(12)
            }
        )

        return c
    }

    private fun heading(value: String) {
        body.addView(
            text(value, 20f, true),
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(18)
                bottomMargin = dp(12)
            }
        )
    }

    private fun paragraph(
        parent: LinearLayout,
        value: String
    ) {
        parent.addView(
            text(value, 14f, false, muted),
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(7)
            }
        )
    }

    private fun button(
        parent: LinearLayout,
        title: String,
        secondary: Boolean = false,
        action: () -> Unit
    ) {
        val b = TextView(this).apply {
            text = title
            textSize = 16f
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            setTextColor(if (secondary) green else Color.WHITE)

            background = shape(
                if (secondary) mint else green,
                14
            )

            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }

        parent.addView(
            b,
            LinearLayout.LayoutParams(-1, dp(54)).apply {
                topMargin = dp(10)
                bottomMargin = dp(3)
            }
        )
    }

    private fun symbolFor(value: String): String {
        return when {
            value.contains("tài khoản", true) ||
                    value.contains("quản trị", true) -> "♙"

            value.contains("nhà trọ", true) -> "⌂"
            value.contains("phòng", true) -> "▦"
            value.contains("hóa đơn", true) -> "▤"
            value.contains("doanh thu", true) -> "▥"
            value.contains("bảo trì", true) ||
                    value.contains("sửa chữa", true) -> "⚒"

            value.contains("thông báo", true) -> "♧"
            value.contains("báo cáo", true) -> "▥"
            else -> "◈"
        }
    }

    private fun chip(
        value: String,
        positive: Boolean = true
    ): TextView {
        val foreground = if (positive) green else orange
        val backgroundColor = if (positive) {
            Color.rgb(229, 246, 235)
        } else {
            Color.rgb(255, 240, 221)
        }

        return text("●  $value", 13f, true, foreground).apply {
            background = shape(backgroundColor, 18)
            setPadding(dp(10), dp(6), dp(10), dp(6))
        }
    }

    private fun entry(
        parent: LinearLayout,
        title: String,
        subtitle: String,
        status: String = "",
        onClick: () -> Unit
    ) {
        val c = card(parent)

        val row = layout(true).apply {
            gravity = Gravity.CENTER_VERTICAL
        }
        c.addView(row)

        val iconView = text(
            symbolFor(title), 24f, true, green
        ).apply {
            gravity = Gravity.CENTER
            background = shape(mint, 14)
        }

        row.addView(
            iconView,
            LinearLayout.LayoutParams(dp(48), dp(48))
        )

        val content = layout().apply {
            setPadding(dp(12), 0, dp(5), 0)
        }
        row.addView(
            content,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        content.addView(text(title, 17f, true))

        if (subtitle.isNotEmpty()) {
            paragraph(content, subtitle)
        }

        if (status.isNotEmpty()) {
            val positive = !status.contains("khóa", true) &&
                    !status.contains("ngừng", true) &&
                    !status.contains("OVERDUE", true)

            content.addView(
                chip(status, positive),
                LinearLayout.LayoutParams(-2, -2).apply {
                    topMargin = dp(10)
                }
            )
        }

        val arrow = text("›", 28f, false, muted).apply {
            gravity = Gravity.CENTER
        }
        row.addView(
            arrow,
            LinearLayout.LayoutParams(dp(23), dp(48))
        )

        c.minimumHeight = dp(88)
        c.isClickable = true
        c.isFocusable = true
        c.setOnClickListener { onClick() }
    }

    private fun detailRow(
        parent: LinearLayout,
        title: String,
        value: String
    ) {
        val row = layout(true).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(12), 0, dp(12))
        }

        parent.addView(row)

        row.addView(
            text(title, 14f, false, muted),
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        row.addView(
            text(value, 15f, true).apply {
                gravity = Gravity.END
            },
            LinearLayout.LayoutParams(0, -2, 1.5f)
        )

        parent.addView(
            View(this).apply {
                setBackgroundColor(border)
            },
            LinearLayout.LayoutParams(-1, dp(1))
        )
    }

    private fun input(
        parent: LinearLayout,
        title: String,
        value: String = "",
        numeric: Boolean = false
    ): EditText {

        parent.addView(
            text(title, 15f, true, muted),
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(10)
            }
        )

        val e = EditText(this).apply {
            setText(value)
            textSize = 16f
            setSingleLine(true)
            setPadding(dp(14), 0, dp(14), 0)
            background = shape(Color.WHITE, 12, border)

            if (numeric) {
                inputType =
                    android.text.InputType.TYPE_CLASS_NUMBER
            }
        }

        parent.addView(
            e,
            LinearLayout.LayoutParams(-1, dp(54)).apply {
                topMargin = dp(7)
                bottomMargin = dp(8)
            }
        )

        return e
    }

    private fun search(
        parent: LinearLayout,
        onChange: (String) -> Unit
    ) {
        val e = input(parent, "Tìm kiếm")
        e.hint = "Nhập tên, email hoặc từ khóa..."

        e.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                onChange(s.toString())
            }

            override fun afterTextChanged(
                s: Editable?
            ) = Unit
        })
    }

    private fun confirm(
        message: String,
        onYes: () -> Unit
    ) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận")
            .setMessage(message)
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Xác nhận") { _, _ ->
                onYes()
            }
            .show()
    }

    private fun info(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Thông báo")
            .setMessage(message)
            .setPositiveButton("Đóng", null)
            .show()
    }

    private fun price(value: Long): String =
        "${money.format(value)}đ"

    private fun finishAction(
        message: String,
        destination: String
    ) {
        AdminStore.writeAudit(this, message)

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()

        show(destination)
    }

    private fun title(page: String): String {
        return mapOf(
            "dashboard" to "Tổng quan hệ thống",
            "accounts" to "Quản lý tài khoản",
            "accountDetail" to "Chi tiết tài khoản",
            "accountForm" to "Thêm / sửa tài khoản",
            "accountLock" to "Khóa tài khoản",
            "properties" to "Quản lý nhà trọ",
            "propertyDetail" to "Chi tiết nhà trọ",
            "propertyForm" to "Thêm / sửa nhà trọ",
            "rooms" to "Giám sát phòng trọ",
            "tenants" to "Tra cứu khách thuê",
            "invoices" to "Danh sách hóa đơn",
            "invoiceDetail" to "Chi tiết hóa đơn",
            "maintenance" to "Giám sát bảo trì",
            "maintenanceDetail" to "Chi tiết bảo trì",
            "reports" to "Báo cáo thống kê",
            "reportExport" to "Xuất báo cáo",
            "notifications" to "Thông báo",
            "roles" to "Phân quyền",
            "audit" to "Nhật ký hoạt động",
            "settings" to "Cài đặt hệ thống",
            "profile" to "Hồ sơ Admin",
            "changePassword" to "Đổi mật khẩu",
            "logout" to "Đăng xuất",
            "empty" to "Đang phát triển"
        )[page] ?: "Quản trị hệ thống"
    }

    private fun parentRoute(page: String): String =
        when (page) {
            "accountDetail", "accountForm" -> "accounts"
            "accountLock" -> "accountDetail"
            "propertyDetail", "propertyForm" -> "properties"
            "invoiceDetail" -> "invoices"
            "maintenanceDetail" -> "maintenance"
            "reportExport" -> "reports"
            "roles", "audit", "profile" -> "settings"
            "changePassword", "logout" -> "profile"
            else -> "dashboard"
        }

    private fun show(page: String) {
        current = page

        root = layout().apply {
            setBackgroundColor(bg)
        }
        setContentView(root)

        // Header xanh nổi bật
        val header = layout().apply {
            setPadding(
                dp(20), dp(18),
                dp(20), dp(20)
            )
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(green, darkGreen)
            )
        }

        root.addView(
            header,
            LinearLayout.LayoutParams(-1, -2)
        )

        val toolbar = layout(true).apply {
            gravity = Gravity.CENTER_VERTICAL
        }
        header.addView(toolbar)

        val back = text(
            if (page == "dashboard") {
                "⌂  NHÀ TRỌ XANH / ADMIN"
            } else {
                "‹  Quay lại"
            },
            14f, true, Color.WHITE
        ).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        toolbar.addView(
            back,
            LinearLayout.LayoutParams(0, dp(44), 1f)
        )

        back.setOnClickListener {
            if (page == "dashboard") {
                show("profile")
            } else {
                show(parentRoute(page))
            }
        }

        val bell = text(
            "🔔", 23f, true, Color.WHITE
        ).apply {
            gravity = Gravity.CENTER
            setOnClickListener {
                show("notifications")
            }
        }

        toolbar.addView(
            bell,
            LinearLayout.LayoutParams(dp(44), dp(44))
        )

        header.addView(
            text(title(page), 25f, true, Color.WHITE)
        )

        paragraphHeader(
            header,
            "Quản lý hệ thống · Dữ liệu minh họa trên máy"
        )

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
        }

        root.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        body = layout().apply {
            setPadding(
                dp(16), dp(18),
                dp(16), dp(24)
            )
        }
        scroll.addView(body)

        when (page) {
            "dashboard" -> dashboard()
            "accounts" -> accounts()
            "accountDetail" -> accountDetail()
            "accountForm" -> accountForm()
            "accountLock" -> accountLock()
            "properties" -> properties()
            "propertyDetail" -> propertyDetail()
            "propertyForm" -> propertyForm()
            "rooms" -> rooms()
            "tenants" -> tenants()
            "invoices" -> invoices()
            "invoiceDetail" -> invoiceDetail()
            "maintenance" -> maintenance()
            "maintenanceDetail" -> maintenanceDetail()
            "reports" -> reports()
            "reportExport" -> reportExport()
            "notifications" -> notifications()
            "roles" -> roles()
            "audit" -> audit()
            "settings" -> settings()
            "profile" -> profile()
            "changePassword" -> changePassword()
            "logout" -> logout()
            "empty" -> empty()
            else -> dashboard()
        }

        bottomBar()
    }

    private fun paragraphHeader(
        parent: LinearLayout,
        value: String
    ) {
        parent.addView(
            text(
                value, 13f, false,
                Color.rgb(208, 234, 223)
            ),
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(6)
            }
        )
    }

    private fun bottomBar() {
        val bar = layout(true).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(5), dp(5), dp(5), dp(5))
            setBackgroundColor(Color.WHITE)
            elevation = dp(6).toFloat()
        }

        root.addView(
            bar,
            LinearLayout.LayoutParams(-1, dp(72))
        )

        val items = listOf(
            Triple("⌂", "Tổng quan", "dashboard"),
            Triple("▦", "Nhà trọ", "properties"),
            Triple("♙", "Tài khoản", "accounts"),
            Triple("▥", "Báo cáo", "reports"),
            Triple("⚙", "Cài đặt", "settings")
        )

        items.forEach { (icon, label, destination) ->
            val selected =
                current == destination ||
                        (
                                destination == "accounts" &&
                                        current.startsWith("account")
                                ) ||
                        (
                                destination == "properties" &&
                                        current.startsWith("property")
                                )

            val item = layout().apply {
                gravity = Gravity.CENTER
                if (selected) {
                    background = shape(mint, 12)
                }
                setOnClickListener {
                    show(destination)
                }
            }

            bar.addView(
                item,
                LinearLayout.LayoutParams(0, -1, 1f).apply {
                    setMargins(dp(2), 0, dp(2), 0)
                }
            )

            item.addView(
                text(
                    icon, 22f, true,
                    if (selected) green else muted
                ).apply {
                    gravity = Gravity.CENTER
                }
            )

            item.addView(
                text(
                    label, 12f, selected,
                    if (selected) green else muted
                ).apply {
                    gravity = Gravity.CENTER
                }
            )
        }
    }
    private fun metricRow(
        first: Pair<String, String>,
        second: Pair<String, String>
    ) {
        val row = layout(true)
        body.addView(row)

        listOf(first, second).forEachIndexed {
                index, pair ->

            val c = layout().apply {
                setPadding(
                    dp(14), dp(15),
                    dp(12), dp(14)
                )
                background = shape(Color.WHITE, 18, border)
                elevation = dp(1).toFloat()
            }

            row.addView(
                c,
                LinearLayout.LayoutParams(
                    0, dp(132), 1f
                ).apply {
                    leftMargin =
                        if (index == 1) dp(5) else 0
                    rightMargin =
                        if (index == 0) dp(5) else 0
                    bottomMargin = dp(10)
                }
            )

            val top = layout(true).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
            c.addView(top)

            val icon = text(
                symbolFor(pair.first),
                22f, true, green
            ).apply {
                gravity = Gravity.CENTER
                background = shape(mint, 10)
            }

            top.addView(
                icon,
                LinearLayout.LayoutParams(dp(36), dp(36))
            )

            top.addView(
                text(pair.first, 13f, true, muted).apply {
                    setPadding(dp(8), 0, 0, 0)
                },
                LinearLayout.LayoutParams(0, -2, 1f)
            )

            c.addView(
                text(pair.second, 28f, true, green),
                LinearLayout.LayoutParams(-1, -2).apply {
                    topMargin = dp(9)
                }
            )
        }
    }

    private fun dashboard() {
        val rooms = MotelRepository.rooms

        val gross = rooms.sumOf {
            it.totalBill(
                MotelRepository.electricPrice,
                MotelRepository.waterPrice,
                MotelRepository.serviceFee
            )
        }

        val paid = rooms
            .filter { it.status == "PAID" }
            .sumOf {
                it.totalBill(
                    MotelRepository.electricPrice,
                    MotelRepository.waterPrice,
                    MotelRepository.serviceFee
                )
            }

        val welcome = card(body, 18).apply {
            background = shape(mint, 18)
        }

        welcome.addView(
            text(
                "Xin chào, Quản trị viên! 👋",
                20f, true, green
            )
        )

        paragraph(
            welcome,
            "Theo dõi hoạt động nhà trọ và quản lý người dùng."
        )

        heading("Tổng quan hoạt động")

        metricRow(
            "Nhà trọ" to
                    AdminStore.properties.size.toString(),
            "Tài khoản" to
                    AdminStore.users.size.toString()
        )

        metricRow(
            "Phòng trọ mẫu" to rooms.size.toString(),
            "Đang thuê" to rooms.count {
                it.status != "EMPTY"
            }.toString()
        )

        val revenue = card(body, 18)

        revenue.addView(
            text(
                "TỔNG HÓA ĐƠN TÍNH THỬ",
                14f, true, muted
            )
        )

        revenue.addView(
            text(price(gross), 27f, true, green),
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(7)
                bottomMargin = dp(7)
            }
        )

        val percent = if (gross > 0L) {
            (paid.toDouble() / gross * 100)
                .toInt().coerceIn(0, 100)
        } else {
            0
        }

        val progress = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            max = 100
            this.progress = percent

            progressTintList =
                ColorStateList.valueOf(green)

            progressBackgroundTintList =
                ColorStateList.valueOf(mint)
        }

        revenue.addView(
            progress,
            LinearLayout.LayoutParams(-1, dp(9)).apply {
                topMargin = dp(7)
                bottomMargin = dp(7)
            }
        )

        paragraph(
            revenue,
            "Đã đánh dấu thu: ${price(paid)}"
        )

        paragraph(
            revenue,
            "Chưa thu: ${price(gross - paid)}"
        )

        heading("Cần chú ý")

        val unpaid = rooms.count {
            it.status == "PENDING" ||
                    it.status == "OVERDUE"
        }

        entry(
            body,
            "Hóa đơn chưa thanh toán",
            "$unpaid phòng chưa đóng",
            "Xem chi tiết"
        ) {
            show("invoices")
        }

        val repairing =
            MotelRepository.maintenanceList.count {
                it.status != "DONE"
            }

        entry(
            body,
            "Yêu cầu sửa chữa",
            "$repairing yêu cầu chưa hoàn tất"
        ) {
            show("maintenance")
        }

        heading("Truy cập nhanh")

        listOf(
            "Quản lý tài khoản" to "accounts",
            "Quản lý nhà trọ" to "properties",
            "Thống kê doanh thu" to "reports",
            "Thông báo" to "notifications"
        ).forEach { (name, destination) ->
            entry(
                body,
                name,
                "Nhấn để mở chức năng"
            ) {
                show(destination)
            }
        }
    }
    private fun accounts() {
        val results = layout()

        search(body) { keyword ->
            results.removeAllViews()
            renderAccounts(results, keyword)
        }

        button(body, "+  THÊM TÀI KHOẢN") {
            editing = false
            show("accountForm")
        }

        heading("Danh sách tài khoản (${AdminStore.users.size})")

        body.addView(results)
        renderAccounts(results, "")
    }

    private fun renderAccounts(
        container: LinearLayout,
        keyword: String
    ) {
        val items = AdminStore.users.filter {
            it.name.contains(keyword, true) ||
                    it.email.contains(keyword, true) ||
                    it.role.contains(keyword, true) ||
                    it.phone.contains(keyword, true)
        }

        if (items.isEmpty()) {
            val c = card(container)
            c.addView(
                text("Không tìm thấy tài khoản", 17f, true)
            )
            paragraph(c, "Hãy thử từ khóa khác.")
            return
        }

        items.forEach { user ->
            val role = when (user.role) {
                "ADMIN" -> "QUẢN TRỊ VIÊN"
                "CHU_TRO" -> "CHỦ TRỌ"
                "KHACH_THUE" -> "KHÁCH THUÊ"
                else -> user.role
            }

            entry(
                container,
                user.name,
                "$role · ${user.email}",
                if (user.active) "Hoạt động" else "Đã khóa"
            ) {
                userId = user.id
                show("accountDetail")
            }
        }
    }

    private fun selectedUser(): AdminUser? =
        AdminStore.users.firstOrNull {
            it.id == userId
        }

    private fun accountDetail() {
        val user = selectedUser() ?: return empty()

        val c = card(body, 18)

        val row = layout(true).apply {
            gravity = Gravity.CENTER_VERTICAL
        }
        c.addView(row)

        row.addView(
            text("♙", 28f, true, green).apply {
                gravity = Gravity.CENTER
                background = shape(mint, 18)
            },
            LinearLayout.LayoutParams(dp(60), dp(60))
        )

        val names = layout().apply {
            setPadding(dp(13), 0, 0, 0)
        }
        row.addView(
            names,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        names.addView(text(user.name, 21f, true))
        paragraph(names, user.role)

        c.addView(
            chip(
                if (user.active) "Hoạt động" else "Đã khóa",
                user.active
            ),
            LinearLayout.LayoutParams(-2, -2).apply {
                topMargin = dp(16)
                bottomMargin = dp(9)
            }
        )

        detailRow(c, "Mã tài khoản", user.id)
        detailRow(c, "Email", user.email)
        detailRow(c, "Số điện thoại", user.phone)
        detailRow(c, "Vai trò", user.role)

        if (user.role != "ADMIN") {
            button(body, "CHỈNH SỬA TÀI KHOẢN") {
                editing = true
                show("accountForm")
            }

            button(
                body,
                if (user.active) {
                    "KHÓA TÀI KHOẢN"
                } else {
                    "MỞ KHÓA TÀI KHOẢN"
                },
                true
            ) {
                show("accountLock")
            }
        } else {
            val note = card(body)
            paragraph(
                note,
                "Tài khoản ADMIN không thể chỉnh sửa " +
                        "quyền bằng biểu mẫu demo."
            )
        }
    }

    private fun accountForm() {
        val user = if (editing) selectedUser() else null

        if (user?.role == "ADMIN") {
            info("Không chỉnh sửa quyền ADMIN trong bản demo.")
            show("accounts")
            return
        }

        val name = input(body, "Họ tên", user?.name ?: "")
        val email = input(body, "Email", user?.email ?: "")
        val phone = input(
            body,
            "Số điện thoại",
            user?.phone ?: ""
        )

        body.addView(
            text("Vai trò", 15f, true, muted)
        )

        val roles = listOf("CHU_TRO", "KHACH_THUE")

        val spinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@AdminHomeActivity,
                android.R.layout.simple_spinner_dropdown_item,
                roles
            )
            setSelection(
                roles.indexOf(user?.role).coerceAtLeast(0)
            )
        }

        body.addView(spinner)

        button(body, "LƯU TÀI KHOẢN") {
            val n = name.text.toString().trim()
            val e = email.text.toString().trim()
            val p = phone.text.toString().trim()

            if (n.length < 2) {
                name.error = "Họ tên tối thiểu 2 ký tự"
                return@button
            }

            if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(e).matches()
            ) {
                email.error = "Email không hợp lệ"
                return@button
            }

            if (AdminStore.users.any {
                    it.email.equals(e, true) &&
                            it.id != user?.id
                }
            ) {
                email.error = "Email đã tồn tại"
                return@button
            }

            if (user == null) {
                AdminStore.users.add(
                    AdminUser(
                        "U" + System.currentTimeMillis()
                            .toString().takeLast(8),
                        n,
                        e,
                        p,
                        roles[spinner.selectedItemPosition]
                    )
                )
            } else {
                user.name = n
                user.email = e
                user.phone = p
                user.role = roles[spinner.selectedItemPosition]
            }

            AdminStore.save(this)
            finishAction("Đã lưu tài khoản", "accounts")
        }
    }

    private fun accountLock() {
        val user = selectedUser() ?: return empty()

        if (user.role == "ADMIN") {
            info("Không thể khóa tài khoản ADMIN.")
            show("accounts")
            return
        }

        val actionName =
            if (user.active) "khóa" else "mở khóa"

        val c = card(body)
        c.addView(
            text(
                "Bạn muốn $actionName tài khoản ${user.name}?",
                18f, true
            )
        )
        paragraph(
            c,
            "Dữ liệu được giữ nguyên, chỉ thay đổi trạng thái."
        )

        button(body, "XÁC NHẬN ${actionName.uppercase()}") {
            confirm("Xác nhận $actionName ${user.name}?") {
                user.active = !user.active
                AdminStore.save(this)
                finishAction(
                    "Đã $actionName tài khoản",
                    "accountDetail"
                )
            }
        }

        button(body, "HỦY", true) {
            show("accountDetail")
        }
    }
    private fun selectedProperty(): AdminProperty? =
        AdminStore.properties.firstOrNull {
            it.id == propertyId
        }

    private fun properties() {
        val results = layout()

        search(body) { keyword ->
            results.removeAllViews()
            renderProperties(results, keyword)
        }

        button(body, "+  THÊM NHÀ TRỌ") {
            editing = false
            show("propertyForm")
        }

        heading("Danh sách nhà trọ")
        body.addView(results)
        renderProperties(results, "")
    }

    private fun renderProperties(
        container: LinearLayout,
        keyword: String
    ) {
        val items = AdminStore.properties.filter {
            it.name.contains(keyword, true) ||
                    it.address.contains(keyword, true)
        }

        if (items.isEmpty()) {
            entry(
                container,
                "Không tìm thấy nhà trọ",
                "Hãy thử từ khóa khác"
            ) {}
        }

        items.forEach { property ->
            entry(
                container,
                property.name,
                "${property.id} · ${property.address}",
                if (property.active) {
                    "Hoạt động"
                } else {
                    "Tạm ngừng"
                }
            ) {
                propertyId = property.id
                show("propertyDetail")
            }
        }
    }

    private fun propertyDetail() {
        val property = selectedProperty() ?: return empty()

        val owner = AdminStore.users.firstOrNull {
            it.id == property.ownerId
        }?.name ?: "Chưa phân công"

        val c = card(body)
        c.addView(text(property.name, 21f, true))
        detailRow(c, "Mã nhà trọ", property.id)
        detailRow(c, "Địa chỉ", property.address)
        detailRow(c, "Chủ trọ", owner)
        detailRow(
            c,
            "Trạng thái",
            if (property.active) "Hoạt động" else "Tạm ngừng"
        )

        paragraph(
            body,
            "Dữ liệu phòng hiện tại thuộc nhà trọ mẫu NT001."
        )

        button(body, "SỬA THÔNG TIN") {
            editing = true
            show("propertyForm")
        }

        if (property.id == "NT001") {
            button(body, "XEM PHÒNG", true) {
                show("rooms")
            }

            button(body, "XEM KHÁCH THUÊ", true) {
                show("tenants")
            }
        }

        button(
            body,
            if (property.active) "TẠM NGỪNG" else "KÍCH HOẠT",
            true
        ) {
            confirm("Bạn muốn đổi trạng thái nhà trọ?") {
                property.active = !property.active
                AdminStore.save(this)
                finishAction(
                    "Đã cập nhật nhà trọ",
                    "properties"
                )
            }
        }
    }

    private fun propertyForm() {
        val property =
            if (editing) selectedProperty() else null

        val name = input(
            body,
            "Tên nhà trọ",
            property?.name ?: ""
        )

        val address = input(
            body,
            "Địa chỉ",
            property?.address ?: ""
        )

        body.addView(
            text("Chủ trọ phụ trách", 15f, true, muted)
        )

        val owners = AdminStore.users.filter {
            it.role == "CHU_TRO" && it.active
        }

        val spinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@AdminHomeActivity,
                android.R.layout.simple_spinner_dropdown_item,
                owners.map { it.name }.ifEmpty {
                    listOf("Chưa có chủ trọ")
                }
            )
            setSelection(
                owners.indexOfFirst {
                    it.id == property?.ownerId
                }.coerceAtLeast(0)
            )
        }

        body.addView(spinner)

        button(body, "LƯU NHÀ TRỌ") {
            val n = name.text.toString().trim()
            val a = address.text.toString().trim()

            if (n.length < 3) {
                name.error = "Tên tối thiểu 3 ký tự"
                return@button
            }

            if (a.isEmpty()) {
                address.error = "Vui lòng nhập địa chỉ"
                return@button
            }

            if (owners.isEmpty()) {
                info("Bạn cần tạo tài khoản Chủ trọ trước.")
                return@button
            }

            val ownerId =
                owners[spinner.selectedItemPosition].id

            if (property == null) {
                AdminStore.properties.add(
                    AdminProperty(
                        "NT" + System.currentTimeMillis()
                            .toString().takeLast(7),
                        n,
                        a,
                        ownerId
                    )
                )
            } else {
                property.name = n
                property.address = a
                property.ownerId = ownerId
            }

            AdminStore.save(this)
            finishAction("Đã lưu nhà trọ", "properties")
        }
    }
    private fun rooms() {
        paragraph(
            body,
            "Dữ liệu phòng mẫu lấy từ MotelRepository."
        )

        val results = layout()

        search(body) { q ->
            results.removeAllViews()
            renderRooms(results, q)
        }

        body.addView(results)
        renderRooms(results, "")
    }

    private fun renderRooms(
        container: LinearLayout,
        keyword: String
    ) {
        val items = MotelRepository.rooms.filter {
            it.roomNumber.contains(keyword, true) ||
                    it.tenantName.contains(keyword, true)
        }

        items.forEach { room ->
            entry(
                container,
                "Phòng ${room.roomNumber}",
                "Tầng ${room.floor} · " +
                        "${price(room.roomRent)}/tháng · " +
                        room.tenantName,
                room.status
            ) {
                roomId = room.roomNumber
                show("invoiceDetail")
            }
        }
    }

    private fun tenants() {
        val results = layout()

        paragraph(
            body,
            "Danh sách khách thuê từ các phòng mẫu."
        )

        search(body) { q ->
            results.removeAllViews()
            renderTenants(results, q)
        }

        body.addView(results)
        renderTenants(results, "")
    }

    private fun renderTenants(
        container: LinearLayout,
        keyword: String
    ) {
        MotelRepository.rooms.filter {
            it.status != "EMPTY" &&
                    (
                            it.tenantName.contains(keyword, true) ||
                                    it.roomNumber.contains(keyword, true)
                            )
        }.forEach { room ->
            entry(
                container,
                room.tenantName,
                "Phòng ${room.roomNumber} · ${room.tenantPhone}",
                "Đang thuê"
            ) {
                roomId = room.roomNumber
                show("invoiceDetail")
            }
        }
    }

    private fun invoices() {
        paragraph(
            body,
            "Hóa đơn được tính từ dữ liệu phòng mẫu, " +
                    "chưa phải lịch sử hóa đơn theo kỳ."
        )
        val results = layout()
        search(body) { q ->
            results.removeAllViews()
            renderInvoices(results, q)
        }
        body.addView(results)
        renderInvoices(results, "")
    }
    private fun renderInvoices(
        container: LinearLayout,
        keyword: String
    ) {
        MotelRepository.rooms.filter {
            it.status != "EMPTY" &&
                    it.roomNumber.contains(keyword, true)
        }.forEach { room ->
            val amount = room.totalBill(
                MotelRepository.electricPrice,
                MotelRepository.waterPrice,
                MotelRepository.serviceFee
            )
            entry(
                container,
                "Hóa đơn phòng ${room.roomNumber}",
                "${room.tenantName} · ${price(amount)}",
                room.status
            ) {
                roomId = room.roomNumber
                show("invoiceDetail")
            }
        }
    }
    private fun invoiceDetail() {
        val room = MotelRepository.rooms.firstOrNull {
            it.roomNumber == roomId
        } ?: return empty()
        val total = room.totalBill(
            MotelRepository.electricPrice,
            MotelRepository.waterPrice,
            MotelRepository.serviceFee
        )
        val c = card(body)
        c.addView(
            text(
                "Phòng ${room.roomNumber}",
                21f, true
            )
        )
        paragraph(c, room.tenantName)
        detailRow(c, "Tiền phòng", price(room.roomRent))
        detailRow(c, "Điện sử dụng", "${room.electricUsed} kWh")
        detailRow(c, "Nước sử dụng", "${room.waterUsed} m³")
        detailRow(
            c,
            "Giá điện",
            price(MotelRepository.electricPrice)
        )
        detailRow(
            c,
            "Giá nước",
            price(MotelRepository.waterPrice)
        )
        detailRow(
            c,
            "Phí dịch vụ",
            price(MotelRepository.serviceFee)
        )
        c.addView(
            text(
                "Tổng cộng: ${price(total)}",
                23f, true, green
            ),
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(17)
            }
        )
        paragraph(
            c,
            "Trạng thái: ${room.status}"
        )
        button(body, "YÊU CẦU ĐỐI SOÁT") {
            AdminStore.writeAudit(
                this,
                "Xem đối soát phòng ${room.roomNumber}"
            )
            info(
                "Đã ghi nhận yêu cầu đối soát demo. " +
                        "Chưa gửi yêu cầu lên máy chủ."
            )
        }
    }
    private fun maintenance() {
        val results = layout()
        search(body) { q ->
            results.removeAllViews()
            renderMaintenance(results, q)
        }
        body.addView(results)
        renderMaintenance(results, "")
    }
    private fun renderMaintenance(
        container: LinearLayout,
        keyword: String
    ) {
        MotelRepository.maintenanceList.filter {
            it.title.contains(keyword, true) ||
                    it.roomNumber.contains(keyword, true)
        }.forEach { item ->
            entry(
                container,
                "Phòng ${item.roomNumber} · ${item.title}",
                "${item.category} · ${item.dateSent}",
                item.status
            ) {
                maintenanceId = item.id
                show("maintenanceDetail")
            }
        }
    }
    private fun maintenanceDetail() {
        val item =
            MotelRepository.maintenanceList.firstOrNull {
                it.id == maintenanceId
            } ?: return empty()
        val c = card(body)
        c.addView(text(item.title, 21f, true))
        detailRow(c, "Phòng", item.roomNumber)
        detailRow(c, "Danh mục", item.category)
        detailRow(c, "Ngày gửi", item.dateSent)
        detailRow(c, "Trạng thái", item.status)
        paragraph(c, item.description)
        heading("Cập nhật tiến độ (Demo)")
        listOf(
            "NEW" to "Mới",
            "FIXING" to "Đang sửa",
            "DONE" to "Hoàn thành"
        ).forEach { (status, name) ->
            button(body, name, true) {
                confirm(
                    "Đổi trạng thái bảo trì thành '$name'?"
                ) {
                    item.status = status
                    MotelRepository.save(this)
                    finishAction(
                        "Đã cập nhật bảo trì ${item.id}",
                        "maintenance"
                    )
                }
            }
        }
    }
    private fun reports() {
        val rooms = MotelRepository.rooms
        val occupied = rooms.count {
            it.status != "EMPTY"
        }
        val percent = if (rooms.isEmpty()) {
            0
        } else {
            occupied * 100 / rooms.size
        }
        metricRow(
            "Tỷ lệ lấp đầy" to "$percent%",
            "Số nhà trọ" to
                    AdminStore.properties.size.toString()
        )
        val total = rooms.sumOf {
            it.totalBill(
                MotelRepository.electricPrice,
                MotelRepository.waterPrice,
                MotelRepository.serviceFee
            )
        }
        val paid = rooms
            .filter { it.status == "PAID" }
            .sumOf {
                it.totalBill(
                    MotelRepository.electricPrice,
                    MotelRepository.waterPrice,
                    MotelRepository.serviceFee
                )
            }
        val c = card(body)
        c.addView(
            text("Tổng hóa đơn tính thử", 16f, true)
        )
        c.addView(
            text(
                price(total),
                28f, true, green
            ),
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(10)
            }
        )
        detailRow(c, "Đã thu", price(paid))
        detailRow(c, "Chưa thu", price(total - paid))
        paragraph(
            body,
            "Báo cáo hiện dựa trên dữ liệu phòng mẫu. " +
                    "Chưa phản ánh doanh thu toàn hệ thống."
        )
        button(body, "XUẤT BÁO CÁO") {
            show("reportExport")
        }
    }
    private fun reportExport() {
        val c = card(body)
        c.addView(
            text("Xuất báo cáo mẫu", 19f, true)
        )
        paragraph(
            c,
            "Chức năng xuất PDF hoặc Excel chưa được " +
                    "triển khai trong phiên bản demo."
        )
        button(body, "XÁC NHẬN") {
            finishAction(
                "Đã yêu cầu xuất báo cáo mẫu",
                "reports"
            )
        }
    }
    private fun notifications() {
        val maintenanceCount =
            MotelRepository.maintenanceList.count {
                it.status != "DONE"
            }
        val overdueCount = MotelRepository.rooms.count {
            it.status == "OVERDUE"
        }
        entry(
            body,
            "Bảo trì chưa hoàn thành",
            "$maintenanceCount yêu cầu cần theo dõi"
        ) {
            show("maintenance")
        }
        entry(
            body,
            "Hóa đơn quá hạn",
            "$overdueCount phòng có công nợ"
        ) {
            show("invoices")
        }
        entry(
            body,
            "Hệ thống đang ở chế độ Demo",
            "Chưa tích hợp thông báo đẩy hoặc email"
        ) {
            info(
                "Cần Firebase Cloud Messaging hoặc " +
                        "backend để gửi thông báo thật."
            )
        }
    }
    private fun roles() {
        listOf(
            "ADMIN" to
                    "Quản lý hệ thống, người dùng và nhà trọ",
            "CHU_TRO" to
                    "Quản lý nhà trọ được phân công",
            "KHACH_THUE" to
                    "Theo dõi thông tin thuê và gửi yêu cầu"
        ).forEach { (name, description) ->
            entry(body, name, description) {
                info(
                    "Phân quyền thật cần được " +
                            "kiểm soát tại backend."
                )
            }
        }
        heading("Giới hạn an toàn")
        val c = card(body)
        paragraph(
            c,
            "Admin không được xem mật khẩu gốc " +
                    "hoặc tự ý xác nhận giao dịch thanh toán."
        )
    }
    private fun audit() {
        val logs = AdminStore.audit(this)
        if (logs.isEmpty()) {
            val c = card(body)
            c.addView(text("Chưa có hoạt động", 18f, true))
            paragraph(
                c,
                "Các thao tác quản trị sẽ xuất hiện tại đây."
            )
            return
        }
        logs.forEach { log ->
            entry(body, log, "") {}
        }
    }
    private fun settings() {
        entry(
            body,
            "Phân quyền",
            "Vai trò ADMIN, CHU_TRO, KHACH_THUE"
        ) {
            show("roles")
        }
        entry(
            body,
            "Nhật ký hoạt động",
            "Lịch sử thao tác quản trị"
        ) {
            show("audit")
        }
        entry(
            body,
            "Thông báo hệ thống",
            "Công nợ và bảo trì"
        ) {
            show("notifications")
        }
        entry(
            body,
            "Hồ sơ Admin",
            "Thông tin tài khoản quản trị"
        ) {
            show("profile")
        }
        entry(
            body,
            "Cấu hình nâng cao",
            "Chức năng đang phát triển"
        ) {
            show("empty")
        }
    }
    private fun profile() {
        val c = card(body)
        c.addView(
            text("Quản trị viên", 21f, true)
        )
        detailRow(
            c,
            "Email",
            "admin@nhatroxanh.vn"
        )
        detailRow(c, "Vai trò", "ADMIN (Demo)")

        button(body, "ĐỔI MẬT KHẨU") {
            show("changePassword")
        }
        button(body, "ĐĂNG XUẤT", true) {
            show("logout")
        }
    }
    private fun changePassword() {
        val currentPassword =
            input(body, "Mật khẩu hiện tại")
        val newPassword =
            input(body, "Mật khẩu mới")
        val confirmPassword =
            input(body, "Nhập lại mật khẩu")
        listOf(
            currentPassword,
            newPassword,
            confirmPassword
        ).forEach {
            it.inputType =
                android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType
                            .TYPE_TEXT_VARIATION_PASSWORD
        }
        button(body, "LƯU MẬT KHẨU") {
            info(
                "Chưa thể đổi mật khẩu trong bản demo. " +
                        "AdminDemoAuth đang dùng tài khoản cố định. " +
                        "Cần backend để đổi mật khẩu an toàn."
            )
        }
    }
    private fun logout() {
        val c = card(body)
        c.addView(
            text(
                "Bạn muốn đăng xuất khỏi tài khoản Admin?",
                19f, true
            )
        )
        button(body, "XÁC NHẬN ĐĂNG XUẤT") {
            confirm("Bạn có chắc chắn muốn đăng xuất?") {
                val intent = Intent(
                    this,
                    LoginActivity::class.java
                ).apply {
                    putExtra("INITIAL_TAB", "ADMIN")
                    flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
        }
        button(body, "HỦY", true) {
            show("profile")
        }
    }
    private fun empty() {
        val c = card(body)
        c.addView(
            text(
                "Chức năng đang được phát triển",
                19f, true
            )
        )
        paragraph(
            c,
            "Cần kết nối cơ sở dữ liệu và API " +
                    "để triển khai nghiệp vụ đầy đủ."
        )
    }
}