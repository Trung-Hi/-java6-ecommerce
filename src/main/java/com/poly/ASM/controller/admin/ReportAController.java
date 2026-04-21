package com.poly.ASM.controller.admin;

import com.poly.ASM.dto.common.ApiResponse;
import com.poly.ASM.repository.order.VipReport;
import com.poly.ASM.service.order.ReportService;
import com.poly.ASM.service.order.dto.RevenueOrderRow;
import lombok.RequiredArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class ReportAController {

    private final ReportService reportService;

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<?>> revenue(@RequestParam(value = "fromDate", required = false) LocalDate fromDate,
                                                   @RequestParam(value = "toDate", required = false) LocalDate toDate,
                                                   @RequestParam(value = "sortField", required = false) String sortField,
                                                   @RequestParam(value = "sortDir", required = false) String sortDir) {
        String fieldValue = sortField != null ? sortField : "orderId";
        String dirValue = sortDir != null ? sortDir : "asc";
        java.util.List<RevenueOrderRow> rows = reportService.revenueByDeliveredOrders(fromDate, toDate, fieldValue, dirValue);
        BigDecimal total = BigDecimal.ZERO;
        for (RevenueOrderRow row : rows) {
            if (row.getLineTotal() != null) {
                total = total.add(row.getLineTotal());
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("rows", rows);
        data.put("grandTotal", total);
        data.put("fromDate", fromDate);
        data.put("toDate", toDate);
        data.put("sortField", fieldValue);
        data.put("sortDir", dirValue);
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo doanh thu thành công", data));
    }

    @GetMapping("/vip")
    public ResponseEntity<ApiResponse<?>> vip() {
        return ResponseEntity.ok(ApiResponse.success("Lấy top khách hàng VIP thành công", reportService.top10VipCustomers()));
    }

    @GetMapping("/vip/export")
    public ResponseEntity<byte[]> exportVip(@RequestParam(value = "format", required = false, defaultValue = "xlsx") String format) throws IOException {
        java.util.List<VipReport> rows = reportService.top10VipCustomers();
        String normalizedFormat = normalizeExcelFormat(format);
        byte[] bytes = buildVipExcel(rows, normalizedFormat);
        String fileName = "khach-vip-" + LocalDate.now() + "." + normalizedFormat;
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentTypeByFormat(normalizedFormat)))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(bytes);
    }

    @GetMapping("/revenue/export")
    public ResponseEntity<byte[]> exportRevenue(@RequestParam(value = "fromDate", required = false) LocalDate fromDate,
                                                @RequestParam(value = "toDate", required = false) LocalDate toDate,
                                                @RequestParam(value = "sortField", required = false) String sortField,
                                                @RequestParam(value = "sortDir", required = false) String sortDir,
                                                @RequestParam(value = "mode", required = false) String mode,
                                                @RequestParam(value = "format", required = false, defaultValue = "xlsx") String format) throws IOException {
        String fieldValue = sortField != null ? sortField : "orderId";
        String dirValue = sortDir != null ? sortDir : "asc";
        String modeValue = mode != null && !mode.isBlank() ? mode : "summary";
        String normalizedFormat = normalizeExcelFormat(format);
        java.util.List<RevenueOrderRow> rows = reportService.revenueByDeliveredOrders(fromDate, toDate, fieldValue, dirValue);
        byte[] bytes = buildRevenueExcel(rows, fromDate, toDate, modeValue, normalizedFormat);
        String fileName = "doanh-thu-" + modeValue + "-" + LocalDate.now() + "." + normalizedFormat;
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentTypeByFormat(normalizedFormat)))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(bytes);
    }

    private byte[] buildRevenueExcel(java.util.List<RevenueOrderRow> rows,
                                     LocalDate fromDate,
                                     LocalDate toDate,
                                     String mode,
                                     String format) throws IOException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        try (Workbook workbook = createWorkbook(format); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Doanh thu");
            CellStyle headerStyle = createHeaderStyle(workbook);
            int rowIndex = 0;
            Row titleRow = sheet.createRow(rowIndex++);
            titleRow.createCell(0).setCellValue("BÁO CÁO DOANH THU - " + mode.toUpperCase());
            Row rangeRow = sheet.createRow(rowIndex++);
            rangeRow.createCell(0).setCellValue("Khoảng thời gian: " + (fromDate != null ? fromDate : "") + " đến " + (toDate != null ? toDate : ""));
            rowIndex++;
            String[] headers = {"STT", "Mã đơn", "Ngày đặt", "Sản phẩm", "Thể loại", "Số lượng", "Đơn giá", "Giảm giá", "Thành tiền"};
            Row headerRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            BigDecimal total = BigDecimal.ZERO;
            int stt = 1;
            for (RevenueOrderRow item : rows) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(stt++);
                row.createCell(1).setCellValue(item.getOrderId() != null ? String.valueOf(item.getOrderId()) : "");
                row.createCell(2).setCellValue(item.getOrderCreateDate() != null ? item.getOrderCreateDate().format(dtf) : "");
                row.createCell(3).setCellValue(item.getProductName() != null ? item.getProductName() : "");
                row.createCell(4).setCellValue(item.getCategoryName() != null ? item.getCategoryName() : "");
                row.createCell(5).setCellValue(item.getQuantity() != null ? item.getQuantity() : 0);
                row.createCell(6).setCellValue(item.getUnitPrice() != null ? item.getUnitPrice().doubleValue() : 0D);
                row.createCell(7).setCellValue(item.getDiscountAmount() != null ? item.getDiscountAmount().doubleValue() : 0D);
                row.createCell(8).setCellValue(item.getLineTotal() != null ? item.getLineTotal().doubleValue() : 0D);
                if (item.getLineTotal() != null) {
                    total = total.add(item.getLineTotal());
                }
            }
            Row totalRow = sheet.createRow(rowIndex);
            totalRow.createCell(7).setCellValue("Tổng cộng");
            totalRow.createCell(8).setCellValue(total.doubleValue());
            totalRow.getCell(7).setCellStyle(headerStyle);
            totalRow.getCell(8).setCellStyle(headerStyle);
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(output);
            return output.toByteArray();
        }
    }

    private byte[] buildVipExcel(java.util.List<VipReport> rows, String format) throws IOException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        try (Workbook workbook = createWorkbook(format); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Khách VIP");
            CellStyle headerStyle = createHeaderStyle(workbook);
            int rowIndex = 0;
            Row titleRow = sheet.createRow(rowIndex++);
            titleRow.createCell(0).setCellValue("DANH SÁCH KHÁCH HÀNG VIP");
            rowIndex++;
            String[] headers = {"STT", "Username", "Họ tên", "Email", "Địa chỉ", "Số điện thoại", "Tổng mua", "Mua lần đầu", "Lần mua gần nhất"};
            Row headerRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            int stt = 1;
            for (VipReport item : rows) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(stt++);
                row.createCell(1).setCellValue(item.getUsername() != null ? item.getUsername() : "");
                row.createCell(2).setCellValue(item.getFullname() != null ? item.getFullname() : "");
                row.createCell(3).setCellValue(item.getEmail() != null ? item.getEmail() : "");
                row.createCell(4).setCellValue(item.getAddress() != null ? item.getAddress() : "");
                row.createCell(5).setCellValue(item.getPhone() != null ? item.getPhone() : "");
                row.createCell(6).setCellValue(item.getTotalAmount() != null ? item.getTotalAmount().doubleValue() : 0D);
                row.createCell(7).setCellValue(item.getFirstOrderDate() != null ? item.getFirstOrderDate().format(dtf) : "");
                row.createCell(8).setCellValue(item.getLastOrderDate() != null ? item.getLastOrderDate().format(dtf) : "");
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(output);
            return output.toByteArray();
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private Workbook createWorkbook(String format) {
        if ("xls".equalsIgnoreCase(format)) {
            return new HSSFWorkbook();
        }
        return new XSSFWorkbook();
    }

    private String normalizeExcelFormat(String format) {
        return "xls".equalsIgnoreCase(format) ? "xls" : "xlsx";
    }

    private String contentTypeByFormat(String format) {
        if ("xls".equalsIgnoreCase(format)) {
            return "application/vnd.ms-excel";
        }
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }
}
