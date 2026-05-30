package com.master.invoicemanagementserver.controller;

import com.master.invoicemanagementserver.dto.VendorDTO;
import com.master.invoicemanagementserver.entity.Vendor;
import com.master.invoicemanagementserver.repository.VendorRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/vendors",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class VendorController {

  private final VendorRepository vendorRepository;

  public VendorController(VendorRepository vendorRepository) {
    this.vendorRepository = vendorRepository;
  }

  @GetMapping
  public ResponseEntity<List<VendorDTO>> getAll() {
    List<VendorDTO> vendors = vendorRepository.findAll()
            .stream().map(VendorDTO::from).collect(Collectors.toList());
    return ResponseEntity.ok(vendors);
  }

  @GetMapping("/{vendorCode}")
  public ResponseEntity<VendorDTO> getByCode(@PathVariable String vendorCode) {
    return vendorRepository.findByVendorCode(vendorCode)
            .map(VendorDTO::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<VendorDTO> create(@RequestBody VendorDTO request) {
    if (vendorRepository.existsByVendorCode(request.getVendorCode())) {
      return ResponseEntity.badRequest().build();
    }
    Vendor vendor = new Vendor();
    vendor.setVendorCode(request.getVendorCode());
    vendor.setName(request.getName());
    return ResponseEntity.ok(VendorDTO.from(vendorRepository.save(vendor)));
  }
}
