package com.nhom9.dtdd

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.nhom9.dtdd.databinding.ActivityHomeTenantBinding

class HomeTenantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeTenantBinding
    private var currentTab = Tab.HOME
    private var currentRoomNumber: String = "203"
    private var selectedCategory: String = "Điện lạnh"
    private var isUrgentPriority: Boolean = true
    private var hasAttachedPhoto: Boolean = false

    private enum class Tab {
        HOME,
        INVOICE,
        REQUEST,
        CONTRACT
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MotelRepository.init(this)
        binding = ActivityHomeTenantBinding.inflate(layoutInflater)
        setContentView(binding.root)
        NewFeatureShortcuts.install(this, binding.root, "tenant")

        val code = intent.getStringExtra("ROOM_CODE") ?: "SP203"
        val found = MotelRepository.getRoom(code)
        currentRoomNumber = found?.roomNumber ?: "203"

        setupBottomNav()
        setupHomeTab()
        setupInvoiceTab()
        setupRequestTab()
        setupContractTab()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentTab != Tab.HOME) {
                    showTab(Tab.HOME)
                } else {
                    finish()
                }
            }
        })

        refreshTenantUI()
        showTab(Tab.HOME)
    }

    override fun onResume() {
        super.onResume()
        MotelRepository.init(this)
        refreshTenantUI()
    }

    private fun refreshTenantUI() {
        val room = MotelRepository.getRoom(currentRoomNumber)
            ?: MotelRepository.getRoom("203")
            ?: MotelRepository.rooms.firstOrNull()
            ?: return

        val ePrice = MotelRepository.electricPrice
        val wPrice = MotelRepository.waterPrice
        val sFee = MotelRepository.serviceFee
        val eUsed = room.electricUsed
        val wUsed = room.waterUsed
        val eCost = eUsed * ePrice
        val wCost = wUsed * wPrice
        val total = room.roomRent + eCost + wCost + sFee
        val totalStr = MotelRepository.formatCurrency(total)

        // 1. Tab Trang chủ
        val shortName = room.tenantName.trim().split("\\s+".toRegex()).lastOrNull()?.ifBlank { "Bạn" } ?: "Vinh"
        binding.tvTenantHeaderRoom.text = "Phòng ${room.roomNumber} · Nhà Trọ Xanh"
        binding.tvTenantHeaderGreeting.text = "Chào $shortName 👋"
        binding.tvTenantBannerTotal.text = totalStr
        binding.tvTenantHomeElectricVal.text = eUsed.toString()
        binding.tvTenantHomeWaterVal.text = wUsed.toString()

        if (room.status == "PAID") {
            binding.tvTenantInvoiceDueStatus.text = "Trạng thái: Đã thanh toán tháng 10"
            binding.btnPayNow.text = "Xem chi tiết hóa đơn"
        } else {
            binding.tvTenantInvoiceDueStatus.text = "Hạn đóng: 05/10 · ${room.statusNote}"
            binding.btnPayNow.text = "Thanh toán ngay"
        }

        // 2. Tab Hóa đơn & QR
        binding.tvTenantBillRent.text = MotelRepository.formatCurrency(room.roomRent)
        binding.tvTenantBillElectricLabel.text = "Điện ($eUsed số × ${MotelRepository.formatCurrency(ePrice)})"
        binding.tvTenantBillElectricAmount.text = MotelRepository.formatCurrency(eCost)
        binding.tvTenantBillWaterLabel.text = "Nước ($wUsed khối × ${MotelRepository.formatCurrency(wPrice)})"
        binding.tvTenantBillWaterAmount.text = MotelRepository.formatCurrency(wCost)
        binding.tvTenantBillServiceAmount.text = MotelRepository.formatCurrency(sFee)
        binding.tvTenantInvoiceTotal.text = totalStr
        binding.tvTenantQrContent.text = "Vietcombank · 0123 456 789\nNội dung: P${room.roomNumber} T10"

        // 3. Tab Bảo trì
        renderMyMaintenanceRequests(room.roomNumber)

        // 4. Tab Hợp đồng
        binding.tvTenantContractSubtitle.text = "Phòng ${room.roomNumber} · Tầng ${room.floor}"
        binding.tvTenantContractRent.text = "${MotelRepository.formatCurrency(room.roomRent)}/tháng"
        binding.tvTenantContractDeposit.text = MotelRepository.formatCurrency(room.deposit)
        binding.tvTenantContractElectricRate.text = "${MotelRepository.formatCurrency(ePrice)}/số"
        binding.tvTenantContractWaterRate.text = "${MotelRepository.formatCurrency(wPrice)}/khối"
        binding.tvTenantContractOccupants.text = "${room.occupants.coerceAtLeast(1)} người"
    }

    private fun renderMyMaintenanceRequests(roomNum: String) {
        binding.layoutTenantMyRequests.removeAllViews()
        val list = MotelRepository.maintenanceList.filter { it.roomNumber == roomNum }
            .ifEmpty { MotelRepository.maintenanceList.take(2) }

        for (item in list) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setBackgroundResource(R.drawable.bg_card_input)
                setPadding(dp(14), dp(14), dp(14), dp(14))
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(10) }
            }

            val leftCol = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            val tvTitle = TextView(this).apply {
                text = item.title
                setTextColor(ContextCompat.getColor(this@HomeTenantActivity, R.color.text_primary))
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
            }
            val tvSub = TextView(this).apply {
                text = "Gửi ${item.dateSent} · ${item.category}"
                setTextColor(ContextCompat.getColor(this@HomeTenantActivity, R.color.text_secondary))
                textSize = 12f
                setPadding(0, dp(2), 0, 0)
            }
            leftCol.addView(tvTitle)
            leftCol.addView(tvSub)

            val badge = TextView(this).apply {
                setPadding(dp(12), dp(4), dp(12), dp(4))
                textSize = 12f
                setTypeface(null, Typeface.BOLD)
            }
            when (item.status) {
                "DONE" -> {
                    badge.text = "Đã xong"
                    badge.setBackgroundResource(R.drawable.bg_pill_green)
                    badge.setTextColor(ContextCompat.getColor(this, R.color.pill_green_text))
                }
                "URGENT" -> {
                    badge.text = "Đang xử lý"
                    badge.setBackgroundResource(R.drawable.bg_pill_blue)
                    badge.setTextColor(ContextCompat.getColor(this, R.color.pill_blue_text))
                }
                else -> {
                    badge.text = "Đang xử lý"
                    badge.setBackgroundResource(R.drawable.bg_pill_blue)
                    badge.setTextColor(ContextCompat.getColor(this, R.color.pill_blue_text))
                }
            }

            card.addView(leftCol)
            card.addView(badge)
            binding.layoutTenantMyRequests.addView(card)
        }
    }

    private fun setupBottomNav() {
        binding.navTenantHome.setOnClickListener { showTab(Tab.HOME) }
        binding.navTenantInvoice.setOnClickListener { showTab(Tab.INVOICE) }
        binding.navTenantRequest.setOnClickListener { showTab(Tab.REQUEST) }
        binding.navTenantContract.setOnClickListener { showTab(Tab.CONTRACT) }
    }

    private fun setupHomeTab() {
        binding.btnLogoutTenant.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("INITIAL_TAB", "TENANT")
            }
            startActivity(intent)
            finish()
        }

        binding.btnPayNow.setOnClickListener {
            showTab(Tab.INVOICE)
        }

        binding.cardTenantElectric.setOnClickListener {
            showTab(Tab.INVOICE)
        }
        binding.cardTenantWater.setOnClickListener {
            showTab(Tab.INVOICE)
        }

        binding.notifItemMaintenance.setOnClickListener {
            showTab(Tab.REQUEST)
        }

        binding.tvSeeAllNotifs.setOnClickListener {
            Toast.makeText(this, "Đang hiển thị tất cả thông báo mới nhất", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupInvoiceTab() {
        binding.btnBackFromInvoice.setOnClickListener {
            showTab(Tab.HOME)
        }

        binding.btnConfirmTransferred.setOnClickListener {
            val room = MotelRepository.getRoom(currentRoomNumber)
            if (room != null) {
                room.status = "PAID"
                room.statusNote = "Đã đóng"
                MotelRepository.save(this)
            }
            refreshTenantUI()
            Toast.makeText(
                this,
                "Đã xác nhận chuyển khoản Phòng $currentRoomNumber! Trạng thái hóa đơn đã cập nhật.",
                Toast.LENGTH_SHORT
            ).show()
            showTab(Tab.HOME)
        }

        binding.btnCopyBankAccount.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("STK", "0123456789")
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                this,
                "Đã sao chép STK 0123 456 789 (Nội dung: P$currentRoomNumber T10)",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupRequestTab() {
        binding.btnBackFromRequest.setOnClickListener {
            showTab(Tab.HOME)
        }

        val categories = arrayOf("Điện lạnh", "Điện nước", "Khóa cửa", "Nội thất", "Khác")
        binding.cardSelectCategory.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Chọn hạng mục bảo trì")
                .setItems(categories) { _, which ->
                    selectedCategory = categories[which]
                    binding.tvSelectedCategory.text = "$selectedCategory ▾"
                }
                .show()
        }

        binding.chipPriorityNormal.setOnClickListener {
            isUrgentPriority = false
            setChipStyle(binding.chipPriorityNormal, true)
            setChipStyle(binding.chipPriorityUrgent, false)
        }
        binding.chipPriorityUrgent.setOnClickListener {
            isUrgentPriority = true
            setChipStyle(binding.chipPriorityNormal, false)
            setChipStyle(binding.chipPriorityUrgent, true)
        }

        binding.boxAddMedia.setOnClickListener {
            hasAttachedPhoto = !hasAttachedPhoto
            if (hasAttachedPhoto) {
                binding.tvAddMediaLabel.text = "✓ Đã đính kèm 1 ảnh sự cố"
                binding.tvAddMediaLabel.setTextColor(ContextCompat.getColor(this, R.color.primary_green))
            } else {
                binding.tvAddMediaLabel.text = "+ Thêm ảnh / video"
                binding.tvAddMediaLabel.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            }
        }

        binding.btnSubmitRequest.setOnClickListener {
            val desc = binding.edtIssueDescription.text.toString().trim()
            if (desc.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mô tả sự cố", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val shortTitle = desc.lines().first().trimEnd(',', '.').take(32)
            val newMaint = MaintenanceItem(
                id = "m_${System.currentTimeMillis()}",
                roomNumber = currentRoomNumber,
                title = shortTitle,
                description = desc,
                category = selectedCategory,
                status = if (isUrgentPriority) "URGENT" else "NEW",
                dateSent = "Hôm nay",
                hasPhoto = hasAttachedPhoto
            )
            MotelRepository.maintenanceList.add(0, newMaint)
            MotelRepository.save(this)
            refreshTenantUI()
            Toast.makeText(
                this,
                "Đã gửi yêu cầu bảo trì tới chủ trọ!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupContractTab() {
        binding.btnViewContractPdf.setOnClickListener {
            val room = MotelRepository.getRoom(currentRoomNumber) ?: return@setOnClickListener
            val message = """
                BẢN HỢP ĐỒNG THUÊ PHÒNG TRỌ
                • Khu trọ: Nhà Trọ Xanh
                • Phòng số: ${room.roomNumber} (Tầng ${room.floor})
                • Bên thuê: ${room.tenantName}
                • SĐT: ${room.tenantPhone} · CCCD: ${room.tenantCccd}
                • Thời hạn: ${room.contractPeriod}
                • Giá thuê: ${MotelRepository.formatCurrency(room.roomRent)}/tháng
                • Tiền đặt cọc: ${MotelRepository.formatCurrency(room.deposit)}
                • Đơn giá điện: ${MotelRepository.formatCurrency(MotelRepository.electricPrice)}/kWh
                • Đơn giá nước: ${MotelRepository.formatCurrency(MotelRepository.waterPrice)}/m³
                • Phí dịch vụ (Internet + rác): ${MotelRepository.formatCurrency(MotelRepository.serviceFee)}/tháng
            """.trimIndent()

            AlertDialog.Builder(this)
                .setTitle("Hợp đồng Phòng ${room.roomNumber}")
                .setMessage(message)
                .setPositiveButton("Đóng", null)
                .show()
        }

        binding.btnContactLandlord.setOnClickListener {
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0912345678"))
            startActivity(dialIntent)
        }
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

    private fun showTab(tab: Tab) {
        currentTab = tab

        binding.screenTenantHome.visibility = if (tab == Tab.HOME) View.VISIBLE else View.GONE
        binding.screenTenantInvoice.visibility = if (tab == Tab.INVOICE) View.VISIBLE else View.GONE
        binding.screenTenantRequest.visibility = if (tab == Tab.REQUEST) View.VISIBLE else View.GONE
        binding.screenTenantContract.visibility = if (tab == Tab.CONTRACT) View.VISIBLE else View.GONE

        updateNavItem(binding.iconTenantHome, binding.tvTenantHome, tab == Tab.HOME)
        updateNavItem(binding.iconTenantInvoice, binding.tvTenantInvoice, tab == Tab.INVOICE)
        updateNavItem(binding.iconTenantRequest, binding.tvTenantRequest, tab == Tab.REQUEST)
        updateNavItem(binding.iconTenantContract, binding.tvTenantContract, tab == Tab.CONTRACT)
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
