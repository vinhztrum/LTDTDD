package com.nhom9.dtdd

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.nhom9.dtdd.databinding.ActivityHomeLandlordBinding

class HomeLandlordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeLandlordBinding
    private var currentScreen = Screen.OVERVIEW
    private var selectedRoomNumber: String = "203"
    private var selectedMeterRoomNumber: String = "204"
    private var currentRoomFilter: String = "ALL"
    private var currentMaintFilter: String = "OPEN"
    private var isUpdatingMeterFields = false

    private enum class Screen {
        OVERVIEW,
        ROOM_LIST,
        ROOM_DETAIL,
        METER_INDEX,
        MAINTENANCE,
        TENANT_INFO
    }

    private val meterCameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val electricNewStr = data?.getStringExtra("ELECTRIC_NEW")?.replace(".", "")?.trim() ?: "1568"
            val roomFromCam = data?.getStringExtra("ROOM_NUMBER")
            if (!roomFromCam.isNullOrBlank() && MotelRepository.getRoom(roomFromCam) != null) {
                selectedMeterRoomNumber = roomFromCam
            }
            val room = MotelRepository.getRoom(selectedMeterRoomNumber)
            val newElecVal = electricNewStr.toIntOrNull() ?: 1568
            if (room != null) {
                room.electricNew = newElecVal
                room.meterPhotoCaptured = true
                MotelRepository.save(this)
            }
            refreshMeterIndexUI()
            Toast.makeText(
                this,
                "AI đã cập nhật chỉ số điện Phòng $selectedMeterRoomNumber: ${MotelRepository.formatNumber(newElecVal)} kWh",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MotelRepository.init(this)
        binding = ActivityHomeLandlordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        NewFeatureShortcuts.install(this, binding.root, "landlord")

        setupBottomNav()
        setupOverviewScreen()
        setupRoomListScreen()
        setupRoomDetailScreen()
        setupMeterIndexScreen()
        setupMaintenanceScreen()
        setupTenantInfoScreen()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (currentScreen) {
                    Screen.ROOM_DETAIL -> showScreen(Screen.ROOM_LIST)
                    Screen.OVERVIEW -> finish()
                    else -> showScreen(Screen.OVERVIEW)
                }
            }
        })

        refreshAllUI()
        showScreen(Screen.OVERVIEW)
    }

    override fun onResume() {
        super.onResume()
        MotelRepository.init(this)
        refreshAllUI()
    }

    private fun refreshAllUI() {
        refreshOverviewUI()
        refreshRoomListUI()
        refreshRoomDetailUI()
        refreshMeterIndexUI()
        refreshMaintenanceUI()
        refreshTenantInfoUI()
    }

    private fun setupBottomNav() {
        binding.navLandlordOverview.setOnClickListener { showScreen(Screen.OVERVIEW) }
        binding.navLandlordRooms.setOnClickListener { showScreen(Screen.ROOM_LIST) }
        binding.navLandlordInvoice.setOnClickListener { showScreen(Screen.METER_INDEX) }
        binding.navLandlordMaint.setOnClickListener { showScreen(Screen.MAINTENANCE) }
        binding.navLandlordTenant.setOnClickListener { showScreen(Screen.TENANT_INFO) }
    }

    // =========================================================================
    // 1. MÀN TỔNG QUAN (OVERVIEW)
    // =========================================================================
    private fun setupOverviewScreen() {
        binding.btnLogoutLandlord.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("INITIAL_TAB", "LANDLORD")
            }
            startActivity(intent)
            finish()
        }

        binding.btnEditGlobalPrices.setOnClickListener {
            showEditGlobalPricesDialog()
        }

        binding.cardStatOccupied.setOnClickListener {
            showScreen(Screen.ROOM_LIST)
            filterRooms("ALL")
        }
        binding.cardStatEmpty.setOnClickListener {
            showScreen(Screen.ROOM_LIST)
            filterRooms("EMPTY")
        }

        binding.bannerMaintenanceAlert.setOnClickListener {
            showScreen(Screen.MAINTENANCE)
        }

        binding.tvSeeAllRooms.setOnClickListener {
            showScreen(Screen.ROOM_LIST)
            filterRooms("UNPAID")
        }

        binding.cardRemind203.setOnClickListener {
            openRoomDetail("203")
        }
        binding.cardRemind105.setOnClickListener {
            openRoomDetail("105")
        }
        binding.cardRemind302.setOnClickListener {
            openRoomDetail("302")
        }
    }

    private fun refreshOverviewUI() {
        val ePrice = MotelRepository.electricPrice
        val wPrice = MotelRepository.waterPrice
        val sFee = MotelRepository.serviceFee

        val occupiedRooms = MotelRepository.rooms.filter { it.status != "EMPTY" }
        val emptyRooms = MotelRepository.rooms.filter { it.status == "EMPTY" }
        val unpaidRooms = occupiedRooms.filter { it.status == "PENDING" || it.status == "OVERDUE" }
        val paidRooms = occupiedRooms.filter { it.status == "PAID" }

        val paidSum = paidRooms.sumOf { it.totalBill(ePrice, wPrice, sFee) }
        val totalSum = occupiedRooms.sumOf { it.totalBill(ePrice, wPrice, sFee) }

        // Cộng thêm phần nền của các phòng khác trong khu trọ (20 phòng) để khớp quy mô khu trọ
        val baseExtraPaid = 27_900_000L
        val baseExtraTotal = 33_600_000L
        val displayPaid = (paidSum + baseExtraPaid) / 1_000_000.0
        val displayTotal = (totalSum + baseExtraTotal) / 1_000_000.0
        binding.tvOverviewRevenue.text = String.format(
            java.util.Locale("vi", "VN"),
            "%.1f tr / %.1f tr",
            displayPaid,
            displayTotal
        )

        binding.tvOverviewUnpaidCount.text = "Còn ${unpaidRooms.size} phòng chưa đóng tiền"
        val totalCount = MotelRepository.rooms.size + 10
        val occupiedCount = totalCount - emptyRooms.size
        binding.tvStatOccupiedCount.text = "$occupiedCount/$totalCount"
        binding.tvStatEmptyCount.text = "${emptyRooms.size}"

        val openMaintCount = MotelRepository.maintenanceList.count { it.status != "DONE" }
        binding.tvBannerMaintCount.text = "🔧 $openMaintCount yêu cầu bảo trì đang mở ›"

        // Cập nhật 3 thẻ nhắc đóng tiền (203, 105, 302)
        bindReminderCard(
            "203",
            binding.tvRemind203Name,
            binding.tvRemind203Sub,
            binding.tvRemind203Amount
        )
        bindReminderCard(
            "105",
            binding.tvRemind105Name,
            binding.tvRemind105Sub,
            binding.tvRemind105Amount
        )
        bindReminderCard(
            "302",
            binding.tvRemind302Name,
            binding.tvRemind302Sub,
            binding.tvRemind302Amount
        )
    }

    private fun bindReminderCard(
        roomNumber: String,
        tvName: TextView,
        tvSub: TextView,
        tvAmount: TextView
    ) {
        val room = MotelRepository.getRoom(roomNumber) ?: return
        val total = room.totalBill(
            MotelRepository.electricPrice,
            MotelRepository.waterPrice,
            MotelRepository.serviceFee
        )
        tvName.text = room.tenantName.ifBlank { "Phòng $roomNumber" }
        tvSub.text = room.statusNote
        tvAmount.text = MotelRepository.formatMillionShort(total)
        when (room.status) {
            "PAID" -> {
                tvAmount.setBackgroundResource(R.drawable.bg_pill_green)
                tvAmount.setTextColor(ContextCompat.getColor(this, R.color.pill_green_text))
                tvAmount.text = "Đã đóng"
            }
            "OVERDUE" -> {
                tvAmount.setBackgroundResource(R.drawable.bg_pill_red)
                tvAmount.setTextColor(ContextCompat.getColor(this, R.color.pill_red_text))
            }
            else -> {
                tvAmount.setBackgroundResource(R.drawable.bg_pill_yellow)
                tvAmount.setTextColor(ContextCompat.getColor(this, R.color.pill_yellow_text))
            }
        }
    }

    // =========================================================================
    // 2. MÀN DANH SÁCH PHÒNG (ROOM LIST) & THÊM PHÒNG MỚI
    // =========================================================================
    private fun setupRoomListScreen() {
        binding.chipRoomAll.setOnClickListener { filterRooms("ALL") }
        binding.chipRoomUnpaid.setOnClickListener { filterRooms("UNPAID") }
        binding.chipRoomEmpty.setOnClickListener { filterRooms("EMPTY") }

        binding.roomCard101.setOnClickListener { openRoomDetail("101") }
        binding.roomCard102.setOnClickListener { openRoomDetail("102") }
        binding.roomCard105.setOnClickListener { openRoomDetail("105") }
        binding.roomCard201.setOnClickListener { openRoomDetail("201") }
        binding.roomCard202.setOnClickListener { openRoomDetail("202") }
        binding.roomCard203.setOnClickListener { openRoomDetail("203") }
        binding.roomCard301.setOnClickListener { openRoomDetail("301") }
        binding.roomCard302.setOnClickListener { openRoomDetail("302") }
        binding.roomCard303.setOnClickListener { openRoomDetail("303") }

        binding.btnAddRoom.setOnClickListener {
            showAddRoomDialog()
        }
    }

    private fun openRoomDetail(roomNumber: String) {
        selectedRoomNumber = roomNumber
        refreshRoomDetailUI()
        refreshTenantInfoUI()
        showScreen(Screen.ROOM_DETAIL)
    }

    private fun refreshRoomListUI() {
        val fixedCards = mapOf(
            "101" to Pair(binding.roomCard101, binding.tvRoom101GridStatus),
            "102" to Pair(binding.roomCard102, binding.tvRoom102GridStatus),
            "105" to Pair(binding.roomCard105, binding.tvRoom105GridStatus),
            "201" to Pair(binding.roomCard201, binding.tvRoom201GridStatus),
            "202" to Pair(binding.roomCard202, binding.tvRoom202GridStatus),
            "203" to Pair(binding.roomCard203, binding.tvRoom203GridStatus),
            "301" to Pair(binding.roomCard301, binding.tvRoom301GridStatus),
            "302" to Pair(binding.roomCard302, binding.tvRoom302GridStatus),
            "303" to Pair(binding.roomCard303, binding.tvRoom303GridStatus)
        )

        for ((num, pair) in fixedCards) {
            val room = MotelRepository.getRoom(num) ?: continue
            applyRoomGridCardStyle(pair.first, pair.second, room)
        }

        // Hiển thị các phòng khác (nếu chủ trọ thêm phòng mới hoặc phòng 204)
        binding.layoutCustomRoomsContainer.removeAllViews()
        val extraRooms = MotelRepository.rooms.filter { !fixedCards.containsKey(it.roomNumber) }
        if (extraRooms.isNotEmpty()) {
            val title = TextView(this).apply {
                text = "Phòng khác / Mới thêm"
                setTextColor(ContextCompat.getColor(this@HomeLandlordActivity, R.color.text_secondary))
                textSize = 13f
                setTypeface(null, Typeface.BOLD)
                setPadding(0, dp(4), 0, dp(8))
            }
            binding.layoutCustomRoomsContainer.addView(title)

            val rowList = extraRooms.chunked(3)
            for (chunk in rowList) {
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = dp(12) }
                    weightSum = 3f
                }
                for (i in 0 until 3) {
                    if (i < chunk.size) {
                        val r = chunk[i]
                        val card = LinearLayout(this).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(dp(12), dp(12), dp(12), dp(12))
                            layoutParams = LinearLayout.LayoutParams(0, dp(82), 1f).apply {
                                if (i < 2) marginEnd = dp(8)
                            }
                            setOnClickListener { openRoomDetail(r.roomNumber) }
                        }
                        val tvNum = TextView(this).apply {
                            text = r.roomNumber
                            setTextColor(ContextCompat.getColor(this@HomeLandlordActivity, R.color.white))
                            textSize = 18f
                            setTypeface(null, Typeface.BOLD)
                            layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                0,
                                1f
                            )
                        }
                        val tvSt = TextView(this).apply {
                            setTextColor(ContextCompat.getColor(this@HomeLandlordActivity, R.color.white))
                            textSize = 11f
                        }
                        card.addView(tvNum)
                        card.addView(tvSt)
                        applyRoomGridCardStyle(card, tvSt, r)
                        row.addView(card)
                    } else {
                        val spacer = View(this).apply {
                            layoutParams = LinearLayout.LayoutParams(0, dp(82), 1f)
                        }
                        row.addView(spacer)
                    }
                }
                binding.layoutCustomRoomsContainer.addView(row)
            }
        }

        filterRooms(currentRoomFilter)
    }

    private fun applyRoomGridCardStyle(card: LinearLayout, tvStatus: TextView, room: RoomData) {
        when (room.status) {
            "PAID" -> {
                card.setBackgroundResource(R.drawable.bg_room_paid)
                tvStatus.text = "Đã đóng"
            }
            "PENDING" -> {
                card.setBackgroundResource(R.drawable.bg_room_pending)
                tvStatus.text = "Chờ đóng"
            }
            "OVERDUE" -> {
                card.setBackgroundResource(R.drawable.bg_room_overdue)
                tvStatus.text = "Quá hạn"
            }
            else -> {
                card.setBackgroundResource(R.drawable.bg_room_empty)
                tvStatus.text = "Trống"
            }
        }
    }

    private fun filterRooms(mode: String) {
        currentRoomFilter = mode
        setChipStyle(binding.chipRoomAll, mode == "ALL")
        setChipStyle(binding.chipRoomUnpaid, mode == "UNPAID")
        setChipStyle(binding.chipRoomEmpty, mode == "EMPTY")

        val fixedCards = mapOf(
            "101" to binding.roomCard101,
            "102" to binding.roomCard102,
            "105" to binding.roomCard105,
            "201" to binding.roomCard201,
            "202" to binding.roomCard202,
            "203" to binding.roomCard203,
            "301" to binding.roomCard301,
            "302" to binding.roomCard302,
            "303" to binding.roomCard303
        )

        for ((num, view) in fixedCards) {
            val r = MotelRepository.getRoom(num) ?: continue
            val match = when (mode) {
                "ALL" -> true
                "UNPAID" -> r.status == "PENDING" || r.status == "OVERDUE"
                "EMPTY" -> r.status == "EMPTY"
                else -> true
            }
            view.alpha = if (match) 1.0f else 0.25f
        }
    }

    private fun showAddRoomDialog() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(12), dp(20), dp(4))
        }

        val edtRoomNum = createDialogInput(container, "Số phòng (VD: 103, 205, 304)", "", InputType.TYPE_CLASS_TEXT)
        val edtFloor = createDialogInput(container, "Tầng (1, 2 hoặc 3)", "2", InputType.TYPE_CLASS_NUMBER)
        val edtRent = createDialogInput(container, "Giá tiền phòng (VNĐ/tháng)", "2200000", InputType.TYPE_CLASS_NUMBER)
        val edtTenant = createDialogInput(container, "Tên khách thuê (để trống nếu phòng trống)", "", InputType.TYPE_CLASS_TEXT)
        val edtPhone = createDialogInput(container, "Số điện thoại khách", "", InputType.TYPE_CLASS_PHONE)

        AlertDialog.Builder(this)
            .setTitle("Thêm phòng trọ mới")
            .setView(container)
            .setPositiveButton("Thêm phòng") { _, _ ->
                val num = edtRoomNum.text.toString().trim()
                if (num.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập số phòng", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (MotelRepository.getRoom(num) != null) {
                    Toast.makeText(this, "Phòng $num đã tồn tại!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val floor = edtFloor.text.toString().toIntOrNull() ?: 1
                val rent = edtRent.text.toString().replace(".", "").replace(",", "").toLongOrNull() ?: 2200000L
                val tenant = edtTenant.text.toString().trim()
                val phone = edtPhone.text.toString().trim()
                val status = if (tenant.isEmpty()) "EMPTY" else "PENDING"
                val statusNote = if (tenant.isEmpty()) "Trống" else "Hạn ngày 05/10"

                val newRoom = RoomData(
                    roomNumber = num,
                    floor = floor,
                    status = status,
                    statusNote = statusNote,
                    tenantName = tenant,
                    tenantPhone = phone.ifEmpty { "0900 000 000" },
                    tenantCccd = "0340 •••• 1234",
                    tenantHometown = "Việt Nam",
                    occupants = if (tenant.isEmpty()) 0 else 1,
                    vehiclePlate = "Chưa đăng ký",
                    moveInDate = "10/2026",
                    contractPeriod = "10/2026 – 10/2028",
                    roomRent = rent,
                    deposit = rent,
                    electricOld = 100,
                    electricNew = 100,
                    waterOld = 10,
                    waterNew = 10
                )
                MotelRepository.rooms.add(newRoom)
                MotelRepository.save(this)
                refreshAllUI()
                Toast.makeText(this, "Đã thêm Phòng $num (Giá ${MotelRepository.formatCurrency(rent)})", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // =========================================================================
    // 3. MÀN CHI TIẾT PHÒNG & CHỈNH SỬA GIÁ TIỀN TRỌ
    // =========================================================================
    private fun setupRoomDetailScreen() {
        binding.btnBackFromDetail.setOnClickListener {
            showScreen(Screen.ROOM_LIST)
        }

        // Nút "✎ Sửa giá phòng" trên góc phải hoặc bấm trực tiếp vào bảng hóa đơn
        binding.btnEditRoomPrice.setOnClickListener {
            showEditRoomPriceDialog()
        }
        binding.cardDetailBillTable.setOnClickListener {
            showEditRoomPriceDialog()
        }

        binding.cardDetailTenant.setOnClickListener {
            val room = MotelRepository.getRoom(selectedRoomNumber) ?: return@setOnClickListener
            if (room.status == "EMPTY") {
                showEditTenantProfileDialog()
            } else {
                refreshTenantInfoUI()
                showScreen(Screen.TENANT_INFO)
            }
        }

        binding.btnMarkPaid.setOnClickListener {
            val room = MotelRepository.getRoom(selectedRoomNumber) ?: return@setOnClickListener
            if (room.status == "EMPTY") {
                showEditTenantProfileDialog()
                return@setOnClickListener
            }
            if (room.status == "PAID") {
                room.status = "OVERDUE"
                room.statusNote = "Quá hạn 4 ngày"
                Toast.makeText(this, "Đã chuyển Phòng ${room.roomNumber} về trạng thái Chưa đóng", Toast.LENGTH_SHORT).show()
            } else {
                room.status = "PAID"
                room.statusNote = "Đã đóng"
                Toast.makeText(this, "Đã xác nhận nhận tiền Phòng ${room.roomNumber}!", Toast.LENGTH_SHORT).show()
            }
            MotelRepository.save(this)
            refreshAllUI()
        }

        binding.btnRemindZalo.setOnClickListener {
            val room = MotelRepository.getRoom(selectedRoomNumber) ?: return@setOnClickListener
            if (room.status == "EMPTY") {
                showEditRoomPriceDialog()
            } else {
                val total = MotelRepository.formatCurrency(
                    room.totalBill(
                        MotelRepository.electricPrice,
                        MotelRepository.waterPrice,
                        MotelRepository.serviceFee
                    )
                )
                Toast.makeText(
                    this,
                    "Đã gửi nhắc đóng tiền ($total) qua Zalo cho ${room.tenantName} (Phòng ${room.roomNumber})",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun refreshRoomDetailUI() {
        val room = MotelRepository.getRoom(selectedRoomNumber) ?: MotelRepository.rooms.firstOrNull() ?: return
        binding.btnBackFromDetail.text = "‹ Phòng ${room.roomNumber}"

        if (room.status == "EMPTY") {
            binding.tvDetailTenantInitials.text = "+"
            binding.tvDetailTenantName.text = "Phòng đang trống"
            binding.tvDetailTenantSub.text = "Bấm vào đây để thêm khách thuê mới"
        } else {
            binding.tvDetailTenantInitials.text = room.initials
            binding.tvDetailTenantName.text = room.tenantName
            binding.tvDetailTenantSub.text = "${room.tenantPhone.replace(" ", "")} · Vào ở ${room.moveInDate}"
        }

        when (room.status) {
            "PAID" -> {
                binding.tvDetailBillStatus.text = "Đã đóng"
                binding.tvDetailBillStatus.setBackgroundResource(R.drawable.bg_pill_green)
                binding.tvDetailBillStatus.setTextColor(ContextCompat.getColor(this, R.color.pill_green_text))
                binding.btnMarkPaid.text = "Hoàn tác (Đánh dấu chưa đóng)"
                binding.btnRemindZalo.text = "Gửi biên lai qua Zalo"
            }
            "PENDING" -> {
                binding.tvDetailBillStatus.text = "Chờ đóng"
                binding.tvDetailBillStatus.setBackgroundResource(R.drawable.bg_pill_yellow)
                binding.tvDetailBillStatus.setTextColor(ContextCompat.getColor(this, R.color.pill_yellow_text))
                binding.btnMarkPaid.text = "Đã nhận tiền"
                binding.btnRemindZalo.text = "Gửi nhắc qua Zalo"
            }
            "OVERDUE" -> {
                binding.tvDetailBillStatus.text = "Quá hạn"
                binding.tvDetailBillStatus.setBackgroundResource(R.drawable.bg_pill_red)
                binding.tvDetailBillStatus.setTextColor(ContextCompat.getColor(this, R.color.pill_red_text))
                binding.btnMarkPaid.text = "Đã nhận tiền"
                binding.btnRemindZalo.text = "Gửi nhắc qua Zalo"
            }
            else -> {
                binding.tvDetailBillStatus.text = "Phòng trống"
                binding.tvDetailBillStatus.setBackgroundResource(R.drawable.bg_pill_blue)
                binding.tvDetailBillStatus.setTextColor(ContextCompat.getColor(this, R.color.pill_blue_text))
                binding.btnMarkPaid.text = "+ Cho thuê phòng này"
                binding.btnRemindZalo.text = "✎ Chỉnh sửa giá phòng"
            }
        }

        val ePrice = MotelRepository.electricPrice
        val wPrice = MotelRepository.waterPrice
        val sFee = MotelRepository.serviceFee
        val eUsed = room.electricUsed
        val wUsed = room.waterUsed
        val eCost = eUsed * ePrice
        val wCost = wUsed * wPrice
        val total = room.roomRent + eCost + wCost + sFee

        binding.tvDetailRoomRent.text = MotelRepository.formatCurrency(room.roomRent)
        binding.tvDetailElectricLabel.text = "Điện ($eUsed số × ${MotelRepository.formatCurrency(ePrice)})"
        binding.tvDetailElectricAmount.text = MotelRepository.formatCurrency(eCost)
        binding.tvDetailWaterLabel.text = "Nước ($wUsed khối × ${MotelRepository.formatCurrency(wPrice)})"
        binding.tvDetailWaterAmount.text = MotelRepository.formatCurrency(wCost)
        binding.tvDetailServiceAmount.text = MotelRepository.formatCurrency(sFee)
        binding.tvDetailTotalAmount.text = MotelRepository.formatCurrency(total)
    }

    /**
     * Hộp thoại cho phép Chủ trọ chỉnh sửa Giá tiền phòng, Tiền cọc, Đơn giá điện/nước/dịch vụ
     */
    private fun showEditRoomPriceDialog() {
        val room = MotelRepository.getRoom(selectedRoomNumber) ?: return

        val scroll = ScrollView(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(12), dp(20), dp(4))
        }
        scroll.addView(container)

        val edtRoomRent = createDialogInput(
            container,
            "Giá tiền phòng ${room.roomNumber} (VNĐ/tháng)",
            room.roomRent.toString(),
            InputType.TYPE_CLASS_NUMBER
        )
        val edtDeposit = createDialogInput(
            container,
            "Tiền đặt cọc (VNĐ)",
            room.deposit.toString(),
            InputType.TYPE_CLASS_NUMBER
        )
        val edtElectricRate = createDialogInput(
            container,
            "Đơn giá điện (VNĐ/kWh)",
            MotelRepository.electricPrice.toString(),
            InputType.TYPE_CLASS_NUMBER
        )
        val edtWaterRate = createDialogInput(
            container,
            "Đơn giá nước (VNĐ/m³)",
            MotelRepository.waterPrice.toString(),
            InputType.TYPE_CLASS_NUMBER
        )
        val edtServiceFee = createDialogInput(
            container,
            "Phí Internet + rác (VNĐ/tháng)",
            MotelRepository.serviceFee.toString(),
            InputType.TYPE_CLASS_NUMBER
        )
        val edtElecUsed = createDialogInput(
            container,
            "Số điện tiêu thụ tháng này (kWh)",
            room.electricUsed.toString(),
            InputType.TYPE_CLASS_NUMBER
        )
        val edtWaterUsed = createDialogInput(
            container,
            "Số nước tiêu thụ tháng này (m³)",
            room.waterUsed.toString(),
            InputType.TYPE_CLASS_NUMBER
        )

        AlertDialog.Builder(this)
            .setTitle("Chỉnh sửa giá Phòng ${room.roomNumber}")
            .setView(scroll)
            .setPositiveButton("Lưu thay đổi") { _, _ ->
                val newRent = edtRoomRent.text.toString().replace(".", "").replace(",", "").toLongOrNull()
                val newDeposit = edtDeposit.text.toString().replace(".", "").replace(",", "").toLongOrNull()
                val newElecRate = edtElectricRate.text.toString().replace(".", "").replace(",", "").toLongOrNull()
                val newWaterRate = edtWaterRate.text.toString().replace(".", "").replace(",", "").toLongOrNull()
                val newService = edtServiceFee.text.toString().replace(".", "").replace(",", "").toLongOrNull()
                val newElecUsed = edtElecUsed.text.toString().toIntOrNull()
                val newWaterUsed = edtWaterUsed.text.toString().toIntOrNull()

                if (newRent != null && newRent > 0) room.roomRent = newRent
                if (newDeposit != null && newDeposit >= 0) room.deposit = newDeposit
                if (newElecRate != null && newElecRate >= 0) MotelRepository.electricPrice = newElecRate
                if (newWaterRate != null && newWaterRate >= 0) MotelRepository.waterPrice = newWaterRate
                if (newService != null && newService >= 0) MotelRepository.serviceFee = newService
                if (newElecUsed != null && newElecUsed >= 0) room.electricNew = room.electricOld + newElecUsed
                if (newWaterUsed != null && newWaterUsed >= 0) room.waterNew = room.waterOld + newWaterUsed

                MotelRepository.save(this)
                refreshAllUI()

                val updatedTotal = MotelRepository.formatCurrency(
                    room.totalBill(
                        MotelRepository.electricPrice,
                        MotelRepository.waterPrice,
                        MotelRepository.serviceFee
                    )
                )
                Toast.makeText(
                    this,
                    "Đã cập nhật giá Phòng ${room.roomNumber}! Tổng hóa đơn mới: $updatedTotal",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    /**
     * Hộp thoại chỉnh sửa Bảng giá chung cho toàn khu trọ
     */
    private fun showEditGlobalPricesDialog() {
        val scroll = ScrollView(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(12), dp(20), dp(4))
        }
        scroll.addView(container)

        val edtElec = createDialogInput(
            container,
            "Đơn giá điện chung (VNĐ/số)",
            MotelRepository.electricPrice.toString(),
            InputType.TYPE_CLASS_NUMBER
        )
        val edtWater = createDialogInput(
            container,
            "Đơn giá nước chung (VNĐ/khối)",
            MotelRepository.waterPrice.toString(),
            InputType.TYPE_CLASS_NUMBER
        )
        val edtService = createDialogInput(
            container,
            "Phí Internet + rác chung (VNĐ/tháng)",
            MotelRepository.serviceFee.toString(),
            InputType.TYPE_CLASS_NUMBER
        )
        val edtAllRoomRent = createDialogInput(
            container,
            "Giá phòng đồng loạt (để trống nếu giữ nguyên từng phòng)",
            "",
            InputType.TYPE_CLASS_NUMBER
        )

        AlertDialog.Builder(this)
            .setTitle("Bảng giá tiền trọ & dịch vụ")
            .setView(scroll)
            .setPositiveButton("Lưu bảng giá") { _, _ ->
                val eRate = edtElec.text.toString().replace(".", "").replace(",", "").toLongOrNull()
                val wRate = edtWater.text.toString().replace(".", "").replace(",", "").toLongOrNull()
                val sFee = edtService.text.toString().replace(".", "").replace(",", "").toLongOrNull()
                val bulkRent = edtAllRoomRent.text.toString().replace(".", "").replace(",", "").toLongOrNull()

                if (eRate != null && eRate >= 0) MotelRepository.electricPrice = eRate
                if (wRate != null && wRate >= 0) MotelRepository.waterPrice = wRate
                if (sFee != null && sFee >= 0) MotelRepository.serviceFee = sFee
                if (bulkRent != null && bulkRent > 0) {
                    MotelRepository.rooms.forEach { it.roomRent = bulkRent }
                }

                MotelRepository.save(this)
                refreshAllUI()
                Toast.makeText(this, "Đã lưu bảng giá mới cho khu trọ!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // =========================================================================
    // 4. MÀN GHI CHỈ SỐ (METER INDEX)
    // =========================================================================
    private fun setupMeterIndexScreen() {
        // Bấm vào thẻ Phòng để chọn phòng cần ghi chỉ số
        binding.cardIndexSelectRoom.setOnClickListener {
            val occupied = MotelRepository.rooms.filter { it.status != "EMPTY" }
            val labels = occupied.map {
                "Phòng ${it.roomNumber} (${it.tenantName}) · Điện cũ: ${it.electricOld}"
            }.toTypedArray()

            AlertDialog.Builder(this)
                .setTitle("Chọn phòng cần ghi chỉ số")
                .setItems(labels) { _, which ->
                    selectedMeterRoomNumber = occupied[which].roomNumber
                    refreshMeterIndexUI()
                }
                .show()
        }

        // Tự động tính chênh lệch khi nhập số điện mới / nước mới
        binding.edtElectricNew.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdatingMeterFields) return
                val room = MotelRepository.getRoom(selectedMeterRoomNumber) ?: return
                val clean = s?.toString()?.replace(".", "")?.replace(",", "")?.trim() ?: ""
                val newVal = clean.toIntOrNull() ?: room.electricOld
                val diff = (newVal - room.electricOld).coerceAtLeast(0)
                binding.tvElectricDiff.text = "+$diff"
            }
        })

        binding.edtWaterNew.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdatingMeterFields) return
                val room = MotelRepository.getRoom(selectedMeterRoomNumber) ?: return
                val clean = s?.toString()?.replace(".", "")?.replace(",", "")?.trim() ?: ""
                val newVal = clean.toIntOrNull() ?: room.waterOld
                val diff = (newVal - room.waterOld).coerceAtLeast(0)
                binding.tvWaterDiff.text = "+$diff"
            }
        })

        val openCameraListener = View.OnClickListener {
            val intent = Intent(this, MeterCameraActivity::class.java)
            meterCameraLauncher.launch(intent)
        }
        binding.cardCaptureMeter.setOnClickListener(openCameraListener)
        binding.btnOpenCameraAi.setOnClickListener(openCameraListener)

        binding.btnSaveMeter.setOnClickListener {
            val room = MotelRepository.getRoom(selectedMeterRoomNumber) ?: return@setOnClickListener
            val elecNew = binding.edtElectricNew.text.toString().replace(".", "").trim().toIntOrNull() ?: room.electricNew
            val waterNew = binding.edtWaterNew.text.toString().replace(".", "").trim().toIntOrNull() ?: room.waterNew

            if (elecNew < room.electricOld || waterNew < room.waterOld) {
                Toast.makeText(this, "Chỉ số mới không được nhỏ hơn chỉ số cũ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            room.electricNew = elecNew
            room.waterNew = waterNew
            if (MotelRepository.meterRecordedCount < 18) {
                MotelRepository.meterRecordedCount++
            }
            MotelRepository.save(this)
            refreshAllUI()

            val total = MotelRepository.formatCurrency(
                room.totalBill(
                    MotelRepository.electricPrice,
                    MotelRepository.waterPrice,
                    MotelRepository.serviceFee
                )
            )
            Toast.makeText(
                this,
                "Đã lưu chỉ số Phòng ${room.roomNumber}! Hóa đơn tạm tính: $total",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.btnCreateAllInvoices.setOnClickListener {
            val occupied = MotelRepository.rooms.filter { it.status != "EMPTY" }
            val totalSum = occupied.sumOf {
                it.totalBill(
                    MotelRepository.electricPrice,
                    MotelRepository.waterPrice,
                    MotelRepository.serviceFee
                )
            }
            Toast.makeText(
                this,
                "Đã chốt & phát hành hóa đơn tháng 10 cho ${occupied.size} phòng (Tổng: ${MotelRepository.formatCurrency(totalSum)})",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun refreshMeterIndexUI() {
        val room = MotelRepository.getRoom(selectedMeterRoomNumber)
            ?: MotelRepository.getRoom("204")
            ?: MotelRepository.rooms.firstOrNull()
            ?: return

        binding.tvIndexProgressSubtitle.text = "Tháng 10 · Đã ghi ${MotelRepository.meterRecordedCount}/18 phòng"
        val lp = binding.viewIndexProgressFill.layoutParams as? LinearLayout.LayoutParams
        if (lp != null) {
            lp.weight = MotelRepository.meterRecordedCount.toFloat().coerceIn(1f, 18f)
            binding.viewIndexProgressFill.layoutParams = lp
        }

        binding.tvIndexRoomBadge.text = room.roomNumber
        binding.tvIndexRoomTitle.text = "Phòng ${room.roomNumber} · ${room.tenantName}"
        binding.tvIndexRoomOldVal.text =
            "Cũ: điện ${MotelRepository.formatNumber(room.electricOld)} · nước ${room.waterOld}"

        isUpdatingMeterFields = true
        binding.edtElectricNew.setText(MotelRepository.formatNumber(room.electricNew))
        binding.edtWaterNew.setText(room.waterNew.toString())
        binding.tvElectricDiff.text = "+${room.electricUsed}"
        binding.tvWaterDiff.text = "+${room.waterUsed}"
        isUpdatingMeterFields = false

        if (room.meterPhotoCaptured) {
            binding.tvMeterPhotoStatus.text = "Đã chụp công tơ (${MotelRepository.formatNumber(room.electricNew)} kWh)"
            binding.tvMeterPhotoStatus.setTextColor(ContextCompat.getColor(this, R.color.primary_green))
        } else {
            binding.tvMeterPhotoStatus.text = "Chụp ảnh để lưu lại"
            binding.tvMeterPhotoStatus.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        }
    }

    // =========================================================================
    // 5. MÀN BẢO TRÌ (MAINTENANCE)
    // =========================================================================
    private fun setupMaintenanceScreen() {
        binding.chipMaintOpen.setOnClickListener { filterMaintenance("OPEN") }
        binding.chipMaintFixing.setOnClickListener { filterMaintenance("FIXING") }
        binding.chipMaintDone.setOnClickListener { filterMaintenance("DONE") }
    }

    private fun filterMaintenance(mode: String) {
        currentMaintFilter = mode
        setChipStyle(binding.chipMaintOpen, mode == "OPEN")
        setChipStyle(binding.chipMaintFixing, mode == "FIXING")
        setChipStyle(binding.chipMaintDone, mode == "DONE")
        refreshMaintenanceUI()
    }

    private fun refreshMaintenanceUI() {
        val openCount = MotelRepository.maintenanceList.count { it.status != "DONE" }
        binding.tvMaintSubtitleCount.text = "$openCount yêu cầu đang mở · Bấm vào thẻ để cập nhật"

        binding.layoutMaintCardsContainer.removeAllViews()
        val filtered = MotelRepository.maintenanceList.filter { item ->
            when (currentMaintFilter) {
                "OPEN" -> item.status != "DONE"
                "FIXING" -> item.status == "FIXING"
                "DONE" -> item.status == "DONE"
                else -> true
            }
        }

        for (item in filtered) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.bg_card_input)
                setPadding(dp(16), dp(16), dp(16), dp(16))
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(10) }
                setOnClickListener { showUpdateMaintenanceDialog(item) }
            }

            val topRow = RelativeLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            val tvTitle = TextView(this).apply {
                text = "Phòng ${item.roomNumber} · ${item.title}"
                setTextColor(ContextCompat.getColor(this@HomeLandlordActivity, R.color.text_primary))
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
                layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(RelativeLayout.ALIGN_PARENT_START)
                    addRule(RelativeLayout.CENTER_VERTICAL)
                }
            }

            val tvBadge = TextView(this).apply {
                setPadding(dp(10), dp(3), dp(10), dp(3))
                textSize = 12f
                setTypeface(null, Typeface.BOLD)
                layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(RelativeLayout.ALIGN_PARENT_END)
                    addRule(RelativeLayout.CENTER_VERTICAL)
                }
            }

            when (item.status) {
                "URGENT" -> {
                    tvBadge.text = "Khẩn cấp"
                    tvBadge.setBackgroundResource(R.drawable.bg_pill_red)
                    tvBadge.setTextColor(ContextCompat.getColor(this, R.color.pill_red_text))
                }
                "NEW" -> {
                    tvBadge.text = "Mới"
                    tvBadge.setBackgroundResource(R.drawable.bg_pill_yellow)
                    tvBadge.setTextColor(ContextCompat.getColor(this, R.color.pill_yellow_text))
                }
                "FIXING" -> {
                    tvBadge.text = "Đang sửa"
                    tvBadge.setBackgroundResource(R.drawable.bg_pill_blue)
                    tvBadge.setTextColor(ContextCompat.getColor(this, R.color.pill_blue_text))
                }
                else -> {
                    tvBadge.text = "Xong"
                    tvBadge.setBackgroundResource(R.drawable.bg_pill_green)
                    tvBadge.setTextColor(ContextCompat.getColor(this, R.color.pill_green_text))
                }
            }

            topRow.addView(tvTitle)
            topRow.addView(tvBadge)
            card.addView(topRow)

            val tvDesc = TextView(this).apply {
                text = item.description
                setTextColor(ContextCompat.getColor(this@HomeLandlordActivity, R.color.text_secondary))
                textSize = 13f
                setPadding(0, dp(6), 0, 0)
            }
            card.addView(tvDesc)

            if (item.hasPhoto) {
                val photoBox = FrameLayout(this).apply {
                    setBackgroundResource(R.drawable.bg_photo_placeholder)
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(56)
                    ).apply { topMargin = dp(10) }
                }
                val photoLabel = TextView(this).apply {
                    text = "Ảnh khách gửi (${item.category})"
                    setTextColor(ContextCompat.getColor(this@HomeLandlordActivity, R.color.text_secondary))
                    textSize = 12f
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER
                    )
                }
                photoBox.addView(photoLabel)
                card.addView(photoBox)
            }

            binding.layoutMaintCardsContainer.addView(card)
        }
    }

    private fun showUpdateMaintenanceDialog(item: MaintenanceItem) {
        val options = arrayOf(
            "Chuyển sang: Đang sửa (Hẹn thợ xử lý)",
            "Chuyển sang: Xong (Đã hoàn thành)",
            "Chuyển sang: Khẩn cấp",
            "Chỉnh sửa ghi chú bảo trì"
        )
        AlertDialog.Builder(this)
            .setTitle("Phòng ${item.roomNumber} · ${item.title}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        item.status = "FIXING"
                        MotelRepository.save(this)
                        refreshAllUI()
                        Toast.makeText(this, "Đã chuyển sang trạng thái Đang sửa", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        item.status = "DONE"
                        MotelRepository.save(this)
                        refreshAllUI()
                        Toast.makeText(this, "Đã đánh dấu hoàn thành bảo trì!", Toast.LENGTH_SHORT).show()
                    }
                    2 -> {
                        item.status = "URGENT"
                        MotelRepository.save(this)
                        refreshAllUI()
                    }
                    3 -> {
                        val container = LinearLayout(this).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(dp(20), dp(12), dp(20), dp(4))
                        }
                        val edtNote = createDialogInput(
                            container,
                            "Ghi chú tiến độ / chi phí",
                            item.description,
                            InputType.TYPE_CLASS_TEXT
                        )
                        AlertDialog.Builder(this)
                            .setTitle("Cập nhật ghi chú")
                            .setView(container)
                            .setPositiveButton("Lưu") { _, _ ->
                                item.description = edtNote.text.toString().trim()
                                MotelRepository.save(this)
                                refreshAllUI()
                            }
                            .setNegativeButton("Hủy", null)
                            .show()
                    }
                }
            }
            .show()
    }

    // =========================================================================
    // 6. MÀN HỒ SƠ KHÁCH THUÊ & CHỈNH SỬA HỒ SƠ + GIÁ PHÒNG
    // =========================================================================
    private fun setupTenantInfoScreen() {
        binding.btnBackFromTenantInfo.setOnClickListener {
            showScreen(Screen.OVERVIEW)
        }

        // Bấm vào tên/phòng để đổi xem hồ sơ của các phòng khác
        val switchTenantListener = View.OnClickListener {
            val occupied = MotelRepository.rooms.filter { it.status != "EMPTY" }
            val labels = occupied.map { "Phòng ${it.roomNumber} · ${it.tenantName}" }.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle("Chọn khách thuê")
                .setItems(labels) { _, which ->
                    selectedRoomNumber = occupied[which].roomNumber
                    refreshTenantInfoUI()
                    refreshRoomDetailUI()
                }
                .show()
        }
        binding.tvTenantProfileRoomSub.setOnClickListener(switchTenantListener)
        binding.tvTenantProfileAvatar.setOnClickListener(switchTenantListener)

        // Nút "✎ Sửa hồ sơ & Giá" hoặc bấm vào bảng hợp đồng
        binding.btnEditTenantProfile.setOnClickListener {
            showEditTenantProfileDialog()
        }
        binding.cardTenantContractBox.setOnClickListener {
            showEditTenantProfileDialog()
        }

        binding.btnCallTenant.setOnClickListener {
            val room = MotelRepository.getRoom(selectedRoomNumber)
            val phone = room?.tenantPhone?.replace(" ", "") ?: "0912345678"
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            startActivity(dialIntent)
        }

        binding.btnZaloTenant.setOnClickListener {
            val room = MotelRepository.getRoom(selectedRoomNumber)
            Toast.makeText(
                this,
                "Đang mở Zalo nhắn tin với ${room?.tenantName ?: "Khách thuê"}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun refreshTenantInfoUI() {
        var room = MotelRepository.getRoom(selectedRoomNumber)
        if (room == null || room.status == "EMPTY") {
            room = MotelRepository.getRoom("203") ?: MotelRepository.rooms.firstOrNull { it.status != "EMPTY" }
        }
        if (room == null) return

        binding.tvTenantProfileAvatar.text = room.initials
        binding.tvTenantProfileName.text = room.tenantName
        binding.tvTenantProfileRoomSub.text = "Phòng ${room.roomNumber} · Đang thuê ▾"

        binding.tvProfilePhone.text = room.tenantPhone
        binding.tvProfileCccd.text = room.tenantCccd
        binding.tvProfileHometown.text = room.tenantHometown
        binding.tvProfileOccupants.text = "${room.occupants} người"
        binding.tvProfileVehicle.text = room.vehiclePlate

        binding.tvProfileContractPeriod.text = room.contractPeriod
        binding.tvProfileDeposit.text = MotelRepository.formatCurrency(room.deposit)
        binding.tvProfileRoomRent.text = "${MotelRepository.formatCurrency(room.roomRent)}/tháng"
    }

    private fun showEditTenantProfileDialog() {
        val room = MotelRepository.getRoom(selectedRoomNumber) ?: return

        val scroll = ScrollView(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(12), dp(20), dp(4))
        }
        scroll.addView(container)

        val edtName = createDialogInput(container, "Họ và tên khách thuê", room.tenantName, InputType.TYPE_CLASS_TEXT)
        val edtPhone = createDialogInput(container, "Số điện thoại", room.tenantPhone, InputType.TYPE_CLASS_PHONE)
        val edtCccd = createDialogInput(container, "Số CCCD", room.tenantCccd, InputType.TYPE_CLASS_TEXT)
        val edtHometown = createDialogInput(container, "Quê quán", room.tenantHometown, InputType.TYPE_CLASS_TEXT)
        val edtOccupants = createDialogInput(
            container,
            "Số người ở",
            room.occupants.coerceAtLeast(1).toString(),
            InputType.TYPE_CLASS_NUMBER
        )
        val edtVehicle = createDialogInput(container, "Biển số xe", room.vehiclePlate, InputType.TYPE_CLASS_TEXT)
        val edtContract = createDialogInput(
            container,
            "Thời hạn hợp đồng",
            room.contractPeriod.ifBlank { "10/2026 – 10/2028" },
            InputType.TYPE_CLASS_TEXT
        )
        val edtRent = createDialogInput(
            container,
            "Giá phòng (VNĐ/tháng)",
            room.roomRent.toString(),
            InputType.TYPE_CLASS_NUMBER
        )
        val edtDeposit = createDialogInput(
            container,
            "Tiền cọc (VNĐ)",
            room.deposit.toString(),
            InputType.TYPE_CLASS_NUMBER
        )

        AlertDialog.Builder(this)
            .setTitle("Hồ sơ & Giá thuê Phòng ${room.roomNumber}")
            .setView(scroll)
            .setPositiveButton("Lưu") { _, _ ->
                val name = edtName.text.toString().trim()
                if (name.isNotEmpty()) {
                    room.tenantName = name
                    if (room.status == "EMPTY") {
                        room.status = "PENDING"
                        room.statusNote = "Hạn ngày 05/10"
                        room.moveInDate = "10/2026"
                    }
                }
                room.tenantPhone = edtPhone.text.toString().trim()
                room.tenantCccd = edtCccd.text.toString().trim()
                room.tenantHometown = edtHometown.text.toString().trim()
                room.occupants = edtOccupants.text.toString().toIntOrNull() ?: 1
                room.vehiclePlate = edtVehicle.text.toString().trim()
                room.contractPeriod = edtContract.text.toString().trim()

                val rent = edtRent.text.toString().replace(".", "").replace(",", "").toLongOrNull()
                val deposit = edtDeposit.text.toString().replace(".", "").replace(",", "").toLongOrNull()
                if (rent != null && rent > 0) room.roomRent = rent
                if (deposit != null && deposit >= 0) room.deposit = deposit

                MotelRepository.save(this)
                refreshAllUI()
                Toast.makeText(
                    this,
                    "Đã lưu thông tin & giá thuê Phòng ${room.roomNumber} (${MotelRepository.formatCurrency(room.roomRent)}/tháng)",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // =========================================================================
    // HELPER UI FUNCTIONS
    // =========================================================================
    private fun createDialogInput(
        parent: LinearLayout,
        label: String,
        initialValue: String,
        inputType: Int
    ): EditText {
        val tv = TextView(this).apply {
            text = label
            setTextColor(ContextCompat.getColor(this@HomeLandlordActivity, R.color.text_secondary))
            textSize = 12f
            setPadding(0, dp(8), 0, dp(2))
        }
        val edt = EditText(this).apply {
            setText(initialValue)
            this.inputType = inputType
            setTextColor(ContextCompat.getColor(this@HomeLandlordActivity, R.color.text_primary))
            textSize = 15f
        }
        parent.addView(tv)
        parent.addView(edt)
        return edt
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun setChipStyle(chip: TextView, active: Boolean) {
        if (active) {
            chip.setBackgroundResource(R.drawable.bg_chip_active)
            chip.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_inactive)
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        }
    }

    private fun showScreen(screen: Screen) {
        currentScreen = screen

        binding.screenOverview.visibility = if (screen == Screen.OVERVIEW) View.VISIBLE else View.GONE
        binding.screenRoomList.visibility = if (screen == Screen.ROOM_LIST) View.VISIBLE else View.GONE
        binding.screenRoomDetail.visibility = if (screen == Screen.ROOM_DETAIL) View.VISIBLE else View.GONE
        binding.screenMeterIndex.visibility = if (screen == Screen.METER_INDEX) View.VISIBLE else View.GONE
        binding.screenMaintenance.visibility = if (screen == Screen.MAINTENANCE) View.VISIBLE else View.GONE
        binding.screenTenantInfo.visibility = if (screen == Screen.TENANT_INFO) View.VISIBLE else View.GONE

        val activeTab = when (screen) {
            Screen.OVERVIEW -> 0
            Screen.ROOM_LIST, Screen.ROOM_DETAIL -> 1
            Screen.METER_INDEX -> 2
            Screen.MAINTENANCE -> 3
            Screen.TENANT_INFO -> 4
        }

        updateNavItem(binding.iconNavOverview, binding.tvNavOverview, activeTab == 0)
        updateNavItem(binding.iconNavRooms, binding.tvNavRooms, activeTab == 1)
        updateNavItem(binding.iconNavInvoice, binding.tvNavInvoice, activeTab == 2)
        updateNavItem(binding.iconNavMaint, binding.tvNavMaint, activeTab == 3)
        updateNavItem(binding.iconNavTenant, binding.tvNavTenant, activeTab == 4)
    }

    private fun updateNavItem(iconView: View, labelView: TextView, active: Boolean) {
        if (active) {
            iconView.setBackgroundResource(R.drawable.bg_nav_icon_active)
            labelView.setTextColor(ContextCompat.getColor(this, R.color.primary_green))
            labelView.setTypeface(null, Typeface.BOLD)
        } else {
            iconView.setBackgroundResource(R.drawable.bg_nav_icon_inactive)
            labelView.setTextColor(ContextCompat.getColor(this, R.color.text_nav_inactive))
            labelView.setTypeface(null, Typeface.NORMAL)
        }
    }
}
