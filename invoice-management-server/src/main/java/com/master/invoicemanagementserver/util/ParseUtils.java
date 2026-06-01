package com.master.invoicemanagementserver.util;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ParseUtils {

  public static String buildBusinessContext(String vendorName, String fileName, String invoiceId, BigDecimal amount) {
    Map<String, String> ctx = new LinkedHashMap<>();
    if (vendorName != null) ctx.put("vendor", vendorName);
    if (fileName != null)   ctx.put("file", fileName);
    if (invoiceId != null)  ctx.put("invoice", invoiceId);
    if (amount != null)     ctx.put("amount", amount.toPlainString());

    return ctx.entrySet().stream()
            .map(e -> e.getKey() + ":" + e.getValue())
            .collect(Collectors.joining(", "));
  }
}
