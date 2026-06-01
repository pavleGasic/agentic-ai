package com.master.invoicemanagementserver.dto;

import java.util.List;

public record EarningsCalculationRequest(List<String> vendorCodes) {}
