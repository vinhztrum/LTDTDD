package com.nhom9.dtdd
import androidx.activity.OnBackPressedCallback
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.Locale
class AdminHomeActivity : AppCompatActivity() {
    private val green = Color.rgb(15, 76, 71)
    private val mint = Color.rgb(226, 242, 234)
    private val gold = Color.rgb(242, 193, 78)
    private val bg = Color.rgb(247, 249, 248)
    private val ink = Color.rgb(20, 48, 45)
    private val muted = Color.rgb(92, 112, 109)
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

        // Khởi tạo dữ liệu quản trị
        AdminStore.init(this)

        // Khởi tạo dữ liệu phòng trọ
        MotelRepository.init(this)

        // Xử lý nút quay lại
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (current == "dashboard") {
                        finish()
                    } else {
                        route(parentRoute(current))
                    }
                }
            }
        )

        // HIỂN THỊ DASHBOARD ADMIN
        show("dashboard")
    }


    private fun dp(x: Int): Int {
        return (x * resources.displayMetrics.density).toInt()
    }
    private fun shape(
        color: Int,
        radius: Int = 14,
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
    private fun ll(vertical: Boolean = true): LinearLayout {
        return LinearLayout(this).apply {
            orientation = if (vertical) {
                LinearLayout.VERTICAL
            } else {
                LinearLayout.HORIZONTAL
            }
        }
    }
    private fun label(
        s: String,
        size: Float = 14f,
        bold: Boolean = false,
        color: Int = ink
    ): TextView {
        return TextView(this).apply {
            text = s
            textSize = size
            setTextColor(color)
            if (bold) {
                setTypeface(null, Typeface.BOLD)
            }
            includeFontPadding = true
        }
    }
    private fun box(
        parent: LinearLayout,
        padding: Int = 14
    ): LinearLayout {
        return ll().apply {
            background = shape(
                Color.WHITE,
                15,
                Color.rgb(224, 234, 228)
            )
            setPadding(
                dp(padding),
                dp(padding),
                dp(padding),
                dp(padding)
            )
            val lp = LinearLayout.LayoutParams(-1, -2)
            lp.bottomMargin = dp(10)
            parent.addView(this, lp)
        }
    }
    private fun heading(text: String) {
        val t = label(text, 17f, true)
        val lp = LinearLayout.LayoutParams(-1, -2)
        lp.setMargins(0, dp(16), 0, dp(12))
        body.addView(t, lp)
    }
    private fun paragraph(
        parent: LinearLayout,
        text: String
    ) {
        val t = label(text, 12f, false, muted)
        val lp = LinearLayout.LayoutParams(-1, -2)
        lp.topMargin = dp(6)
        parent.addView(t, lp)
    }
    private fun action(
        parent: LinearLayout,
        text: String,
        secondary: Boolean = false,
        run: () -> Unit
    ) {
        val b = TextView(this).apply {
            this.text = text
            textSize = 14f
            gravity = Gravity.CENTER

            setTypeface(null, Typeface.BOLD)

            setTextColor(
                if (secondary) green else Color.WHITE
            )
            background = shape(
                if (secondary) mint else green,
                11
            )
            setOnClickListener {
                run()
            }
        }

        val lp = LinearLayout.LayoutParams(-1, dp(46))
        lp.topMargin = dp(9)

        parent.addView(b, lp)
    }

    private fun shortAction(
        parent: LinearLayout,
        text: String,
        run: () -> Unit
    ) {
        action(parent, text, true, run)
    }

    private fun entry(
        parent: LinearLayout,
        title: String,
        subtitle: String,
        badge: String = "",
        run: () -> Unit
    ) {
        val c = box(parent)

        c.addView(label(title, 15f, true))

        if (subtitle.isNotBlank()) {
            paragraph(c, subtitle)
        }

        if (badge.isNotBlank()) {
            paragraph(c, "● $badge")
        }

        c.setOnClickListener {
            run()
        }

        val lp = c.layoutParams as LinearLayout.LayoutParams
        c.minimumHeight = dp(75)
        c.layoutParams = lp
    }

    private fun edit(
        parent: LinearLayout,
        hint: String,
        value: String = "",
        numeric: Boolean = false
    ): EditText {
        parent.addView(
            label(hint, 12f, true, muted)
        )

        val e = EditText(this).apply {
            setText(value)
            textSize = 14f
            setSingleLine(true)

            setPadding(
                dp(13),
                dp(8),
                dp(13),
                dp(8)
            )

            background = shape(
                Color.WHITE,
                10,
                Color.rgb(214, 226, 218)
            )

            if (numeric) {
                inputType =
                    android.text.InputType.TYPE_CLASS_NUMBER
            }
        }

        val lp = LinearLayout.LayoutParams(-1, dp(48))
        lp.topMargin = dp(5)
        lp.bottomMargin = dp(13)

        parent.addView(e, lp)

        return e
    }

    private fun search(onChange: (String) -> Unit) {
        val e = edit(body, "Tìm kiếm", "")

        e.hint = "Nhập từ khóa tìm kiếm..."

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

    private fun route(name: String) {
        show(name)
    }

    private fun finishAction(
        message: String,
        destination: String = "dashboard"
    ) {
        AdminStore.writeAudit(this, message)

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()

        route(destination)
    }

    private fun price(v: Long): String {
        return "${money.format(v)}đ"
    }

    // =========================================
    // 2. ĐIỀU HƯỚNG CÁC MÀN HÌNH
    // =========================================

    private fun show(page: String) {

        current = page

        root = ll()
        root.setBackgroundColor(bg)

        setContentView(root)

        val header = ll().apply {
            setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(16)
            )

            setBackgroundColor(green)
        }

        root.addView(
            header,
            LinearLayout.LayoutParams(-1, -2)
        )

        val top = ll(false)
        header.addView(top)

        val back = label(
            if (page == "dashboard") {
                "⌂ NHÀ TRỌ XANH / ADMIN"
            } else {
                "‹  Quay lại"
            },
            12f,
            true,
            Color.WHITE
        )

        top.addView(
            back,
            LinearLayout.LayoutParams(
                0,
                dp(35),
                1f
            )
        )

        back.gravity = Gravity.CENTER_VERTICAL

        back.setOnClickListener {
            if (page == "dashboard") {
                route("profile")
            } else {
                route(parentRoute(page))
            }
        }

        // CHUÔNG THÔNG BÁO
        val bell = label(
            "🔔",
            21f,
            true,
            Color.WHITE
        )

        top.addView(bell)

        bell.setOnClickListener {
            route("notifications")
        }

        // TÊN MÀN HÌNH
        header.addView(
            label(
                title(page),
                23f,
                true,
                Color.WHITE
            )
        )

        paragraphHeader(
            header,
            "Quản lý hệ thống  •  Dữ liệu minh họa trên máy"
        )

        // VÙNG CUỘN NỘI DUNG
        val scroll = ScrollView(this).apply {
            isFillViewport = true
        }

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        body = ll().apply {
            setPadding(
                dp(16),
                dp(14),
                dp(16),
                dp(15)
            )
        }

        scroll.addView(body)

        // ĐIỀU HƯỚNG ĐẾN CÁC CHỨC NĂNG
        when (page) {

            "dashboard" -> dashboard()
            "notifications" -> notifications()

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

            "roles" -> roles()
            "audit" -> audit()
            "settings" -> settings()

            "profile" -> profile()
            "changePassword" -> changePassword()

            "saved" -> saved()
            "logout" -> logout()

            "forgot" -> info(
                "Khôi phục mật khẩu cần backend gửi email. " +
                        "Chưa triển khai trong bản demo."
            )

            "empty" -> empty()

            else -> dashboard()
        }

        bottomBar()
    }

    private fun paragraphHeader(
        header: LinearLayout,
        str: String
    ) {
        val t = label(
            str,
            11f,
            false,
            Color.rgb(196, 226, 217)
        )

        val lp = LinearLayout.LayoutParams(-1, -2)
        lp.topMargin = dp(6)

        header.addView(t, lp)
    }

    private fun title(p: String): String {
        return mapOf(
            "dashboard" to "Tổng quan hệ thống",
            "notifications" to "Thông báo",
            "accounts" to "Quản lý tài khoản",
            "accountDetail" to "Chi tiết tài khoản",
            "accountForm" to "Thêm / sửa tài khoản",
            "accountLock" to "Khóa tài khoản",
            "properties" to "Quản lý nhà trọ",
            "propertyDetail" to "Chi tiết nhà trọ",
            "propertyForm" to "Thêm / sửa nhà trọ",
            "rooms" to "Giám sát phòng",
            "tenants" to "Tra cứu khách thuê",
            "invoices" to "Danh sách hóa đơn",
            "invoiceDetail" to "Chi tiết hóa đơn",
            "maintenance" to "Giám sát bảo trì",
            "maintenanceDetail" to "Chi tiết bảo trì",
            "reports" to "Báo cáo thống kê",
            "reportExport" to "Xuất báo cáo",
            "roles" to "Phân quyền",
            "audit" to "Nhật ký hoạt động",
            "settings" to "Cài đặt",
            "profile" to "Hồ sơ Admin",
            "changePassword" to "Đổi mật khẩu",
            "saved" to "Thành công",
            "logout" to "Đăng xuất",
            "empty" to "Chưa có dữ liệu"
        )[p] ?: "Quản trị"
    }

    private fun parentRoute(p: String): String {
        return when (p) {

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
    }

    // =========================================
    // 3. THANH ĐIỀU HƯỚNG DƯỚI
    // =========================================

    private fun bottomBar() {

        val bar = ll(false).apply {
            setPadding(
                dp(5),
                dp(7),
                dp(5),
                dp(7)
            )

            setBackgroundColor(Color.WHITE)
        }

        root.addView(
            bar,
            LinearLayout.LayoutParams(-1, dp(66))
        )

        val menus = listOf(
            "⌂" to "dashboard",
            "▦" to "properties",
            "♙" to "accounts",
            "▥" to "reports",
            "⚙" to "settings"
        )

        menus.forEach { (icon, route) ->

            val name = when (route) {
                "dashboard" -> "Tổng quan"
                "properties" -> "Nhà trọ"
                "accounts" -> "Tài khoản"
                "reports" -> "Báo cáo"
                else -> "Cài đặt"
            }

            val b = label(
                "$icon\n$name",
                11f,
                current == route,
                if (current == route) green else muted
            )

            b.gravity = Gravity.CENTER

            bar.addView(
                b,
                LinearLayout.LayoutParams(
                    0,
                    -1,
                    1f
                )
            )

            b.setOnClickListener {
                this.route(route)
            }
        }
    }

    // =========================================
    // 4. THẺ THỐNG KÊ DASHBOARD
    // =========================================

    private fun metricRow(
        a: Pair<String, String>,
        b: Pair<String, String>
    ) {
        val row = ll(false)
        body.addView(row)

        listOf(a, b).forEach { (k, v) ->

            val c = ll().apply {

                setPadding(
                    dp(14),
                    dp(15),
                    dp(12),
                    dp(15)
                )

                background = shape(
                    Color.WHITE,
                    14,
                    Color.rgb(225, 235, 228)
                )
            }

            val lp = LinearLayout.LayoutParams(
                0,
                dp(98),
                1f
            )

            lp.setMargins(
                dp(2),
                0,
                dp(5),
                dp(8)
            )

            row.addView(c, lp)

            c.addView(
                label(k, 12f, false, muted)
            )

            val valText = label(
                v,
                25f,
                true,
                green
            )

            val vlp = LinearLayout.LayoutParams(-1, -2)
            vlp.topMargin = dp(6)

            c.addView(valText, vlp)
        }
    }

    // =========================================
    // 5. DASHBOARD ADMIN
    // =========================================

    private fun dashboard() {

        val rooms = MotelRepository.rooms

        metricRow(
            "Nhà trọ" to AdminStore.properties.size.toString(),
            "Tài khoản" to AdminStore.users.size.toString()
        )

        metricRow(
            "Phòng trọ mẫu" to rooms.size.toString(),
            "Đang thuê" to rooms.count {
                it.status != "EMPTY"
            }.toString()
        )

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

        val c = box(body)

        c.addView(
            label(
                "Tổng hóa đơn tính thử",
                13f,
                true,
                green
            )
        )

        c.addView(
            label(
                price(gross),
                24f,
                true
            )
        )

        paragraph(
            c,
            "Đã đánh dấu thu: ${price(paid)} · " +
                    "Chưa thu: ${price(gross - paid)}"
        )

        heading("Cần chú ý")

        entry(
            body,
            "Hóa đơn chưa thanh toán",
            "${rooms.count {
                it.status == "PENDING" ||
                        it.status == "OVERDUE"
            }} phòng chưa đóng",
            "Xem chi tiết"
        ) {
            route("invoices")
        }

        entry(
            body,
            "Yêu cầu sửa chữa",
            "${MotelRepository.maintenanceList.count {
                it.status != "DONE"
            }} yêu cầu chưa hoàn tất"
        ) {
            route("maintenance")
        }

        heading("Truy cập nhanh")

        listOf(
            "Quản lý tài khoản" to "accounts",
            "Quản lý nhà trọ" to "properties",
            "Thống kê doanh thu" to "reports",
            "Thông báo" to "notifications"
        ).forEach { (t, p) ->
            shortAction(body, "› $t") {
                route(p)
            }
        }
    }

    // =========================================
    // 6. THÔNG BÁO
    // =========================================

    private fun notifications() {

        entry(
            body,
            "Bảo trì chưa hoàn thành",
            "${MotelRepository.maintenanceList.count {
                it.status != "DONE"
            }} yêu cầu cần theo dõi",
            "Bảo trì"
        ) {
            route("maintenance")
        }

        entry(
            body,
            "Hóa đơn quá hạn",
            "${MotelRepository.rooms.count {
                it.status == "OVERDUE"
            }} phòng có công nợ",
            "Hóa đơn"
        ) {
            route("invoices")
        }

        entry(
            body,
            "Hệ thống đang ở chế độ Demo",
            "Chưa có dịch vụ thông báo đẩy hoặc email thật"
        ) {
            info(
                "Tính năng thông báo đẩy sẽ cần " +
                        "Firebase Cloud Messaging hoặc backend."
            )
        }
    }

    // =========================================
    // 7. DANH SÁCH TÀI KHOẢN
    // =========================================

    private fun accounts() {

        action(body, "+ THÊM TÀI KHOẢN") {
            editing = false
            route("accountForm")
        }

        val results = ll()
        body.addView(results)

        fun refresh(q: String) {

            results.removeAllViews()

            AdminStore.users.filter {
                it.name.contains(q, true) ||
                        it.email.contains(q, true) ||
                        it.role.contains(q, true)
            }.forEach { u ->

                entry(
                    results,
                    u.name,
                    "${u.role} · ${u.email}",
                    if (u.active) "Hoạt động" else "Đã khóa"
                ) {
                    userId = u.id
                    route("accountDetail")
                }
            }
        }

        search {
            refresh(it)
        }

        refresh("")
    }

    private fun selectedUser(): AdminUser? {
        return AdminStore.users.firstOrNull {
            it.id == userId
        }
    }

    // =========================================
    // 8. CHI TIẾT TÀI KHOẢN
    // =========================================

    private fun accountDetail() {

        val u = selectedUser() ?: return empty()

        val c = box(body)

        c.addView(
            label(u.name, 20f, true)
        )

        paragraph(
            c,
            "Mã: ${u.id}\n" +
                    "Email: ${u.email}\n" +
                    "SĐT: ${u.phone}\n" +
                    "Vai trò: ${u.role}\n" +
                    "Trạng thái: ${
                        if (u.active) "Hoạt động" else "Đã khóa"
                    }"
        )

        action(body, "CHỈNH SỬA TÀI KHOẢN") {
            editing = true
            route("accountForm")
        }

        if (u.role != "ADMIN") {

            shortAction(
                body,
                if (u.active) {
                    "KHÓA TÀI KHOẢN"
                } else {
                    "MỞ KHÓA TÀI KHOẢN"
                }
            ) {
                route("accountLock")
            }
        }
    }

    // =========================================
    // 9. THÊM / SỬA TÀI KHOẢN
    // =========================================

    private fun accountForm() {

        val u = if (editing) selectedUser() else null

        val name = edit(
            body,
            "Họ tên",
            u?.name ?: ""
        )

        val email = edit(
            body,
            "Email",
            u?.email ?: ""
        )

        val phone = edit(
            body,
            "Số điện thoại",
            u?.phone ?: ""
        )

        body.addView(
            label("Vai trò", 12f, true, muted)
        )

        val roles = listOf(
            "CHU_TRO",
            "KHACH_THUE"
        )

        val spin = Spinner(this)

        spin.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            roles
        )

        spin.setSelection(
            roles.indexOf(u?.role).coerceAtLeast(0)
        )

        body.addView(spin)

        if (u?.role == "ADMIN") {
            info(
                "Không chỉnh sửa quyền ADMIN " +
                        "bằng biểu mẫu demo."
            )
            route("accounts")
            return
        }

        action(body, "LƯU TÀI KHOẢN") {

            val n = name.text.toString().trim()
            val e = email.text.toString().trim()
            val p = phone.text.toString().trim()

            if (n.length < 2) {
                name.error = "Nhập ít nhất 2 ký tự"
                return@action
            }

            if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(e).matches()
            ) {
                email.error = "Email không hợp lệ"
                return@action
            }

            if (AdminStore.users.any {
                    it.email.equals(e, true) &&
                            it.id != u?.id
                }
            ) {
                email.error = "Email đã tồn tại"
                return@action
            }

            if (u == null) {

                AdminStore.users.add(
                    AdminUser(
                        "U" + System.currentTimeMillis()
                            .toString().takeLast(8),
                        n,
                        e,
                        p,
                        roles[spin.selectedItemPosition]
                    )
                )

            } else {
                u.name = n
                u.email = e
                u.phone = p
                u.role = roles[spin.selectedItemPosition]
            }

            AdminStore.save(this)

            finishAction(
                "Đã lưu tài khoản",
                "accounts"
            )
        }
    }

    // =========================================
    // 10. KHÓA / MỞ KHÓA TÀI KHOẢN
    // =========================================

    private fun accountLock() {

        val u = selectedUser() ?: return empty()

        val desc = if (u.active) {
            "khóa"
        } else {
            "mở khóa"
        }

        val c = box(body)

        c.addView(
            label(
                "Bạn có muốn $desc tài khoản ${u.name}?",
                16f,
                true
            )
        )

        paragraph(
            c,
            "Dữ liệu được giữ nguyên, " +
                    "chỉ thay đổi trạng thái."
        )

        action(
            body,
            "XÁC NHẬN ${desc.uppercase()}"
        ) {
            confirm(
                "Xác nhận $desc ${u.name}?"
            ) {
                u.active = !u.active

                AdminStore.save(this)

                finishAction(
                    "Đã $desc tài khoản",
                    "accountDetail"
                )
            }
        }

        shortAction(body, "HỦY") {
            route("accountDetail")
        }
    }

    // =========================================
    // 11. DANH SÁCH NHÀ TRỌ
    // =========================================

    private fun properties() {

        action(body, "+ THÊM NHÀ TRỌ") {
            editing = false
            route("propertyForm")
        }

        val list = ll()
        body.addView(list)

        fun refresh(q: String) {

            list.removeAllViews()

            AdminStore.properties.filter {
                it.name.contains(q, true) ||
                        it.address.contains(q, true)
            }.forEach { p ->

                entry(
                    list,
                    p.name,
                    "${p.id} · ${p.address}",
                    if (p.active) {
                        "Hoạt động"
                    } else {
                        "Tạm ngừng"
                    }
                ) {
                    propertyId = p.id
                    route("propertyDetail")
                }
            }
        }

        search {
            refresh(it)
        }

        refresh("")
    }

    private fun selectedProperty(): AdminProperty? {
        return AdminStore.properties.firstOrNull {
            it.id == propertyId
        }
    }

    // =========================================
    // 12. CHI TIẾT NHÀ TRỌ
    // =========================================

    private fun propertyDetail() {

        val p = selectedProperty() ?: return empty()

        val owner = AdminStore.users.firstOrNull {
            it.id == p.ownerId
        }?.name ?: "Chưa phân công"

        val c = box(body)

        c.addView(
            label(p.name, 20f, true)
        )

        paragraph(
            c,
            "Mã: ${p.id}\n" +
                    "Địa chỉ: ${p.address}\n" +
                    "Chủ trọ: $owner\n" +
                    "Trạng thái: ${
                        if (p.active) "Hoạt động" else "Tạm ngừng"
                    }"
        )

        paragraph(
            body,
            "Lưu ý: danh sách phòng trong MotelRepository " +
                    "hiện chỉ thuộc nhà trọ mẫu NT001."
        )

        action(body, "SỬA THÔNG TIN") {
            editing = true
            route("propertyForm")
        }

        if (p.id == "NT001") {

            shortAction(body, "XEM PHÒNG") {
                route("rooms")
            }

            shortAction(body, "XEM KHÁCH THUÊ") {
                route("tenants")
            }
        }

        shortAction(
            body,
            if (p.active) "TẠM NGỪNG" else "KÍCH HOẠT"
        ) {
            confirm(
                "Đổi trạng thái nhà trọ?"
            ) {
                p.active = !p.active

                AdminStore.save(this)

                finishAction(
                    "Đã cập nhật nhà trọ",
                    "properties"
                )
            }
        }
    }

    // =========================================
    // 13. THÊM / SỬA NHÀ TRỌ
    // =========================================

    private fun propertyForm() {

        val p = if (editing) selectedProperty() else null

        val name = edit(
            body,
            "Tên nhà trọ",
            p?.name ?: ""
        )

        val address = edit(
            body,
            "Địa chỉ",
            p?.address ?: ""
        )

        body.addView(
            label(
                "Chủ trọ phụ trách",
                12f,
                true,
                muted
            )
        )

        val owners = AdminStore.users.filter {
            it.role == "CHU_TRO" && it.active
        }

        val names = owners.map {
            it.name
        }

        val spin = Spinner(this)

        spin.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            names.ifEmpty {
                listOf("Chưa có chủ trọ")
            }
        )

        spin.setSelection(
            owners.indexOfFirst {
                it.id == p?.ownerId
            }.coerceAtLeast(0)
        )

        body.addView(spin)

        action(body, "LƯU NHÀ TRỌ") {

            if (name.text.toString().trim().length < 3) {
                name.error =
                    "Tên nhà trọ ít nhất 3 ký tự"
                return@action
            }

            if (address.text.toString().isBlank()) {
                address.error =
                    "Vui lòng nhập địa chỉ"
                return@action
            }

            if (owners.isEmpty()) {
                info(
                    "Hãy tạo tài khoản Chủ trọ trước."
                )
                return@action
            }

            val owner = owners[
                spin.selectedItemPosition
            ].id

            if (p == null) {

                AdminStore.properties.add(
                    AdminProperty(
                        "NT" + System.currentTimeMillis()
                            .toString().takeLast(7),
                        name.text.toString().trim(),
                        address.text.toString().trim(),
                        owner
                    )
                )

            } else {

                p.name = name.text.toString().trim()
                p.address = address.text.toString().trim()
                p.ownerId = owner
            }

            AdminStore.save(this)

            finishAction(
                "Đã lưu nhà trọ",
                "properties"
            )
        }
    }

    // =========================================
    // 14. GIÁM SÁT PHÒNG TRỌ
    // =========================================

    private fun rooms() {

        paragraph(
            body,
            "Dữ liệu các phòng thuộc nhà trọ mẫu NT001, " +
                    "lấy trực tiếp từ MotelRepository."
        )

        val result = ll()
        body.addView(result)

        fun refresh(q: String) {

            result.removeAllViews()

            MotelRepository.rooms.filter {
                it.roomNumber.contains(q, true) ||
                        it.tenantName.contains(q, true)
            }.forEach { r ->

                entry(
                    result,
                    "Phòng ${r.roomNumber}",
                    "Tầng ${r.floor} · " +
                            "${price(r.roomRent)}/tháng · " +
                            r.tenantName,
                    r.status
                ) {
                    roomId = r.roomNumber
                    route("invoiceDetail")
                }
            }
        }

        search {
            refresh(it)
        }

        refresh("")
    }

    // =========================================
    // 15. TRA CỨU KHÁCH THUÊ
    // =========================================

    private fun tenants() {

        paragraph(
            body,
            "Chỉ xem người thuê của các phòng mẫu; " +
                    "không công khai CCCD trong màn hình tổng quan."
        )

        val result = ll()
        body.addView(result)

        fun refresh(q: String) {

            result.removeAllViews()

            MotelRepository.rooms.filter {
                it.status != "EMPTY" &&
                        (
                                it.tenantName.contains(q, true) ||
                                        it.roomNumber.contains(q, true)
                                )
            }.forEach { r ->

                entry(
                    result,
                    r.tenantName,
                    "Phòng ${r.roomNumber} · ${r.tenantPhone}",
                    "Đang thuê"
                ) {
                    roomId = r.roomNumber
                    route("invoiceDetail")
                }
            }
        }

        search {
            refresh(it)
        }

        refresh("")
    }

    // =========================================
    // 16. DANH SÁCH HÓA ĐƠN
    // =========================================

    private fun invoices() {

        paragraph(
            body,
            "Hóa đơn tính từ giá thuê và số điện nước hiện có. " +
                    "Chưa có bảng hóa đơn lưu theo kỳ."
        )

        val list = ll()
        body.addView(list)

        fun refresh(q: String) {

            list.removeAllViews()

            MotelRepository.rooms.filter {
                it.status != "EMPTY" &&
                        it.roomNumber.contains(q, true)
            }.forEach { r ->

                val total = r.totalBill(
                    MotelRepository.electricPrice,
                    MotelRepository.waterPrice,
                    MotelRepository.serviceFee
                )

                entry(
                    list,
                    "Hóa đơn phòng ${r.roomNumber}",
                    "${r.tenantName} · ${price(total)}",
                    r.status
                ) {
                    roomId = r.roomNumber
                    route("invoiceDetail")
                }
            }
        }

        search {
            refresh(it)
        }

        refresh("")
    }

    // =========================================
    // 17. CHI TIẾT HÓA ĐƠN
    // =========================================

    private fun invoiceDetail() {

        val r = MotelRepository.rooms.firstOrNull {
            it.roomNumber == roomId
        } ?: MotelRepository.rooms.firstOrNull()
        ?: return empty()

        val c = box(body)

        c.addView(
            label(
                "Phòng ${r.roomNumber} · ${r.tenantName}",
                17f,
                true
            )
        )

        paragraph(
            c,
            "Tiền phòng: ${price(r.roomRent)}\n" +
                    "Điện: ${r.electricUsed} kWh × " +
                    "${price(MotelRepository.electricPrice)}\n" +
                    "Nước: ${r.waterUsed} m³ × " +
                    "${price(MotelRepository.waterPrice)}\n" +
                    "Dịch vụ: ${price(MotelRepository.serviceFee)}"
        )

        c.addView(
            label(
                "Tổng: ${
                    price(
                        r.totalBill(
                            MotelRepository.electricPrice,
                            MotelRepository.waterPrice,
                            MotelRepository.serviceFee
                        )
                    )
                }",
                21f,
                true,
                green
            )
        )

        paragraph(
            c,
            "Trạng thái: ${r.status}. " +
                    "Admin chỉ giám sát, không tự ý xác nhận thanh toán."
        )

        action(body, "YÊU CẦU ĐỐI SOÁT") {

            info(
                "Đã ghi nhận yêu cầu đối soát demo. " +
                        "Chưa gửi thông báo cho chủ trọ " +
                        "vì chưa có máy chủ."
            )

            AdminStore.writeAudit(
                this,
                "Xem đối soát phòng ${r.roomNumber}"
            )
        }
    }

    // =========================================
    // 18. DANH SÁCH BẢO TRÌ
    // =========================================

    private fun maintenance() {

        val list = ll()
        body.addView(list)

        fun refresh(q: String) {

            list.removeAllViews()

            MotelRepository.maintenanceList.filter {
                it.title.contains(q, true) ||
                        it.roomNumber.contains(q, true)
            }.forEach { m ->

                entry(
                    list,
                    "Phòng ${m.roomNumber} · ${m.title}",
                    "${m.category} · ${m.dateSent}",
                    m.status
                ) {
                    maintenanceId = m.id
                    route("maintenanceDetail")
                }
            }
        }

        search {
            refresh(it)
        }

        refresh("")
    }

    // =========================================
    // 19. CHI TIẾT BẢO TRÌ
    // =========================================

    private fun maintenanceDetail() {

        val m = MotelRepository.maintenanceList.firstOrNull {
            it.id == maintenanceId
        } ?: return empty()

        val c = box(body)

        c.addView(
            label(m.title, 19f, true)
        )

        paragraph(
            c,
            "Phòng: ${m.roomNumber}\n" +
                    "Danh mục: ${m.category}\n" +
                    "Nội dung: ${m.description}\n" +
                    "Ngày gửi: ${m.dateSent}\n" +
                    "Trạng thái: ${m.status}"
        )

        body.addView(
            label(
                "Cập nhật tiến độ (Demo)",
                13f,
                true,
                green
            )
        )

        listOf(
            "NEW" to "Mới",
            "FIXING" to "Đang sửa",
            "DONE" to "Hoàn thành"
        ).forEach { (s, t) ->

            shortAction(body, t) {

                confirm(
                    "Đổi trạng thái bảo trì thành '$t'?"
                ) {
                    m.status = s

                    MotelRepository.save(this)

                    finishAction(
                        "Đã cập nhật bảo trì ${m.id}",
                        "maintenance"
                    )
                }
            }
        }
    }

    // =========================================
    // 20. BÁO CÁO THỐNG KÊ
    // =========================================

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
            "Số nhà trọ" to AdminStore.properties.size.toString()
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

        val c = box(body)

        c.addView(
            label(
                "Tổng hóa đơn tính thử",
                14f,
                true
            )
        )

        c.addView(
            label(
                price(total),
                25f,
                true,
                green
            )
        )

        paragraph(
            c,
            "Đã đánh dấu thanh toán: ${price(paid)}\n" +
                    "Còn lại: ${price(total - paid)}"
        )

        paragraph(
            body,
            "Chưa phải doanh thu toàn hệ thống hay " +
                    "báo cáo lịch sử. Dự án hiện chỉ có dữ liệu phòng mẫu."
        )

        action(body, "XUẤT BÁO CÁO") {
            route("reportExport")
        }
    }

    // =========================================
    // 21. XUẤT BÁO CÁO
    // =========================================

    private fun reportExport() {

        val c = box(body)

        c.addView(
            label(
                "Xuất báo cáo mẫu",
                17f,
                true
            )
        )

        paragraph(
            c,
            "Bản demo chưa kết xuất PDF/Excel. " +
                    "Bạn có thể sao chép thống kê và " +
                    "phát triển xuất CSV ở bước tiếp theo."
        )

        action(body, "XÁC NHẬN") {

            finishAction(
                "Đã yêu cầu xuất báo cáo mẫu",
                "reports"
            )
        }
    }

    // =========================================
    // 22. PHÂN QUYỀN
    // =========================================

    private fun roles() {

        listOf(
            "ADMIN" to
                    "Quản lý tài khoản, nhà trọ, cấu hình và giám sát",

            "CHU_TRO" to
                    "Quản lý nhà trọ được phân công",

            "KHACH_THUE" to
                    "Theo dõi thông tin thuê và gửi yêu cầu"
        ).forEach { (t, d) ->

            entry(body, t, d) {

                info(
                    "Phân quyền thật cần được kiểm soát tại backend."
                )
            }
        }

        heading("Giới hạn an toàn")

        paragraph(
            body,
            "Admin không được xem mật khẩu gốc " +
                    "hoặc trực tiếp sửa giao dịch thanh toán " +
                    "của người thuê."
        )
    }

    // =========================================
    // 23. NHẬT KÝ HOẠT ĐỘNG
    // =========================================

    private fun audit() {

        val items = AdminStore.audit(this)

        if (items.isEmpty()) {

            entry(
                body,
                "Chưa có hoạt động",
                "Hãy thử thêm tài khoản hoặc nhà trọ"
            ) {}

        } else {

            items.forEach {

                entry(
                    body,
                    it,
                    ""
                ) {}
            }
        }
    }

    // =========================================
    // 24. CÀI ĐẶT ADMIN
    // =========================================

    private fun settings() {

        entry(
            body,
            "Phân quyền",
            "Vai trò ADMIN, CHU_TRO, KHACH_THUE"
        ) {
            route("roles")
        }

        entry(
            body,
            "Nhật ký hoạt động",
            "Lịch sử thao tác Demo"
        ) {
            route("audit")
        }

        entry(
            body,
            "Thông báo hệ thống",
            "Công nợ và bảo trì"
        ) {
            route("notifications")
        }

        entry(
            body,
            "Hồ sơ Admin",
            "Thông tin tài khoản quản trị"
        ) {
            route("profile")
        }

        entry(
            body,
            "Cấu hình nâng cao",
            "Chức năng cần backend triển khai"
        ) {
            route("empty")
        }
    }

    // =========================================
    // 25. HỒ SƠ ADMIN
    // =========================================

    private fun profile() {

        val c = box(body)

        c.addView(
            label(
                "Quản trị viên",
                19f,
                true
            )
        )

        paragraph(
            c,
            "admin@nhatroxanh.vn\n" +
                    "Vai trò: ADMIN (demo)"
        )

        action(body, "ĐỔI MẬT KHẨU") {
            route("changePassword")
        }

        shortAction(body, "ĐĂNG XUẤT") {
            route("logout")
        }
    }

    // =========================================
    // 26. ĐỔI MẬT KHẨU
    // =========================================

    private fun changePassword() {

        val a = edit(
            body,
            "Mật khẩu hiện tại"
        )

        val b = edit(
            body,
            "Mật khẩu mới"
        )

        val c = edit(
            body,
            "Nhập lại mật khẩu"
        )

        listOf(a, b, c).forEach {
            it.inputType =
                android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        action(body, "LƯU MẬT KHẨU") {

            info(
                "Không thể đổi mật khẩu demo tại đây: " +
                        "AdminDemoAuth đang dùng hằng số cố định. " +
                        "Cần backend xác thực để hỗ trợ " +
                        "đổi mật khẩu an toàn."
            )
        }
    }

    // =========================================
    // 27. THÔNG BÁO THÀNH CÔNG
    // =========================================

    private fun saved() {

        entry(
            body,
            "✓ Thao tác thành công",
            "Quay về trang tổng quan"
        ) {
            route("dashboard")
        }
    }

    // =========================================
    // 28. ĐĂNG XUẤT ADMIN
    // =========================================

    private fun logout() {

        val c = box(body)

        c.addView(
            label(
                "Bạn muốn đăng xuất khỏi tài khoản Admin?",
                16f,
                true
            )
        )

        action(
            body,
            "XÁC NHẬN ĐĂNG XUẤT"
        ) {

            confirm(
                "Bạn có chắc chắn muốn đăng xuất?"
            ) {

                val intent = Intent(
                    this,
                    LoginActivity::class.java
                ).apply {

                    putExtra(
                        "INITIAL_TAB",
                        "ADMIN"
                    )

                    flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

                startActivity(intent)
                finish()
            }
        }

        shortAction(body, "HỦY") {
            route("profile")
        }
    }

    // =========================================
    // 29. TRẠNG THÁI CHƯA CÓ DỮ LIỆU
    // =========================================

    private fun empty() {

        val c = box(body)

        c.addView(
            label(
                "Chức năng đang được phát triển",
                17f,
                true
            )
        )

        paragraph(
            c,
            "Cần kết nối database và API " +
                    "để thực hiện nghiệp vụ đầy đủ."
        )
    }

    // =========================================
    // 30. XỬ LÝ NÚT BACK
    // =========================================


}
