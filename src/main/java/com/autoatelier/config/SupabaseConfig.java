package com.autoatelier.config;

public class SupabaseConfig {

    public static final String SUPABASE_URL = "https://swvnmoboauavzxcwcnbf.supabase.co";
    public static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InN3dm5tb2JvYXVhdnp4Y3djbmJmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzM2NTQyMDgsImV4cCI6MjA4OTIzMDIwOH0.5RadPFATgwrMdoWhMma02l1X6FcL3uC2c0yFUWfvo-g";

    public static final String REST_URL = SUPABASE_URL + "/rest/v1";
    public static final String AUTH_URL = SUPABASE_URL + "/auth/v1";
    public static final String STORAGE_URL = SUPABASE_URL + "/storage/v1";

    public static final String ORDERS_BUCKET = "order-photos";
    public static final String SERVICES_BUCKET = "service-images";

    private SupabaseConfig() {}
}
