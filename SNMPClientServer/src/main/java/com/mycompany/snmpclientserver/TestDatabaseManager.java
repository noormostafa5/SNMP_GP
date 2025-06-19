package com.mycompany.snmpclientserver;

public class TestDatabaseManager {
    public static void main(String[] args) {
        try {
            // تهيئة الاتصال بقاعدة البيانات
            DatabaseManager.initialize();

            // إضافة قاعدة الإجراءات الافتراضية
            DatabaseManager.addDefaultActionRule();

            // محاكاة حدوث خطأ في الخادم
            DatabaseManager.handleServerError(
                "BTS-1",                    // server_name
                "192.168.1.100",            // server_ip
                "CPU_UTILIZATION",          // error_type
                "CPU usage exceeded 90% for more than 5 minutes"  // description
            );

            // عرض تقارير الخادم
            DatabaseManager.getServerReports("192.168.1.100");

            // إغلاق الاتصال بقاعدة البيانات
            DatabaseManager.close();
            
            System.out.println("Test completed successfully!");
        } catch (Exception e) {
            System.err.println("Error during test: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 