# ProGuard / R8 rules for Pet Rap Sheet release build

-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep enums used in valueOf and serialization
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep booking models and enums
-keep class app.petrapsheet.booking.Species { *; }
-keep class app.petrapsheet.ui.DocumentType { *; }
-keep class app.petrapsheet.history.BookingRecord { *; }
-keep class app.petrapsheet.booking.Charge { *; }
-keep class app.petrapsheet.booking.Booking { *; }
