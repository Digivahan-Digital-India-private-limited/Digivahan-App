package com.digivahan.data.api;

public interface APIData {

    // Base URLs
    // test base url....
    String TEST_BASE_URL = "https://api.digicapital.co.in";
    String LIVE_BASE_URL = "https://api.digivahan.in";

    // live base url
    String GET_BASE_URL = "https://nextpayindia.com/api/base_url.php";
    String BASE_URL = LIVE_BASE_URL;
    String API_FOLDER = "/api/";
    String IMAGE_BASE_URL = "https://jobplan.in/";
    String IMAGE_UPLOAD_URL = "https://jobplan.in/upload/";

    // Endpoints
    String ADD_DEVICE_DATA = "/api/device/device_data";
    String APP_INFO_DATA = "/api/device/app_keys";


    // auth section end points
    String USER_REGISTER = "/api/auth/register/init";
    String CHECK_USER_REGISTER = "/api/auth/check/init";
    String RESEND_OTP = "/api/auth/register/resend-otp";
    String LOGIN = "/api/auth/sign-in";
    String LOGOUT = "/api/auth/logout-user";
    String NEW_PASSWORD = "/api/auth/check/new-password";
    String FORGET_PASSWORD = "/api/auth/request-reset-password";
    String USER_VERIFY_EMAIL_PHONE = "/api/auth/user/verify/request";
    String OTP_BASED_LOGIN = "/api/auth/otp-based-login";
    String CHANGE_PASSWORD = "/api/auth/change-password";

//    String OTP_BASED_LOGIN_VERIFY = "/api/auth/verify-login-otp";*



    String GET_NEAR_BY_SERVICES = "/api/get/all-service";
    String GET_FUEL_PRICE = "/api/v1/fuel/states";
    String GET_TRENDING_CARS = "/api/list/all-car";

    String GET_TRENDING_CARS_BY_ID = "/api/user/trending-cars/";
    String GET_COMPARE_VEHICLE_DATA_SET = "/api/vehicles/compare/get-all-compare";
    String GET_TIPS_TRICKS = "/api/v1/tips-tricks";
    String GET_NEW_LIST = "/api/v1/news";
    String GET_BENEFIT_VIDEO = "/api/v1/qr-benefits";


    // Address APIS end points
    String DELETE_ADDRESS = "/api/v1/user-address/delete";
    String ADD_ADDRESS = "/api/v1/user-address/add";
    String UPDATE_ADDRESS = "/api/v1/user-address/upadte";

    // Emergency APIS end points
    String ADD_EMERGENCY_CONTACT = "/api/v1/add/emergency-contact";
    String DELETE_EMERGENCY_CONTACT = "/api/v1/delete/emergency-contact";
    String UPDATE_EMERGENCY_CONTACT = "/api/v1/update/emergency-contact";

    String UPLOAD_PROFILE_IMAGE = "/api/upload/profile-image";
    String UPDATE_USER_DATA = "/api/update_user";

    // vehicle APIs end points
    String CHECK_VEHICLE = "/api/v1/add-vehicle";
    String ADD_VEHICLE = "/api/v1/user/add-garage";
    String REFRESH_VEHICLE = "/api/v1/refresh/vehicle-data";
    String GET_VEHICLE_LIST = "/api/v1/garage/";
    String DELETE_VEHICLE_ITEM = "/api/v1/garage/remove-vehicle";

    String UPLOAD_SINGLE_FILE = "/api/v1/notification/image";
    String DELETE_SINGLE_FILE = "/api/v1/notification/delete-image";

    String UPLOAD_VEHICLE_FILE = "/api/upload/single";
    String DELETE_VEHICLE_FILE = "/api/vehicle/doc-delete";


    // order APIs end points
    String GET_ORDER_ESTIMATED_TIME = "/api/check/courier-service";
    String CREATE_ORDER = "/api/user/create-order";
    String GET_ORDER_LIST = "/api/orders-user-list";
    String RAZORPAY_PAYMENT = "/api/v1/razorpay/order";
    String SEND_FEEDBACK = "/api/review-submit";
    String GET_USER_DETAILS = "/api/get_user_details";

    // Notification APIs...
    String SEND_NOTIFICATION = "/api/notifications/send";
    String SET_NOTIFICATION_SOUND = "/api/notifications/user/on-notification";
    String GET_NOTIFICATION = "/api/notifications/";
    String SET_NOTIFICATION_SEEN = "/api/notifications/user/seen-notification";


    String CALL_USER = "/api/notifications/send/call-notification";
    String CREATE_CHAT_ROOM = "/api/create/room";
    String VAULT_ACCESS = "/vault-document-access";
    String CHANG_PRIMARY_CONTACT = "/api/v1/user/change-primary-contact";
    String CANCEL_ORDER = "/api/order/user-cancel";
    String TRACK_ORDER = "/api/track-order-status";


    // QR endpoints....
    String CREATE_QR_CODE = "/api/generate-qr";
    String ASSIGN_QR_CODE = "/api/qr-assignment";
    String GET_QR_CODE_BY_ID = "/api/qr/";
    String CHECK_QR_CODE = "/api/check-qr";
    String GET_QR_TEMPLATE = "/api/create/qr-template-user/";


    // Doc access APIs end points...
    String DOC_ACCESS_CHECK = "/api/check/security-code";
    String DOC_ACCESS_VERIFICATION = "/api/verify/security-code";

    // chatting system APIS

    String SEND_CHAT_MESSAGE = "/api/send/messages";
    String GET_CHAT_MESSAGE_LIST = "/api/messages/";


    // App update APIS end points...
    String GET_APP_INFO = "/api/v1/app-info";
    String GET_ANDROID_APP_INFO = "/api/v1/app-info/android";

    // call and message API
    String CONTACT_VIA_CALL = "/api/user/contact-via-call";
    String SEND_SMS = "/api/send/sms-notification";


    // BBPS Services APIS end points...
    String GET_BILLER_LIST = "/api/biller-list";
    String GET_BILLER_DETAILS = "/api/biller-details?billerId=";
    String GET_BILLER_ENQUIRY = "/api/biller-enquiry";
    String GET_VALIDATE_BILLER = "/api/validate-biller";
    String PAYMENT_SERVICE = "/api/user/payment-service";
}
