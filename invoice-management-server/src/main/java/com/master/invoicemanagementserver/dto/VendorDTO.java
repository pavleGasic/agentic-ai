package com.master.invoicemanagementserver.dto;

import com.master.invoicemanagementserver.entity.Vendor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VendorDTO {
    private Long id;
    private String vendorCode;
    private String name;

    public static VendorDTO from(Vendor vendor) {
        VendorDTO dto = new VendorDTO();
        dto.id = vendor.getId();
        dto.vendorCode = vendor.getVendorCode();
        dto.name = vendor.getName();
        return dto;
    }
}
