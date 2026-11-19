package com.nhom9.dtdd

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
import androidx.appcompat.app.AppCompatActivity

/** Independent interactive demo. No original screens, repositories or layouts are changed. */
class NewFeatureActivity : AppCompatActivity() {
    private val forest = Color.parseColor("#0F4C47")
    private val ink = Color.parseColor("#173B37")
    private val muted = Color.parseColor("#71847F")
    private val canvas = Color.parseColor("#F4F8F6")
    private val mint = Color.parseColor("#E7F2EE")
    private val gold = Color.parseColor("#E9B956")
    private lateinit var body: LinearLayout
    private var role = "tenant"
    private var page = "ranking"
    private var selectedParcel = 0
    private var parcelFilter = "Tất cả"
    private var parcelQuery = ""
    private var rankingMonth = 11
    private var selectedRoom = 0
    private var selectedRental = 0
    private var selectedPayment = 0
    private var selectedAsset = 0
    private var inventoryRoom = "203"
    private var tenantConfirmed = false
    private val received = mutableSetOf(3, 4)
    private val parcels = listOf(
        Parcel("PKG-1025", "Shopee Express", "Tai nghe Bluetooth", "18/11 · 09:20", "Kệ A2", "Phòng 203", "Nguyễn Văn Vinh"),
        Parcel("PKG-1026", "Giao Hàng Nhanh", "Sách và dụng cụ học tập", "18/11 · 11:45", "Kệ B1", "Phòng 105", "Trần Minh Anh"),
        Parcel("PKG-1027", "J&T Express", "Áo khoác mùa đông", "19/11 · 08:15", "Kệ A1", "Phòng 302", "Lê Thảo Nhi"),
        Parcel("PKG-1028", "Viettel Post", "Đồ gia dụng", "16/11 · 14:30", "Đã giao", "Phòng 201", "Phạm Hoàng Long"),
        Parcel("PKG-1029", "Giao Hàng Tiết Kiệm", "Phụ kiện điện thoại", "15/11 · 10:00", "Đã giao", "Phòng 101", "Đỗ Hồng Hà")
    )
    private data class Parcel(val id: String, val carrier: String, val product: String, val time: String, val shelf: String, val room: String, val recipient: String)
    private data class Room(val name: String, val score: Int, val saving: Int, val electricity: Int, val water: Int)
    private val rooms = listOf(Room("203", 96, 18, 18, 2), Room("105", 91, 15, 15, 2), Room("302", 87, 12, 12, 1), Room("201", 79, 9, 9, 1), Room("101", 74, 7, 7, 1))
    private val rentals = listOf("Phòng 203" to "01/09/2026 – hiện tại", "Phòng 105" to "01/01/2026 – 31/08/2026")
    private val payments = listOf(Triple("10/2026", "2.980.000đ", intArrayOf(2500000, 300000, 120000, 60000)), Triple("09/2026", "2.910.000đ", intArrayOf(2500000, 250000, 100000, 60000)), Triple("08/2026", "2.870.000đ", intArrayOf(2500000, 210000, 100000, 60000)))
    private val assets = listOf("Giường gỗ", "Tủ quần áo", "Máy lạnh", "Bàn học", "Ghế", "Chìa khóa", "Đèn phòng", "Quạt")
    private val amounts = listOf("1 chiếc", "1 chiếc", "1 bộ", "1 chiếc", "2 chiếc", "2 chiếc", "2 chiếc", "1 chiếc")
    private val conditions = mutableMapOf<String, MutableList<String>>()
    private val notes = mutableMapOf<String, MutableList<String>>()
    private val conditionOptions = arrayOf("Tốt", "Cần sửa", "Hư hỏng")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        role = intent.getStringExtra("role") ?: "tenant"
        page = intent.getStringExtra("page") ?: "ranking"
        render()
    }
    private fun dp(n: Int) = (n * resources.displayMetrics.density + .5f).toInt()
    private fun bg(color: Int, radius: Int = 16, stroke: Int? = null): GradientDrawable = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat()
        if (stroke != null) setStroke(dp(1), stroke)
    }
    private fun column(pad: Int = 0) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        if (pad > 0) setPadding(dp(pad), dp(pad), dp(pad), dp(pad))
    }
    private fun row() = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
    private fun text(s: String, size: Float = 14f, bold: Boolean = false, color: Int = ink) = TextView(this).apply {
        this.text = s; textSize = size; setTextColor(color)
        if (bold) setTypeface(null, Typeface.BOLD)
        includeFontPadding = true
    }
    private fun gap(parent: LinearLayout, h: Int = 10) { parent.addView(View(this), LinearLayout.LayoutParams(1, dp(h))) }
    private fun card(): LinearLayout = column(16).apply {
        background = bg(Color.WHITE, 17, Color.parseColor("#E6EFEB"))
        elevation = dp(1).toFloat()
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
    }
    private fun badge(s: String, color: Int = forest, back: Int = mint): TextView = text(s, 11f, true, color).apply {
        setPadding(dp(10), dp(6), dp(10), dp(6)); background = bg(back, 20)
    }
    private fun addTitle(s: String, subtitle: String = "") {
        body.addView(text(s, 21f, true)); if (subtitle.isNotEmpty()) { gap(body, 5); body.addView(text(subtitle, 12f, false, muted)) }; gap(body, 16)
    }
    private fun info(label: String, value: String, parent: LinearLayout) {
        val line = row(); line.addView(text(label, 13f, false, muted), LinearLayout.LayoutParams(0, -2, 1f))
        line.addView(text(value, 13f, true), LinearLayout.LayoutParams(-2, -2)); parent.addView(line); gap(parent, 12)
    }
    private fun button(s: String, onClick: () -> Unit, secondary: Boolean = false): TextView = text(s, 14f, true, if (secondary) forest else Color.WHITE).apply {
        gravity = Gravity.CENTER; setPadding(dp(14), dp(15), dp(14), dp(15))
        background = bg(if (secondary) mint else forest, 13)
        setOnClickListener { onClick() }
    }
    private fun addButton(s: String, next: String, secondary: Boolean = false) {
        body.addView(button(s, { go(next) }, secondary), LinearLayout.LayoutParams(-1, -2)); gap(body, 12)
    }
    private fun go(target: String) { page = target; render() }
    private fun status(i: Int) = if (received.contains(i)) "Đã nhận" else "Chờ nhận"
    private fun sectionLabel(title: String, subtitle: String = "") {
        val r = row(); r.addView(text(title, 17f, true), LinearLayout.LayoutParams(0, -2, 1f))
        if (subtitle.isNotEmpty()) r.addView(text(subtitle, 11f, false, muted))
        body.addView(r); gap(body, 10)
    }
    private fun iconTile(symbol: String, tint: Int = mint): TextView = text(symbol, 24f, true, forest).apply {
        gravity = Gravity.CENTER; background = bg(tint, 13)
    }
    private fun meterBar(parent: LinearLayout, value: Int, max: Int = 100, tint: Int = forest) {
        val track = row().apply { background = bg(mint, 6) }
        val fraction = (value.toFloat() / max).coerceIn(0f, 1f)
        track.addView(View(this).apply { background = bg(tint, 6) }, LinearLayout.LayoutParams(0, dp(9), fraction.coerceAtLeast(.001f)))
        track.addView(View(this), LinearLayout.LayoutParams(0, dp(9), (1f - fraction).coerceAtLeast(.001f)))
        parent.addView(track)
    }
    private fun metric(parent: LinearLayout, icon: String, label: String, value: String, amount: Int) {
        val r = row(); r.addView(iconTile(icon), LinearLayout.LayoutParams(dp(48), dp(48)))
        val col = column().apply { setPadding(dp(12), 0, 0, 0) }
        val t = row(); t.addView(text(label, 13f, true), LinearLayout.LayoutParams(0, -2, 1f)); t.addView(text(value, 13f, true, forest))
        col.addView(t); gap(col, 9); meterBar(col, amount)
        r.addView(col, LinearLayout.LayoutParams(0, -2, 1f)); parent.addView(r); gap(parent, 18)
    }
    private fun horizontalStat(parent: LinearLayout, left: String, right: String) {
        val r = row(); r.addView(text(left, 12f, false, muted), LinearLayout.LayoutParams(0, -2, 1f))
        r.addView(text(right, 13f, true, forest)); parent.addView(r); gap(parent, 12)
    }
    private fun photoTile(icon: String, label: String, detail: String): LinearLayout = card().apply {
        val r = row(); r.addView(iconTile(icon), LinearLayout.LayoutParams(dp(58), dp(58)))
        val col = column().apply { setPadding(dp(12), 0, 0, 0) }
        col.addView(text(label, 16f, true)); gap(col, 6); col.addView(text(detail, 12f, false, muted))
        r.addView(col, LinearLayout.LayoutParams(0, -2, 1f)); addView(r)
    }
    private fun parcelCard(i: Int) {
        val p = parcels[i]; val c = card()
        val line = row(); line.addView(text("📦", 26f), LinearLayout.LayoutParams(dp(42), -2))
        val mid = column(); mid.addView(text(p.product, 15f, true)); gap(mid, 4); mid.addView(text(p.carrier + " · " + p.id, 12f, false, muted))
        line.addView(mid, LinearLayout.LayoutParams(0, -2, 1f)); line.addView(text("›", 27f, true, muted)); c.addView(line)
        gap(c, 12)
        val line2 = row(); line2.addView(badge(status(i), if (received.contains(i)) muted else forest, if (received.contains(i)) canvas else mint))
        line2.addView(text("  " + p.time, 11f, false, muted)); c.addView(line2)
        gap(c, 9); c.addView(text("📍 ${p.shelf}   •   ${p.room}", 12f, false, muted))
        c.setOnClickListener { selectedParcel = i; go("parcel_detail") }; body.addView(c)
    }
    private fun parcelList() {
        addTitle(if (role == "tenant") "Bưu kiện của bạn" else "Quản lý bưu kiện", "Theo dõi và nhận hàng ngay tại nhà trọ")
        val summary = card(); summary.background = bg(forest)
        summary.addView(text("📦  ${parcels.size} bưu kiện", 21f, true, Color.WHITE)); gap(summary, 7)
        summary.addView(text("${parcels.indices.count { !received.contains(it) }} chờ nhận     •     ${received.size} đã nhận", 13f, false, Color.WHITE)); body.addView(summary)
        val search = EditText(this).apply {
            setSingleLine(true); hint = "🔎  Tìm mã đơn, sản phẩm, phòng..."; textSize = 13f
            setPadding(dp(14), dp(10), dp(14), dp(10)); background = bg(Color.WHITE, 13)
            setText(parcelQuery); setSelection(text.length)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { parcelQuery = s.toString(); updateParcelResults() }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
        body.addView(search, LinearLayout.LayoutParams(-1, dp(48))); gap(body, 12)
        val filters = row()
        listOf("Tất cả", "Chờ nhận", "Đã nhận").forEach { f ->
            val chip = text(f, 12f, true, if (parcelFilter == f) Color.WHITE else forest).apply {
                gravity = Gravity.CENTER; setPadding(dp(10), dp(10), dp(10), dp(10)); background = bg(if (parcelFilter == f) forest else mint, 20)
                setOnClickListener { parcelFilter = f; render() }
            }
            filters.addView(chip, LinearLayout.LayoutParams(0, -2, 1f).apply { rightMargin = dp(6) })
        }
        body.addView(filters); gap(body, 16)
        parcelResultContainer = column(); body.addView(parcelResultContainer); updateParcelResults()
    }
    private var parcelResultContainer: LinearLayout? = null
    private fun updateParcelResults() {
        val container = parcelResultContainer ?: return
        container.removeAllViews()
        val matching = parcels.indices.filter { i ->
            val p = parcels[i]
            (parcelFilter == "Tất cả" || status(i) == parcelFilter) &&
                listOf(p.id, p.product, p.carrier, p.room, p.recipient).any { it.contains(parcelQuery.trim(), true) }
        }
        if (matching.isEmpty()) container.addView(text("Không tìm thấy bưu kiện phù hợp.", 14f, false, muted))
        matching.forEach { i ->
            val savedBody = body; body = container; parcelCard(i); body = savedBody
        }
    }
    private fun parcelDetail() {
        val p = parcels[selectedParcel]
        addTitle("Chi tiết ${p.id}", "Kiểm tra thông tin trước khi nhận")
        val hero = card(); hero.addView(text("📦  ${p.product}", 20f, true)); gap(hero, 8); hero.addView(badge(status(selectedParcel))); body.addView(hero)
        val c = card(); info("Đơn vị vận chuyển", p.carrier, c); info("Người nhận", p.recipient, c)
        info("Phòng", p.room, c); info("Ngày giao", p.time, c); info("Vị trí", p.shelf, c); body.addView(c)
        if (!received.contains(selectedParcel)) addButton(if (role == "tenant") "Xem mã nhận hàng  →" else "Xác nhận giao bưu kiện  →", "parcel_code")
        else { val done = card(); done.addView(text("✓  Bưu kiện đã được nhận", 15f, true, forest)); body.addView(done) }
        addButton("←  Về danh sách", "parcel_list", true)
    }
    private fun parcelCode() {
        val p = parcels[selectedParcel]; addTitle("Mã nhận bưu kiện", "Chỉ hiển thị sau khi chọn đơn hàng")
        val c = card(); c.gravity = Gravity.CENTER_HORIZONTAL
        c.addView(text("MÃ NHẬN HÀNG", 12f, true, muted)); gap(c, 14)
        val qrId = resources.getIdentifier("parcel_qr_" + p.id.takeLast(4), "drawable", packageName)
        if (qrId != 0) {
            val qr = ImageView(this).apply {
                setImageResource(qrId)
                scaleType = ImageView.ScaleType.FIT_CENTER
                contentDescription = "Mã QR nhận bưu kiện " + p.id
                setBackgroundColor(Color.WHITE)
                setPadding(dp(12), dp(12), dp(12), dp(12))
            }
            c.addView(qr, LinearLayout.LayoutParams(dp(228), dp(228)))
            gap(c, 10)
        }
        c.addView(text("Mã nhận: " + p.id.takeLast(4) + "-" + p.room.takeLast(3), 22f, true, forest)); gap(c, 10)
        c.addView(text("QR dùng để demo • Không kết nối tủ nhận hàng thật", 11f, false, muted)); body.addView(c)
        val d = card(); info("Mã đơn", p.id, d); info("Sản phẩm", p.product, d); info("Vị trí", p.shelf, d); body.addView(d)
        addButton(if (role == "tenant") "✓  Tôi đã nhận hàng" else "✓  Xác nhận đã giao hàng", "parcel_done")
    }
    private fun parcelDone() {
        received.add(selectedParcel)
        val c = card(); c.gravity = Gravity.CENTER_HORIZONTAL
        c.addView(text("✓", 60f, true, forest)); gap(c, 6)
        c.addView(text("Xác nhận thành công!", 22f, true)); gap(c, 8)
        c.addView(text("${parcels[selectedParcel].id} đã chuyển sang trạng thái Đã nhận.", 13f, false, muted)); body.addView(c)
        addButton("Về danh sách bưu kiện", "parcel_list")
    }
    private fun ranking() {
        addTitle("Bảng xếp hạng phòng xanh", "Cùng tiết kiệm điện và nước · Tháng $rankingMonth/2026")
        val months = row()
        listOf(10, 11).forEach { m ->
            months.addView(button("Tháng $m", { rankingMonth = m; render() }, rankingMonth != m), LinearLayout.LayoutParams(0, -2, 1f).apply { rightMargin = dp(8) })
        }; body.addView(months); gap(body, 16)
        val champion = rooms[3]
        val top = card().apply { background = bg(forest, 20) }
        top.addView(text("✦  BẢNG XẾP HẠNG THÁNG", 12f, true, Color.parseColor("#D6E9E2"))); gap(top, 12)
        top.addView(text("🏆  PHÒNG 201", 26f, true, Color.WHITE)); gap(top, 6)
        top.addView(text("Hạng 1 · Tiết kiệm 23%", 16f, true, Color.WHITE)); gap(top, 8)
        top.addView(text("Dẫn đầu phong trào sống xanh", 12f, false, Color.parseColor("#D6E9E2")))
        gap(top, 16); top.addView(badge("★  980 điểm xanh", forest, gold)); body.addView(top)
        sectionLabel("Thứ hạng các phòng", "Chạm để xem thành tích")
        val rankingOrder = listOf(3, 0, 1, 2, 4)
        rankingOrder.forEachIndexed { rank, roomIndex ->
            val r = rooms[roomIndex]; val score = listOf(980, 920, 870, 790, 740)[rank] - if (rankingMonth == 10) 35 else 0
            val saving = listOf(23, 18, 15, 12, 7)[rank]
            val c = card(); val line = row()
            line.addView(iconTile(listOf("🥇", "🥈", "🥉", "④", "⑤")[rank]), LinearLayout.LayoutParams(dp(48), dp(48)))
            val details = column().apply { setPadding(dp(12), 0, 0, 0) }
            details.addView(text("Phòng ${r.name}", 16f, true)); gap(details, 4)
            details.addView(text("Tiết kiệm $saving% so với tháng trước", 12f, false, muted))
            line.addView(details, LinearLayout.LayoutParams(0, -2, 1f)); line.addView(text("›", 26f, true, muted)); c.addView(line)
            gap(c, 13); horizontalStat(c, "Điểm xanh", "$score / 1000"); meterBar(c, score, 1000, if (rank == 0) gold else forest)
            c.setOnClickListener { selectedRoom = roomIndex; go("ranking_detail") }; body.addView(c)
        }
        body.addView(text("* Dữ liệu xếp hạng minh họa phục vụ demo.", 11f, false, muted))
    }
    private fun rankingDetail() {
        val r = rooms[selectedRoom]
        val rank = listOf(3, 0, 1, 2, 4).indexOf(selectedRoom) + 1
        val score = listOf(980, 920, 870, 790, 740)[rank - 1] - if (rankingMonth == 10) 35 else 0
        val saving = listOf(23, 18, 15, 12, 7)[rank - 1]
        addTitle("Thành tích phòng ${r.name}", "Hạng $rank · Tháng $rankingMonth/2026")
        val hero = card().apply { background = bg(forest, 20); gravity = Gravity.CENTER_HORIZONTAL }
        hero.addView(text(if (rank == 1) "🏆" else "🏅", 48f)); gap(hero, 9)
        hero.addView(text("$score điểm xanh", 29f, true, Color.WHITE)); gap(hero, 8)
        hero.addView(text("Tiết kiệm $saving% so với tháng trước", 13f, false, Color.WHITE)); gap(hero, 16)
        meterBar(hero, score, 1000, gold); body.addView(hero)
        sectionLabel("Chỉ số so sánh", "So với tháng trước")
        val c = card()
        metric(c, "⚡", "Điện năng", "110 → ${110 - r.electricity} kWh", 75)
        metric(c, "💧", "Nước sinh hoạt", "8 → ${8 - r.water} m³", 62)
        metric(c, "🌱", "Mức tiết kiệm", "$saving%", saving * 4)
        body.addView(c)
        val tip = card(); tip.addView(text("💡  Gợi ý sống xanh", 16f, true)); gap(tip, 9)
        tip.addView(text("Tắt thiết bị khi ra khỏi phòng, tận dụng ánh sáng tự nhiên và kiểm tra vòi nước thường xuyên.", 13f)); body.addView(tip)
        addButton("←  Về bảng xếp hạng", "ranking", true)
    }
    private fun rentalHistory() {
        addTitle("Lịch sử thuê trọ", "Các phòng bạn đã và đang thuê")
        val hero = card().apply { background = bg(forest) }
        hero.addView(text("🏠  HÀNH TRÌNH THUÊ TRỌ", 12f, true, Color.WHITE)); gap(hero, 10)
        hero.addView(text("02 phòng đã thuê", 24f, true, Color.WHITE)); gap(hero, 6)
        hero.addView(text("01 đang thuê  •  01 đã kết thúc", 12f, false, Color.WHITE)); body.addView(hero)
        sectionLabel("Các lần thuê", "Chọn phòng để xem chi tiết")
        rentals.forEachIndexed { i, (_, _) ->
            val room = if (i == 0) "203" else "105"
            val date = if (i == 0) "03/2025 – hiện tại" else "06/2024 – 02/2025"
            val c = photoTile("⌂", "Phòng $room · Nhà Trọ Xanh", date)
            gap(c, 12); c.addView(badge(if (i == 0) "● Đang thuê" else "✓ Đã kết thúc")); gap(c, 12)
            horizontalStat(c, "Hợp đồng", if (i == 0) "HD-203-2025" else "HD-105-2024")
            c.addView(text("Xem chi tiết hợp đồng  ›", 13f, true, forest))
            c.setOnClickListener { selectedRental = i; go("rental_detail") }; body.addView(c)
        }
    }
    private fun rentalDetail() {
        val i = selectedRental; val room = if (i == 0) "203" else "105"
        addTitle("Chi tiết lần thuê", "Phòng $room · ${if (i == 0) "Đang thuê" else "Đã kết thúc"}")
        val hero = photoTile("🏠", "Phòng $room · Tầng ${if (i == 0) 2 else 1}", "Nhà Trọ Xanh")
        gap(hero, 12); hero.addView(badge(if (i == 0) "Đang thuê" else "Đã kết thúc")); body.addView(hero)
        sectionLabel("Thông tin hợp đồng")
        val c = card(); info("Tên khu trọ", "Nhà Trọ Xanh", c); info("Phòng", room, c)
        info("Bắt đầu", if (i == 0) "01/03/2025" else "01/06/2024", c)
        info("Kết thúc", if (i == 0) "Đang thuê" else "28/02/2025", c)
        info("Giá thuê", if (i == 0) "2.200.000đ/tháng" else "2.000.000đ/tháng", c)
        info("Mã hợp đồng", if (i == 0) "HD-203-2025" else "HD-105-2024", c); body.addView(c)
        addButton("←  Về lịch sử thuê", "rental_history", true)
    }
    private var paymentYear = "Tất cả"
    private fun paymentHistory() {
        addTitle("Lịch sử thanh toán", "Hóa đơn đã thanh toán của phòng 203")
        val hero = card().apply { background = bg(forest) }
        hero.addView(text("✓  THANH TOÁN HOÀN TẤT", 12f, true, Color.WHITE)); gap(hero, 8)
        hero.addView(text("${payments.size} biên nhận", 26f, true, Color.WHITE)); gap(hero, 5)
        hero.addView(text("Có thể chọn từng tháng để xem chi tiết", 12f, false, Color.WHITE)); body.addView(hero)
        val filter = row()
        listOf("Tất cả", "2026", "2025").forEach { y ->
            filter.addView(button(y, { paymentYear = y; render() }, paymentYear != y), LinearLayout.LayoutParams(0, -2, 1f).apply { rightMargin = dp(7) })
        }; body.addView(filter); gap(body, 17)
        sectionLabel("Danh sách biên nhận")
        if (paymentYear == "2025") {
            val empty = card(); empty.addView(text("🗓  Chưa có biên nhận mẫu năm 2025", 14f, true)); body.addView(empty)
        } else payments.forEachIndexed { i, p ->
            val item = photoTile("₫", "Tháng ${p.first}", "Phòng 203 · Đã thanh toán")
            gap(item, 12); horizontalStat(item, "Tổng thanh toán", p.second)
            item.addView(badge("✓ Đã thanh toán")); gap(item, 10)
            item.addView(text("Xem biên nhận  ›", 13f, true, forest))
            item.setOnClickListener { selectedPayment = i; go("payment_detail") }; body.addView(item)
        }
    }
    private fun money(n: Int) = "%,dđ".format(java.util.Locale("vi", "VN"), n)
    private fun paymentDetail() {
        val p = payments[selectedPayment]
        addTitle("Biên nhận thanh toán", "Tháng ${p.first} · Phòng 203")
        val hero = card().apply { gravity = Gravity.CENTER_HORIZONTAL }
        hero.addView(text("✓", 46f, true, forest)); gap(hero, 8)
        hero.addView(badge("ĐÃ THANH TOÁN")); gap(hero, 12)
        hero.addView(text(p.second, 30f, true, forest)); gap(hero, 8)
        hero.addView(text("Nhà Trọ Xanh · Biên nhận minh họa", 12f, false, muted)); body.addView(hero)
        sectionLabel("Chi tiết thanh toán")
        val d = card()
        listOf("🏠 Tiền phòng", "⚡ Tiền điện", "💧 Tiền nước", "▦ Dịch vụ").forEachIndexed { i, name -> info(name, money(p.third[i]), d) }
        val line = View(this).apply { setBackgroundColor(mint) }; d.addView(line, LinearLayout.LayoutParams(-1, dp(2))); gap(d, 14)
        horizontalStat(d, "TỔNG CỘNG", money(p.third.sum())); body.addView(d)
        val meta = card(); info("Mã biên nhận", "BR-${p.first.replace("/", "")}-203", meta)
        info("Phương thức", "Chuyển khoản", meta); info("Trạng thái", "Đã thanh toán", meta); body.addView(meta)
        addButton("←  Về lịch sử thanh toán", "payment_history", true)
    }
    private fun conditionsForRoom() = conditions.getOrPut(inventoryRoom) { MutableList(assets.size) { "Tốt" } }
    private fun notesForRoom() = notes.getOrPut(inventoryRoom) { MutableList(assets.size) { "" } }
    private fun inventoryRooms() {
        addTitle("Kiểm kê bàn giao", "Chọn phòng để kiểm tra tài sản")
        listOf("203" to "8 tài sản • Chuẩn bị bàn giao", "105" to "8 tài sản • Chưa bàn giao").forEach { (id, detail) ->
            val c = photoTile("⌂", "Phòng $id", detail); gap(c, 12); c.addView(badge(if (id == "203") "Đang thuê" else "Phòng trống")); gap(c, 12)
            c.addView(text("Kiểm kê ngay  →", 13f, true)); c.setOnClickListener { inventoryRoom = id; go("inventory_list") }; body.addView(c)
        }
    }
    private fun inventoryList(tenant: Boolean) {
        addTitle("Đồ dùng phòng $inventoryRoom", if (tenant) "Danh sách bàn giao để khách thuê xác nhận" else "Chạm vào từng món để cập nhật tình trạng")
        val c = card(); c.background = bg(forest)
        c.addView(text("🛏  ${assets.size} hạng mục tài sản", 18f, true, Color.WHITE)); gap(c, 8)
        c.addView(text("${conditionsForRoom().count { it == "Tốt" }} tốt  •  ${conditionsForRoom().count { it != "Tốt" }} cần chú ý", 13f, false, Color.WHITE)); body.addView(c)
        assets.forEachIndexed { i, name ->
            val item = card(); val line = row(); line.addView(iconTile(listOf("🛏", "▣", "❄", "▤", "▥", "⚿", "☼", "✺")[i]), LinearLayout.LayoutParams(dp(46), dp(46))); val title = column().apply { setPadding(dp(10), 0, 0, 0) }; title.addView(text(name, 15f, true)); gap(title, 4); title.addView(text(amounts[i], 12f, false, muted)); line.addView(title, LinearLayout.LayoutParams(0, -2, 1f))
            line.addView(badge(conditionsForRoom()[i], if (conditionsForRoom()[i] == "Tốt") forest else Color.parseColor("#A85C20"), if (conditionsForRoom()[i] == "Tốt") mint else Color.parseColor("#FFF0DC")))
            item.addView(line); gap(item, 8); item.addView(text("Số lượng: ${amounts[i]}" + if (notesForRoom()[i].isNotEmpty()) "\nGhi chú: ${notesForRoom()[i]}" else "", 12f, false, muted))
            if (!tenant) { gap(item, 7); item.addView(text("Chỉnh sửa tình trạng  →", 12f, true)); item.setOnClickListener { selectedAsset = i; go("inventory_edit") } }
            body.addView(item)
        }
        if (tenant) {
            body.addView(button(if (tenantConfirmed) "✓  Đã xác nhận xem bàn giao" else "✓  Xác nhận đã xem đồ dùng", {
                tenantConfirmed = true; Toast.makeText(this, "Đã xác nhận trong phiên demo", Toast.LENGTH_SHORT).show(); render()
            }, tenantConfirmed)); gap(body, 12)
        }
        addButton("Xem biên bản bàn giao  →", "inventory_report")
    }
    private fun inventoryEdit() {
        addTitle("Cập nhật tình trạng", "Phòng $inventoryRoom · ${assets[selectedAsset]}")
        val c = photoTile("▣", assets[selectedAsset], amounts[selectedAsset]); gap(c, 16)
        c.addView(text("Chọn tình trạng", 13f, true)); gap(c, 7)
        val group = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
        conditionOptions.forEachIndexed { index, s ->
            val r = RadioButton(this).apply { text = s; textSize = 14f; buttonTintList = ColorStateList.valueOf(forest); id = View.generateViewId(); isChecked = conditionsForRoom()[selectedAsset] == s }
            group.addView(r); if (index == 0 && group.checkedRadioButtonId == -1) r.isChecked = true
        }
        c.addView(group); gap(c, 10); c.addView(text("Ghi chú", 13f, true)); gap(c, 6)
        val note = EditText(this).apply { hint = "Ví dụ: trầy nhẹ ở góc..."; textSize = 13f; setSingleLine(false); minLines = 2; setText(notesForRoom()[selectedAsset]); background = bg(canvas, 9); setPadding(dp(10), dp(8), dp(10), dp(8)) }
        c.addView(note); body.addView(c)
        body.addView(button("Lưu tình trạng", {
            val selected = group.checkedRadioButtonId
            val rb = group.findViewById<RadioButton>(selected)
            conditionsForRoom()[selectedAsset] = rb?.text?.toString() ?: "Tốt"
            notesForRoom()[selectedAsset] = note.text.toString()
            Toast.makeText(this, "Đã cập nhật tình trạng (demo)", Toast.LENGTH_SHORT).show(); go("inventory_list")
        })); gap(body, 12)
    }
    private fun inventoryReport() {
        addTitle("Biên bản bàn giao", "Tổng hợp hiện trạng phòng $inventoryRoom")
        val c = card(); c.addView(text("▣  BIÊN BẢN BÀN GIAO", 19f, true, forest)); gap(c, 6); c.addView(text("NHÀ TRỌ XANH", 12f, true, muted)); gap(c, 14)
        info("Phòng", inventoryRoom, c); info("Ngày lập", "19/11/2026", c); info("Chủ trọ", "Nguyễn Văn A", c)
        info("Khách thuê", "Nguyễn Văn Vinh", c); info("Tổng tài sản", "${assets.size} hạng mục", c); body.addView(c)
        assets.forEachIndexed { i, s ->
            val item = card(); item.addView(text("${i + 1}. $s", 14f, true)); gap(item, 5)
            item.addView(text("${amounts[i]}  •  ${conditionsForRoom()[i]}" + if (notesForRoom()[i].isNotEmpty()) "\n${notesForRoom()[i]}" else "", 12f, false, muted)); body.addView(item)
        }
        if (role == "tenant") { val c2 = card(); c2.addView(text(if (tenantConfirmed) "✓ Khách thuê đã xác nhận xem" else "Chưa xác nhận xem đồ dùng", 14f, true)); body.addView(c2) }
        addButton("←  Về danh sách tài sản", if (role == "tenant") "handover_tenant" else "inventory_list", true)
    }
    private fun titleForPage() = when (page) {
        "parcel_list" -> if (role == "tenant") "Tủ nhận hàng" else "Quản lý bưu kiện"
        "parcel_detail" -> "Chi tiết bưu kiện"; "parcel_code" -> "Mã nhận hàng"; "parcel_done" -> "Hoàn tất"
        "ranking" -> "Phòng trọ xanh"; "ranking_detail" -> "Thành tích phòng"
        "rental_history" -> "Lịch sử thuê"; "rental_detail" -> "Chi tiết thuê"
        "payment_history" -> "Lịch sử thanh toán"; "payment_detail" -> "Biên nhận"
        "inventory_rooms" -> "Chọn phòng"; "inventory_list" -> "Kiểm kê tài sản"; "inventory_edit" -> "Chỉnh sửa"
        "inventory_report" -> "Biên bản bàn giao"; "handover_tenant" -> "Đồ dùng bàn giao"
        else -> "Nhà Trọ Xanh"
    }
    private fun render() {
        parcelResultContainer = null
        val root = column().apply { setBackgroundColor(canvas) }
        val header = row().apply { setPadding(dp(16), dp(16), dp(16), dp(16)); setBackgroundColor(forest) }
        header.addView(text("‹", 32f, false, Color.WHITE).apply { setPadding(0, 0, dp(17), 0); setOnClickListener { onBackPressed() } })
        val titleCol = column(); titleCol.addView(text(titleForPage(), 19f, true, Color.WHITE))
        titleCol.addView(text(if (role == "tenant") "KHÁCH THUÊ  •  NHÀ TRỌ XANH" else "CHỦ TRỌ  •  NHÀ TRỌ XANH", 10f, false, Color.parseColor("#D5E9E2")))
        header.addView(titleCol); root.addView(header)
        val scroll = ScrollView(this).apply { isFillViewport = true }
        body = column().apply { setPadding(dp(16), dp(20), dp(16), dp(24)) }
        scroll.addView(body); root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        when (page) {
            "parcel_list" -> parcelList(); "parcel_detail" -> parcelDetail(); "parcel_code" -> parcelCode(); "parcel_done" -> parcelDone()
            "ranking" -> ranking(); "ranking_detail" -> rankingDetail()
            "rental_history" -> rentalHistory(); "rental_detail" -> rentalDetail()
            "payment_history" -> paymentHistory(); "payment_detail" -> paymentDetail()
            "inventory_rooms" -> inventoryRooms(); "inventory_list" -> inventoryList(false)
            "handover_tenant" -> { inventoryRoom = "203"; inventoryList(true) }
            "inventory_edit" -> inventoryEdit(); "inventory_report" -> inventoryReport()
        }
        gap(body, 12); body.addView(text("Dữ liệu minh họa • Thao tác lưu trong phiên chạy thử", 11f, false, muted).apply { gravity = Gravity.CENTER })
        setContentView(root)
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when (page) {
            "parcel_detail" -> go("parcel_list"); "parcel_code" -> go("parcel_detail"); "parcel_done" -> go("parcel_list")
            "ranking_detail" -> go("ranking"); "rental_detail" -> go("rental_history"); "payment_detail" -> go("payment_history")
            "inventory_list" -> go("inventory_rooms"); "inventory_edit" -> go("inventory_list")
            "inventory_report" -> go(if (role == "tenant") "handover_tenant" else "inventory_list")
            else -> finish()
        }
    }
}
