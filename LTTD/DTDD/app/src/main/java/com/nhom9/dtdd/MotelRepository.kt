package com.nhom9.dtdd

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

data class RoomData(
    val roomNumber: String,
    val floor: Int,
    var status: String, // "PAID", "PENDING", "OVERDUE", "EMPTY"
    var statusNote: String, // VD: "Quá hạn 4 ngày", "Hạn ngày 05/10", "Đã đóng", "Trống"
    var tenantName: String,
    var tenantPhone: String,
    var tenantCccd: String,
    var tenantHometown: String,
    var occupants: Int,
    var vehiclePlate: String,
    var moveInDate: String,
    var contractPeriod: String,
    var roomRent: Long,      // Giá phòng (VNĐ/tháng)
    var deposit: Long,       // Tiền cọc (VNĐ)
    var electricOld: Int,
    var electricNew: Int,
    var waterOld: Int,
    var waterNew: Int,
    var meterPhotoCaptured: Boolean = false
) {
    val electricUsed: Int get() = (electricNew - electricOld).coerceAtLeast(0)
    val waterUsed: Int get() = (waterNew - waterOld).coerceAtLeast(0)

    fun totalBill(electricPrice: Long, waterPrice: Long, serviceFee: Long): Long {
        if (status == "EMPTY") return 0L
        return roomRent + (electricUsed * electricPrice) + (waterUsed * waterPrice) + serviceFee
    }

    val initials: String
        get() {
            if (tenantName.isBlank()) return roomNumber
            val parts = tenantName.trim().split("\\s+".toRegex())
            return if (parts.size >= 2) {
                "${parts[parts.size - 2].first().uppercaseChar()}${parts.last().first().uppercaseChar()}"
            } else {
                parts.first().take(2).uppercase()
            }
        }
}

data class MaintenanceItem(
    val id: String,
    val roomNumber: String,
    val title: String,
    var description: String,
    val category: String,
    var status: String, // "URGENT", "NEW", "FIXING", "DONE"
    val dateSent: String,
    val hasPhoto: Boolean = false
)

object MotelRepository {
    private const val PREF_NAME = "nha_tro_xanh_prefs"
    private const val KEY_INITIALIZED = "initialized_v1"
    private const val KEY_ELECTRIC_PRICE = "electric_price"
    private const val KEY_WATER_PRICE = "water_price"
    private const val KEY_SERVICE_FEE = "service_fee"
    private const val KEY_METER_RECORDED_COUNT = "meter_recorded_count"
    private const val KEY_ROOMS_JSON = "rooms_json"
    private const val KEY_MAINT_JSON = "maint_json"

    var electricPrice: Long = 3500L
    var waterPrice: Long = 20000L
    var serviceFee: Long = 150000L
    var meterRecordedCount: Int = 12

    val rooms = mutableListOf<RoomData>()
    val maintenanceList = mutableListOf<MaintenanceItem>()

    private var isLoaded = false

    fun init(context: Context) {
        if (isLoaded) return
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_INITIALIZED, false)) {
            seedDefaults()
            save(context)
            prefs.edit().putBoolean(KEY_INITIALIZED, true).apply()
        } else {
            electricPrice = prefs.getLong(KEY_ELECTRIC_PRICE, 3500L)
            waterPrice = prefs.getLong(KEY_WATER_PRICE, 20000L)
            serviceFee = prefs.getLong(KEY_SERVICE_FEE, 150000L)
            meterRecordedCount = prefs.getInt(KEY_METER_RECORDED_COUNT, 12)
            loadRoomsFromJson(prefs.getString(KEY_ROOMS_JSON, null))
            loadMaintFromJson(prefs.getString(KEY_MAINT_JSON, null))
            if (rooms.isEmpty()) seedDefaults()
        }
        isLoaded = true
    }

    fun save(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val roomsArr = JSONArray()
        for (r in rooms) {
            val o = JSONObject().apply {
                put("roomNumber", r.roomNumber)
                put("floor", r.floor)
                put("status", r.status)
                put("statusNote", r.statusNote)
                put("tenantName", r.tenantName)
                put("tenantPhone", r.tenantPhone)
                put("tenantCccd", r.tenantCccd)
                put("tenantHometown", r.tenantHometown)
                put("occupants", r.occupants)
                put("vehiclePlate", r.vehiclePlate)
                put("moveInDate", r.moveInDate)
                put("contractPeriod", r.contractPeriod)
                put("roomRent", r.roomRent)
                put("deposit", r.deposit)
                put("electricOld", r.electricOld)
                put("electricNew", r.electricNew)
                put("waterOld", r.waterOld)
                put("waterNew", r.waterNew)
                put("meterPhotoCaptured", r.meterPhotoCaptured)
            }
            roomsArr.put(o)
        }

        val maintArr = JSONArray()
        for (m in maintenanceList) {
            val o = JSONObject().apply {
                put("id", m.id)
                put("roomNumber", m.roomNumber)
                put("title", m.title)
                put("description", m.description)
                put("category", m.category)
                put("status", m.status)
                put("dateSent", m.dateSent)
                put("hasPhoto", m.hasPhoto)
            }
            maintArr.put(o)
        }

        prefs.edit()
            .putLong(KEY_ELECTRIC_PRICE, electricPrice)
            .putLong(KEY_WATER_PRICE, waterPrice)
            .putLong(KEY_SERVICE_FEE, serviceFee)
            .putInt(KEY_METER_RECORDED_COUNT, meterRecordedCount)
            .putString(KEY_ROOMS_JSON, roomsArr.toString())
            .putString(KEY_MAINT_JSON, maintArr.toString())
            .apply()
    }

    private fun loadRoomsFromJson(json: String?) {
        rooms.clear()
        if (json.isNullOrBlank()) return
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                rooms.add(
                    RoomData(
                        roomNumber = o.getString("roomNumber"),
                        floor = o.getInt("floor"),
                        status = o.getString("status"),
                        statusNote = o.optString("statusNote", ""),
                        tenantName = o.optString("tenantName", ""),
                        tenantPhone = o.optString("tenantPhone", ""),
                        tenantCccd = o.optString("tenantCccd", ""),
                        tenantHometown = o.optString("tenantHometown", ""),
                        occupants = o.optInt("occupants", 1),
                        vehiclePlate = o.optString("vehiclePlate", ""),
                        moveInDate = o.optString("moveInDate", "03/2025"),
                        contractPeriod = o.optString("contractPeriod", "03/2025 – 03/2027"),
                        roomRent = o.getLong("roomRent"),
                        deposit = o.getLong("deposit"),
                        electricOld = o.getInt("electricOld"),
                        electricNew = o.getInt("electricNew"),
                        waterOld = o.getInt("waterOld"),
                        waterNew = o.getInt("waterNew"),
                        meterPhotoCaptured = o.optBoolean("meterPhotoCaptured", false)
                    )
                )
            }
        } catch (_: Exception) {
        }
    }

    private fun loadMaintFromJson(json: String?) {
        maintenanceList.clear()
        if (json.isNullOrBlank()) return
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                maintenanceList.add(
                    MaintenanceItem(
                        id = o.getString("id"),
                        roomNumber = o.getString("roomNumber"),
                        title = o.getString("title"),
                        description = o.getString("description"),
                        category = o.optString("category", "Điện lạnh"),
                        status = o.getString("status"),
                        dateSent = o.optString("dateSent", "03/10"),
                        hasPhoto = o.optBoolean("hasPhoto", false)
                    )
                )
            }
        } catch (_: Exception) {
        }
    }

    private fun seedDefaults() {
        electricPrice = 3500L
        waterPrice = 20000L
        serviceFee = 150000L
        meterRecordedCount = 12

        rooms.clear()
        // Tầng 1
        rooms.add(
            RoomData(
                roomNumber = "101", floor = 1, status = "PAID", statusNote = "Đã đóng",
                tenantName = "Nguyễn Văn An", tenantPhone = "0905 112 233", tenantCccd = "0480 •••• 1120",
                tenantHometown = "Đà Nẵng", occupants = 2, vehiclePlate = "43C1 – 456.78",
                moveInDate = "01/2025", contractPeriod = "01/2025 – 01/2027",
                roomRent = 2000000L, deposit = 2000000L,
                electricOld = 1120, electricNew = 1190, waterOld = 50, waterNew = 53
            )
        )
        rooms.add(
            RoomData(
                roomNumber = "102", floor = 1, status = "PAID", statusNote = "Đã đóng",
                tenantName = "Hoàng Thị Bích", tenantPhone = "0988 334 455", tenantCccd = "0381 •••• 9901",
                tenantHometown = "Thanh Hóa", occupants = 1, vehiclePlate = "36B1 – 221.09",
                moveInDate = "06/2025", contractPeriod = "06/2025 – 06/2027",
                roomRent = 2000000L, deposit = 2000000L,
                electricOld = 980, electricNew = 1040, waterOld = 41, waterNew = 44
            )
        )
        rooms.add(
            RoomData(
                roomNumber = "105", floor = 1, status = "PENDING", statusNote = "Hạn ngày 05/10",
                tenantName = "Trần Thị Mai", tenantPhone = "0914 223 344", tenantCccd = "0361 •••• 4412",
                tenantHometown = "Nam Định", occupants = 2, vehiclePlate = "18B2 – 889.12",
                moveInDate = "04/2025", contractPeriod = "04/2025 – 04/2027",
                roomRent = 1900000L, deposit = 1900000L,
                electricOld = 1200, electricNew = 1280, waterOld = 60, waterNew = 64
            )
        )

        // Tầng 2
        rooms.add(
            RoomData(
                roomNumber = "201", floor = 2, status = "PAID", statusNote = "Đã đóng",
                tenantName = "Đặng Quốc Khánh", tenantPhone = "0935 667 788", tenantCccd = "0520 •••• 3319",
                tenantHometown = "Bình Định", occupants = 2, vehiclePlate = "77L1 – 554.32",
                moveInDate = "02/2025", contractPeriod = "02/2025 – 02/2027",
                roomRent = 2200000L, deposit = 2200000L,
                electricOld = 1310, electricNew = 1380, waterOld = 72, waterNew = 76
            )
        )
        rooms.add(
            RoomData(
                roomNumber = "202", floor = 2, status = "EMPTY", statusNote = "Trống",
                tenantName = "", tenantPhone = "", tenantCccd = "",
                tenantHometown = "", occupants = 0, vehiclePlate = "",
                moveInDate = "", contractPeriod = "",
                roomRent = 2200000L, deposit = 2200000L,
                electricOld = 850, electricNew = 850, waterOld = 30, waterNew = 30
            )
        )
        rooms.add(
            RoomData(
                roomNumber = "203", floor = 2, status = "OVERDUE", statusNote = "Quá hạn 4 ngày",
                tenantName = "Phạm Trần Thanh Vinh", tenantPhone = "0912 345 678", tenantCccd = "0340 •••• 8821",
                tenantHometown = "Quảng Ngãi", occupants = 2, vehiclePlate = "29B1 – 123.45",
                moveInDate = "03/2025", contractPeriod = "03/2025 – 03/2027",
                roomRent = 2200000L, deposit = 2200000L,
                electricOld = 1400, electricNew = 1486, waterOld = 80, waterNew = 84
            )
        )
        rooms.add(
            RoomData(
                roomNumber = "204", floor = 2, status = "PAID", statusNote = "Đã đóng",
                tenantName = "Võ Minh Tuấn", tenantPhone = "0977 889 900", tenantCccd = "0490 •••• 7712",
                tenantHometown = "Quảng Nam", occupants = 2, vehiclePlate = "92D1 – 334.56",
                moveInDate = "05/2025", contractPeriod = "05/2025 – 05/2027",
                roomRent = 2200000L, deposit = 2200000L,
                electricOld = 1482, electricNew = 1568, waterOld = 96, waterNew = 100
            )
        )

        // Tầng 3
        rooms.add(
            RoomData(
                roomNumber = "301", floor = 3, status = "PAID", statusNote = "Đã đóng",
                tenantName = "Phan Thanh Tùng", tenantPhone = "0966 554 433", tenantCccd = "0400 •••• 6654",
                tenantHometown = "Nghệ An", occupants = 1, vehiclePlate = "37B2 – 912.34",
                moveInDate = "03/2025", contractPeriod = "03/2025 – 03/2027",
                roomRent = 2100000L, deposit = 2100000L,
                electricOld = 740, electricNew = 800, waterOld = 28, waterNew = 31
            )
        )
        rooms.add(
            RoomData(
                roomNumber = "302", floor = 3, status = "PENDING", statusNote = "Hạn ngày 05/10",
                tenantName = "Lê Quang Minh", tenantPhone = "0944 778 899", tenantCccd = "0010 •••• 5523",
                tenantHometown = "Hà Nội", occupants = 2, vehiclePlate = "29E1 – 778.90",
                moveInDate = "02/2025", contractPeriod = "02/2025 – 02/2027",
                roomRent = 2050000L, deposit = 2050000L,
                electricOld = 1100, electricNew = 1190, waterOld = 48, waterNew = 52
            )
        )
        rooms.add(
            RoomData(
                roomNumber = "303", floor = 3, status = "EMPTY", statusNote = "Trống",
                tenantName = "", tenantPhone = "", tenantCccd = "",
                tenantHometown = "", occupants = 0, vehiclePlate = "",
                moveInDate = "", contractPeriod = "",
                roomRent = 2100000L, deposit = 2100000L,
                electricOld = 500, electricNew = 500, waterOld = 20, waterNew = 20
            )
        )

        maintenanceList.clear()
        maintenanceList.add(
            MaintenanceItem(
                id = "m1",
                roomNumber = "203",
                title = "Máy lạnh không mát",
                description = "Khách báo hôm qua, đã 2 ngày chưa xử lý.",
                category = "Điện lạnh",
                status = "URGENT",
                dateSent = "03/10",
                hasPhoto = true
            )
        )
        maintenanceList.add(
            MaintenanceItem(
                id = "m2",
                roomNumber = "105",
                title = "Vòi nước rỉ",
                description = "Vòi lavabo nhỏ giọt, cần thay ron.",
                category = "Điện nước",
                status = "NEW",
                dateSent = "04/10",
                hasPhoto = false
            )
        )
        maintenanceList.add(
            MaintenanceItem(
                id = "m3",
                roomNumber = "302",
                title = "Đèn hành lang",
                description = "Thợ điện hẹn sáng mai 8:00.",
                category = "Điện nước",
                status = "FIXING",
                dateSent = "02/10",
                hasPhoto = false
            )
        )
        maintenanceList.add(
            MaintenanceItem(
                id = "m4",
                roomNumber = "201",
                title = "Khóa cửa",
                description = "Đã thay ổ khóa, chi phí 180.000đ.",
                category = "Khóa cửa",
                status = "DONE",
                dateSent = "28/09",
                hasPhoto = false
            )
        )
    }

    fun getRoom(roomNumber: String): RoomData? {
        val normalized = roomNumber.uppercase().removePrefix("SP").removePrefix("P").trim()
        return rooms.find { it.roomNumber == normalized }
    }

    fun formatCurrency(amount: Long): String {
        val nf = NumberFormat.getInstance(Locale("vi", "VN"))
        return "${nf.format(amount)}đ"
    }

    fun formatNumber(value: Int): String {
        val nf = NumberFormat.getInstance(Locale("vi", "VN"))
        return nf.format(value)
    }

    fun formatNumberLong(value: Long): String {
        val nf = NumberFormat.getInstance(Locale("vi", "VN"))
        return nf.format(value)
    }

    fun formatMillionShort(amount: Long): String {
        val millions = amount / 1_000_000.0
        return String.format(Locale("vi", "VN"), "%.2f tr", millions)
    }
}
