package com.nhom9.dtdd
data class AdminUser(
    var id: String,
    var name: String,
    var email: String,
    var phone: String,
    var role: String,
    var active: Boolean = true
)

data class AdminProperty(
    var id: String,
    var name: String,
    var address: String,
    var ownerId: String,
    var active: Boolean = true
)

object AdminStore {

    val users = mutableListOf(
        AdminUser(
            id = "U001",
            name = "Quản trị viên",
            email = "admin@nhatroxanh.vn",
            phone = "0900000000",
            role = "ADMIN"
        ),
        AdminUser(
            id = "U002",
            name = "Trần Văn Minh",
            email = "minh@gmail.com",
            phone = "0912345678",
            role = "CHU_TRO"
        ),
        AdminUser(
            id = "U003",
            name = "Phạm Trần Thanh Vinh",
            email = "vinh@gmail.com",
            phone = "0930123456",
            role = "KHACH_THUE"
        )
    )

    val properties = mutableListOf(
        AdminProperty(
            id = "NT001",
            name = "Nhà Trọ Xanh",
            address = "Đà Nẵng",
            ownerId = "U002"
        )
    )

    private const val PREF_NAME = "admin_store"

    fun init(context: android.content.Context) {
        val prefs = context.getSharedPreferences(
            PREF_NAME,
            android.content.Context.MODE_PRIVATE
        )

        val userJson = prefs.getString("users", null)
        val propertyJson = prefs.getString("properties", null)

        if (userJson != null) {
            users.clear()
            val array = org.json.JSONArray(userJson)

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)

                users.add(
                    AdminUser(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        email = obj.getString("email"),
                        phone = obj.getString("phone"),
                        role = obj.getString("role"),
                        active = obj.optBoolean("active", true)
                    )
                )
            }
        }

        if (propertyJson != null) {
            properties.clear()
            val array = org.json.JSONArray(propertyJson)

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)

                properties.add(
                    AdminProperty(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        address = obj.getString("address"),
                        ownerId = obj.getString("ownerId"),
                        active = obj.optBoolean("active", true)
                    )
                )
            }
        }
    }

    fun save(context: android.content.Context) {
        val prefs = context.getSharedPreferences(
            PREF_NAME,
            android.content.Context.MODE_PRIVATE
        )

        val userArray = org.json.JSONArray()

        users.forEach { user ->
            val obj = org.json.JSONObject()

            obj.put("id", user.id)
            obj.put("name", user.name)
            obj.put("email", user.email)
            obj.put("phone", user.phone)
            obj.put("role", user.role)
            obj.put("active", user.active)

            userArray.put(obj)
        }

        val propertyArray = org.json.JSONArray()

        properties.forEach { property ->
            val obj = org.json.JSONObject()

            obj.put("id", property.id)
            obj.put("name", property.name)
            obj.put("address", property.address)
            obj.put("ownerId", property.ownerId)
            obj.put("active", property.active)

            propertyArray.put(obj)
        }

        prefs.edit()
            .putString("users", userArray.toString())
            .putString("properties", propertyArray.toString())
            .apply()
    }

    fun writeAudit(
        context: android.content.Context,
        message: String
    ) {
        val prefs = context.getSharedPreferences(
            PREF_NAME,
            android.content.Context.MODE_PRIVATE
        )

        val oldLogs = prefs.getString(
            "audit",
            "[]"
        ) ?: "[]"

        val logs = org.json.JSONArray(oldLogs)

        val date = java.text.SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            java.util.Locale.getDefault()
        ).format(java.util.Date())

        logs.put("$date - $message")

        prefs.edit()
            .putString("audit", logs.toString())
            .apply()
    }

    fun audit(
        context: android.content.Context
    ): List<String> {
        val prefs = context.getSharedPreferences(
            PREF_NAME,
            android.content.Context.MODE_PRIVATE
        )

        val json = prefs.getString(
            "audit",
            "[]"
        ) ?: "[]"

        val array = org.json.JSONArray(json)

        return (0 until array.length())
            .map { array.getString(it) }
            .reversed()
    }
}
