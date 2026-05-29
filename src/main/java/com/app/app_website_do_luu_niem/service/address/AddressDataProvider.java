package com.app.app_website_do_luu_niem.service.address;

import java.util.List;

public interface AddressDataProvider {

    String getId();

    List<AddressPlace> fetchProvinces() throws Exception;

    List<AddressPlace> fetchDistricts(String provinceCode) throws Exception;

    List<AddressPlace> fetchWards(String districtCode) throws Exception;
}
