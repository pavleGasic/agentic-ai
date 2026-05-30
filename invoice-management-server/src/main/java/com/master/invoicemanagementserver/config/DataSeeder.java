package com.master.invoicemanagementserver.config;

import com.master.invoicemanagementserver.entity.Vendor;
import com.master.invoicemanagementserver.repository.VendorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final VendorRepository vendorRepository;

    public DataSeeder(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (vendorRepository.count() > 0) {
            return;
        }

        List<Vendor> vendors = List.of(
                vendor("VEND-001", "Acme Supplies d.o.o."),
                vendor("VEND-002", "TechPro Solutions"),
                vendor("VEND-003", "Global Logistics Serbia")
        );

        vendorRepository.saveAll(vendors);
        log.info("DataSeeder: inserted {} test vendors", vendors.size());
    }

    private static Vendor vendor(String code, String name) {
        Vendor v = new Vendor();
        v.setVendorCode(code);
        v.setName(name);
        return v;
    }
}
