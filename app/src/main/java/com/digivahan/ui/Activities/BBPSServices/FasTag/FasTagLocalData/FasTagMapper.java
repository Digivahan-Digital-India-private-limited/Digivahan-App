package com.digivahan.ui.Activities.BBPSServices.FasTag.FasTagLocalData;

import com.digivahan.data.model.FasTagCardModel;

public class FasTagMapper {

    // Entity → Model
    public static FasTagCardModel toModel(FasTagEntity entity) {
        FasTagCardModel model = new FasTagCardModel();

        model.setBillerId(entity.billerId);
        model.setVehicleNumber(entity.vehicleNumber);
        model.setBillerName(entity.billerName);
        model.setCategoryKey(entity.categoryKey);
        model.setType(entity.type);
        model.setCategoryName(entity.categoryName);
        model.setCoverageCity(entity.coverageCity);
        model.setCoverageState(entity.coverageState);
        model.setUpdatedDate(entity.updatedDate);
        model.setBillerStatus(entity.billerStatus);
        model.setIconUrl(entity.iconUrl);
        model.setCoveragePincode(entity.coveragePincode);
        model.setAvailable(entity.isAvailable);
        model.setMinAmount(entity.minAmount);
        model.setMaxAmount(entity.maxAmount);

        return model;
    }

    // Model → Entity
    public static FasTagEntity toEntity(FasTagCardModel model) {
        FasTagEntity entity = new FasTagEntity();

        entity.billerId = model.getBillerId();
        entity.vehicleNumber = model.getVehicleNumber();
        entity.billerName = model.getBillerName();
        entity.categoryKey = model.getCategoryKey();
        entity.type = model.getType();
        entity.categoryName = model.getCategoryName();
        entity.coverageCity = model.getCoverageCity();
        entity.coverageState = model.getCoverageState();
        entity.updatedDate = model.getUpdatedDate();
        entity.billerStatus = model.getBillerStatus();
        entity.iconUrl = model.getIconUrl();
        entity.coveragePincode = model.getCoveragePincode();
        entity.isAvailable = model.isAvailable();
        entity.minAmount = model.getMinAmount();
        entity.maxAmount = model.getMaxAmount();

        return entity;
    }
}