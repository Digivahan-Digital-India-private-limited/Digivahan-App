package com.digivahan.data.model;

import java.io.Serializable;
import java.util.ArrayList;

public class GarageItemModel implements Serializable {
    String vehicle_id, owner_name, vehicle_number, vehicle_name,
            registration_date, ownership_details, registered_rto,
            makers_model, makers_name, vehicle_class, fuel_type, fuel_norms,
            engine, chassis_number, insurer_name, insurance_type, insurance_expiry, financer_name,
            insurance_renewed_date, vehicle_age, fitness_upto, pollution_renew_date, pollution_expiry,
            color, unloaded_weight, rc_status, insurance_policy_number, _id,
            insurance_url, pollution_url, registration_url, fitness_url, permit_url, permitNumber, permitType, permitValidFrom, permitValidUpto,
            nationalPermitNumber, nationalPermitValidUpto, nationalPermitIssuedBy, category;

    ArrayList<vehicleDocuments> vehicleDocumentsArrayList;

    public GarageItemModel() {}

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFinancer_name() {
        return financer_name;
    }

    public void setFinancer_name(String financer_name) {
        this.financer_name = financer_name;
    }

    public String getNationalPermitNumber() {
        return nationalPermitNumber;
    }

    public void setNationalPermitNumber(String nationalPermitNumber) {
        this.nationalPermitNumber = nationalPermitNumber;
    }

    public String getNationalPermitValidUpto() {
        return nationalPermitValidUpto;
    }

    public void setNationalPermitValidUpto(String nationalPermitValidUpto) {
        this.nationalPermitValidUpto = nationalPermitValidUpto;
    }

    public String getNationalPermitIssuedBy() {
        return nationalPermitIssuedBy;
    }

    public void setNationalPermitIssuedBy(String nationalPermitIssuedBy) {
        this.nationalPermitIssuedBy = nationalPermitIssuedBy;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public String getPermitType() {
        return permitType;
    }

    public void setPermitType(String permitType) {
        this.permitType = permitType;
    }

    public String getPermitValidFrom() {
        return permitValidFrom;
    }

    public void setPermitValidFrom(String permitValidFrom) {
        this.permitValidFrom = permitValidFrom;
    }

    public String getPermitValidUpto() {
        return permitValidUpto;
    }

    public void setPermitValidUpto(String permitValidUpto) {
        this.permitValidUpto = permitValidUpto;
    }

    public String getVehicle_id() {
        return vehicle_id;
    }

    public void setVehicle_id(String vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

    public String getOwner_name() {
        return owner_name;
    }

    public void setOwner_name(String owner_name) {
        this.owner_name = owner_name;
    }

    public String getVehicle_number() {
        return vehicle_number;
    }

    public void setVehicle_number(String vehicle_number) {
        this.vehicle_number = vehicle_number;
    }

    public String getVehicle_name() {
        return vehicle_name;
    }

    public void setVehicle_name(String vehicle_name) {
        this.vehicle_name = vehicle_name;
    }

    public String getRegistration_date() {
        return registration_date;
    }

    public void setRegistration_date(String registration_date) {
        this.registration_date = registration_date;
    }

    public String getOwnership_details() {
        return ownership_details;
    }

    public void setOwnership_details(String ownership_details) {
        this.ownership_details = ownership_details;
    }

    public String getRegistered_rto() {
        return registered_rto;
    }

    public void setRegistered_rto(String registered_rto) {
        this.registered_rto = registered_rto;
    }

    public String getMakers_model() {
        return makers_model;
    }

    public void setMakers_model(String makers_model) {
        this.makers_model = makers_model;
    }

    public String getMakers_name() {
        return makers_name;
    }

    public void setMakers_name(String makers_name) {
        this.makers_name = makers_name;
    }

    public String getVehicle_class() {
        return vehicle_class;
    }

    public void setVehicle_class(String vehicle_class) {
        this.vehicle_class = vehicle_class;
    }

    public String getFuel_type() {
        return fuel_type;
    }

    public void setFuel_type(String fuel_type) {
        this.fuel_type = fuel_type;
    }

    public String getFuel_norms() {
        return fuel_norms;
    }

    public void setFuel_norms(String fuel_norms) {
        this.fuel_norms = fuel_norms;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getChassis_number() {
        return chassis_number;
    }

    public void setChassis_number(String chassis_number) {
        this.chassis_number = chassis_number;
    }

    public String getInsurer_name() {
        return insurer_name;
    }

    public void setInsurer_name(String insurer_name) {
        this.insurer_name = insurer_name;
    }

    public String getInsurance_type() {
        return insurance_type;
    }

    public void setInsurance_type(String insurance_type) {
        this.insurance_type = insurance_type;
    }

    public String getInsurance_expiry() {
        return insurance_expiry;
    }

    public void setInsurance_expiry(String insurance_expiry) {
        this.insurance_expiry = insurance_expiry;
    }

    public String getInsurance_renewed_date() {
        return insurance_renewed_date;
    }

    public void setInsurance_renewed_date(String insurance_renewed_date) {
        this.insurance_renewed_date = insurance_renewed_date;
    }

    public String getVehicle_age() {
        return vehicle_age;
    }

    public void setVehicle_age(String vehicle_age) {
        this.vehicle_age = vehicle_age;
    }

    public String getFitness_upto() {
        return fitness_upto;
    }

    public void setFitness_upto(String fitness_upto) {
        this.fitness_upto = fitness_upto;
    }

    public String getPollution_renew_date() {
        return pollution_renew_date;
    }

    public void setPollution_renew_date(String pollution_renew_date) {
        this.pollution_renew_date = pollution_renew_date;
    }

    public String getPollution_expiry() {
        return pollution_expiry;
    }

    public void setPollution_expiry(String pollution_expiry) {
        this.pollution_expiry = pollution_expiry;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getUnloaded_weight() {
        return unloaded_weight;
    }

    public void setUnloaded_weight(String unloaded_weight) {
        this.unloaded_weight = unloaded_weight;
    }

    public String getRc_status() {
        return rc_status;
    }

    public void setRc_status(String rc_status) {
        this.rc_status = rc_status;
    }

    public String getInsurance_policy_number() {
        return insurance_policy_number;
    }

    public void setInsurance_policy_number(String insurance_policy_number) {
        this.insurance_policy_number = insurance_policy_number;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getInsurance_url() {
        return insurance_url;
    }

    public void setInsurance_url(String insurance_url) {
        this.insurance_url = insurance_url;
    }

    public String getPollution_url() {
        return pollution_url;
    }

    public void setPollution_url(String pollution_url) {
        this.pollution_url = pollution_url;
    }

    public String getRegistration_url() {
        return registration_url;
    }

    public void setRegistration_url(String registration_url) {
        this.registration_url = registration_url;
    }

    public String getFitness_url() {
        return fitness_url;
    }

    public void setFitness_url(String fitness_url) {
        this.fitness_url = fitness_url;
    }

    public String getPermit_url() {
        return permit_url;
    }

    public void setPermit_url(String permit_url) {
        this.permit_url = permit_url;
    }

    public ArrayList<vehicleDocuments> getVehicleDocumentsArrayList() {
        return vehicleDocumentsArrayList;
    }

    public void setVehicleDocumentsArrayList(ArrayList<vehicleDocuments> vehicleDocumentsArrayList) {
        this.vehicleDocumentsArrayList = vehicleDocumentsArrayList;
    }

    public static class vehicleDocuments implements Serializable{
        String doc_name, doc_type, public_id, doc_number, doc_url, _id;

        public vehicleDocuments() {}

        public String getDoc_name() {
            return doc_name;
        }

        public void setDoc_name(String doc_name) {
            this.doc_name = doc_name;
        }

        public String getDoc_number() {
            return doc_number;
        }

        public void setDoc_number(String doc_number) {
            this.doc_number = doc_number;
        }

        public String getDoc_url() {
            return doc_url;
        }

        public void setDoc_url(String doc_url) {
            this.doc_url = doc_url;
        }

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getDoc_type() {
            return doc_type;
        }

        public void setDoc_type(String doc_type) {
            this.doc_type = doc_type;
        }

        public String getPublic_id() {
            return public_id;
        }

        public void setPublic_id(String public_id) {
            this.public_id = public_id;
        }
    }

}
