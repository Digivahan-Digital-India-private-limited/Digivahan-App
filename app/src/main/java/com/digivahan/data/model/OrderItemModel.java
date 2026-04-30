package com.digivahan.data.model;

import java.io.Serializable;

public class OrderItemModel implements Serializable {

    // same values
    private double weight;

    // parent level
    private String _id, orderId, createdAt, updatedAt;

    // order data set details
    private String payment_order_id, order_status, active_partner, order_date, payment_method, shipping_customer_name,
            shipping_last_name, shipping_phone, shipping_address, shipping_address_2,
            shipping_city, shipping_state, shipping_country, shipping_pincode, shipping_email,
            billing_customer_name, billing_last_name, billing_phone, billing_address,
            billing_address_2, billing_city, billing_state, billing_country, billing_pincode;

    boolean is_prepared;

    private int sub_total, order_value, is_prepaid, shipping_is_billing, is_return, declared_value, length, breadth, height;

    // order item details..

    private String vehicle_id, order_type, name, sku, selling_price_currency, discount, tax;
    private int units, selling_price;

    // ship_rocket details
    private int ship_rocket_order_id, ship_rocket_shipment_id, onboarding_completed_now, status_code;
    private String payment_id, ship_rocket_status, awb_code, courier_company_id, courier_name;
    private boolean new_channel;

    public OrderItemModel() {}

    public void setOrder_status(String order_status) {
        this.order_status = order_status;
    }

    public String getActive_partner() {
        return active_partner;
    }

    public void setActive_partner(String active_partner) {
        this.active_partner = active_partner;
    }

    public boolean isIs_prepared() {
        return is_prepared;
    }

    public void setIs_prepared(boolean is_prepared) {
        this.is_prepared = is_prepared;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }


    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getPayment_order_id() {
        return payment_order_id;
    }

    public void setPayment_order_id(String payment_order_id) {
        this.payment_order_id = payment_order_id;
    }

    public String getOrder_date() {
        return safe(order_date);
    }


    public void setOrder_date(String order_date) {
        this.order_date = order_date;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }


    public void setShipping_customer_name(String shipping_customer_name) {
        this.shipping_customer_name = shipping_customer_name;
    }

    public String getShipping_last_name() {
        return shipping_last_name;
    }

    public void setShipping_last_name(String shipping_last_name) {
        this.shipping_last_name = shipping_last_name;
    }

    public void setShipping_phone(String shipping_phone) {
        this.shipping_phone = shipping_phone;
    }


    public void setShipping_address(String shipping_address) {
        this.shipping_address = shipping_address;
    }

    public String getShipping_address_2() {
        return shipping_address_2;
    }

    public void setShipping_address_2(String shipping_address_2) {
        this.shipping_address_2 = shipping_address_2;
    }

    public String getShipping_city() {
        return shipping_city;
    }

    public void setShipping_city(String shipping_city) {
        this.shipping_city = shipping_city;
    }

    public String getShipping_state() {
        return shipping_state;
    }

    public void setShipping_state(String shipping_state) {
        this.shipping_state = shipping_state;
    }

    public String getShipping_country() {
        return shipping_country;
    }

    public void setShipping_country(String shipping_country) {
        this.shipping_country = shipping_country;
    }

    public String getShipping_pincode() {
        return shipping_pincode;
    }

    public void setShipping_pincode(String shipping_pincode) {
        this.shipping_pincode = shipping_pincode;
    }

    public String getShipping_email() {
        return shipping_email;
    }

    public void setShipping_email(String shipping_email) {
        this.shipping_email = shipping_email;
    }

    public void setBilling_customer_name(String billing_customer_name) {
        this.billing_customer_name = billing_customer_name;
    }

    public String getBilling_last_name() {
        return billing_last_name;
    }

    public void setBilling_last_name(String billing_last_name) {
        this.billing_last_name = billing_last_name;
    }

    public void setBilling_phone(String billing_phone) {
        this.billing_phone = billing_phone;
    }

    public String getBilling_address() {
        return billing_address;
    }

    public void setBilling_address(String billing_address) {
        this.billing_address = billing_address;
    }

    public String getBilling_address_2() {
        return billing_address_2;
    }

    public void setBilling_address_2(String billing_address_2) {
        this.billing_address_2 = billing_address_2;
    }

    public String getBilling_city() {
        return billing_city;
    }

    public void setBilling_city(String billing_city) {
        this.billing_city = billing_city;
    }

    public String getBilling_state() {
        return billing_state;
    }

    public void setBilling_state(String billing_state) {
        this.billing_state = billing_state;
    }

    public String getBilling_country() {
        return billing_country;
    }

    public void setBilling_country(String billing_country) {
        this.billing_country = billing_country;
    }

    public String getBilling_pincode() {
        return billing_pincode;
    }

    public void setBilling_pincode(String billing_pincode) {
        this.billing_pincode = billing_pincode;
    }

    public void setSub_total(int sub_total) {
        this.sub_total = sub_total;
    }

    public void setOrder_value(int order_value) {
        this.order_value = order_value;
    }

    public int getIs_prepaid() {
        return is_prepaid;
    }

    public void setIs_prepaid(int is_prepaid) {
        this.is_prepaid = is_prepaid;
    }

    public int getShipping_is_billing() {
        return shipping_is_billing;
    }

    public void setShipping_is_billing(int shipping_is_billing) {
        this.shipping_is_billing = shipping_is_billing;
    }

    public int getIs_return() {
        return is_return;
    }

    public void setIs_return(int is_return) {
        this.is_return = is_return;
    }

    public int getDeclared_value() {
        return declared_value;
    }

    public void setDeclared_value(int declared_value) {
        this.declared_value = declared_value;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getBreadth() {
        return breadth;
    }

    public void setBreadth(int breadth) {
        this.breadth = breadth;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getVehicle_id() {
        return vehicle_id;
    }

    public void setVehicle_id(String vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSelling_price_currency() {
        return selling_price_currency;
    }

    public void setSelling_price_currency(String selling_price_currency) {
        this.selling_price_currency = selling_price_currency;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getTax() {
        return tax;
    }

    public void setTax(String tax) {
        this.tax = tax;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    public int getSelling_price() {
        return selling_price;
    }

    public void setSelling_price(int selling_price) {
        this.selling_price = selling_price;
    }

    public int getShip_rocket_order_id() {
        return ship_rocket_order_id;
    }

    public void setShip_rocket_order_id(int ship_rocket_order_id) {
        this.ship_rocket_order_id = ship_rocket_order_id;
    }

    public int getShip_rocket_shipment_id() {
        return ship_rocket_shipment_id;
    }

    public void setShip_rocket_shipment_id(int ship_rocket_shipment_id) {
        this.ship_rocket_shipment_id = ship_rocket_shipment_id;
    }

    public int getOnboarding_completed_now() {
        return onboarding_completed_now;
    }

    public void setOnboarding_completed_now(int onboarding_completed_now) {
        this.onboarding_completed_now = onboarding_completed_now;
    }

    public String getAwb_code() {
        return awb_code;
    }

    public void setAwb_code(String awb_code) {
        this.awb_code = awb_code;
    }

    public String getCourier_company_id() {
        return courier_company_id;
    }

    public void setCourier_company_id(String courier_company_id) {
        this.courier_company_id = courier_company_id;
    }

    public String getCourier_name() {
        return courier_name;
    }

    public void setCourier_name(String courier_name) {
        this.courier_name = courier_name;
    }

    public String getPayment_id() {
        return payment_id;
    }

    public void setPayment_id(String payment_id) {
        this.payment_id = payment_id;
    }

    public String getShip_rocket_status() {
        return safe(ship_rocket_status);
    }

    public void setShip_rocket_status(String ship_rocket_status) {
        this.ship_rocket_status = ship_rocket_status;
    }

    public int getStatus_code() {
        return status_code;
    }

    public void setStatus_code(int status_code) {
        this.status_code = status_code;
    }

    public boolean isNew_channel() {
        return new_channel;
    }

    public void setNew_channel(boolean new_channel) {
        this.new_channel = new_channel;
    }

    public String getOrder_type() {
        return order_type;
    }

    public void setOrder_type(String order_type) {
        this.order_type = order_type;
    }


    public String getOrder_status() {
        return safe(order_status);
    }

    public String getOrderId() {
        return safe(orderId);
    }

    public String getCreatedAt() {
        return safe(createdAt);
    }

    public String getUpdatedAt() {
        return safe(updatedAt);
    }

    public String getPayment_method() {
        return safe(payment_method);
    }

    public String getShipping_customer_name() {
        return safe(shipping_customer_name);
    }

    public String getShipping_phone() {
        return safe(shipping_phone);
    }

    public String getShipping_address() {
        return safe(shipping_address);
    }

    public String getBilling_customer_name() {
        return safe(billing_customer_name);
    }

    public String getBilling_phone() {
        return safe(billing_phone);
    }


    public int getSub_total() {
        return sub_total;
    }

    public int getOrder_value() {
        return order_value;
    }

    public double getWeight() {
        return weight;
    }




    private String safe(String value) {
        return value == null ? "" : value;
    }

}
