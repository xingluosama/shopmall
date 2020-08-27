package com.shopmall.order.client;

import com.shopmall.order.dto.AddressDTO;

import java.util.ArrayList;
import java.util.List;

public class AddressClient {
    public static final List<AddressDTO> addressList = new ArrayList<AddressDTO>() {
        {
            AddressDTO address = new AddressDTO();
            address.setId(1L);
            address.setAddress("航头镇航头路233号");
            address.setCity("上海");
            address.setDistrict("浦东新区");
            address.setName("李四");
            address.setPhone("15800000000");
            address.setState("上海");
            address.setZipCode("210000");
            address.setIsDefault(true);
            add(address);

            AddressDTO address2 = new AddressDTO();
            address2.setId(2L);
            address2.setAddress("天堂路 3号楼");
            address2.setCity("北京");
            address2.setDistrict("100000");
            address2.setName("张三");
            address2.setPhone("13600000000");
            address2.setState("北京");
            address.setZipCode("100000");
            address2.setIsDefault(false);
            add(address2);
        }
    };

    public static AddressDTO findById(Long id) {
        for (AddressDTO addressDTO : addressList) {
            if (addressDTO.getId() == id) return addressDTO;
        }
        return null;
    }
}
