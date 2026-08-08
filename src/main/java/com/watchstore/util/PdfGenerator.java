package com.watchstore.util;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.watchstore.model.StockExport;
import com.watchstore.model.StockExportItem;
import com.watchstore.model.StockReceipt;
import com.watchstore.model.StockReceiptItem;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class PdfGenerator {

    private static Font getFont(int size, int style) {
        try {
            // Read font from resources
            InputStream is = PdfGenerator.class.getResourceAsStream("/fonts/Roboto-Regular.ttf");
            if (is != null) {
                byte[] fontBytes = is.readAllBytes();
                BaseFont bf = BaseFont.createFont("Roboto-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);
                return new Font(bf, size, style);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Fallback font (might not support full UTF-8 Vietnamese)
        return new Font(Font.HELVETICA, size, style);
    }

    private static Font getBoldFont(int size) {
        return getFont(size, Font.BOLD);
    }

    private static Font getNormalFont(int size) {
        return getFont(size, Font.NORMAL);
    }

    public static void generateReceiptPdf(StockReceipt receipt, OutputStream outputStream) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        Font titleFont = getBoldFont(18);
        Font boldFont = getBoldFont(12);
        Font normalFont = getNormalFont(12);

        // Header
        Paragraph header = new Paragraph("WATCHSTORE", titleFont);
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);

        Paragraph subHeader = new Paragraph("PHIẾU NHẬP KHO", getBoldFont(16));
        subHeader.setAlignment(Element.ALIGN_CENTER);
        subHeader.setSpacingAfter(20f);
        document.add(subHeader);

        // Info
        document.add(new Paragraph("Mã phiếu: " + receipt.getReceiptCode(), normalFont));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String dateStr = receipt.getReceiptDate() != null ? receipt.getReceiptDate().format(formatter) : "";
        document.add(new Paragraph("Ngày tạo: " + dateStr, normalFont));
        document.add(new Paragraph("Người lập phiếu: " + receipt.getCreatedByName(), normalFont));
        document.add(new Paragraph("Nhà cung cấp: " + receipt.getSupplierName(), normalFont));
        document.add(new Paragraph("Kho nhập: " + receipt.getWarehouseName(), normalFont));
        
        Paragraph pInfo = new Paragraph("Danh sách sản phẩm:", boldFont);
        pInfo.setSpacingBefore(15f);
        pInfo.setSpacingAfter(10f);
        document.add(pInfo);

        // Table
        PdfPTable table = new PdfPTable(new float[]{1, 4, 2, 2, 2, 3});
        table.setWidthPercentage(100);
        
        String[] headers = {"STT", "Sản phẩm", "SKU", "Số lượng", "Đơn giá", "Thành tiền"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, boldFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5f);
            table.addCell(cell);
        }

        int index = 1;
        int totalQty = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (receipt.getItems() != null) {
            for (StockReceiptItem item : receipt.getItems()) {
                table.addCell(new Phrase(String.valueOf(index++), normalFont));
                table.addCell(new Phrase(item.getProductName() + " - " + item.getVariantName(), normalFont));
                table.addCell(new Phrase(item.getSku(), normalFont));
                
                PdfPCell qtyCell = new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
                qtyCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(qtyCell);
                
                PdfPCell priceCell = new PdfPCell(new Phrase(item.getUnitCost() != null ? String.format("%,.0f", item.getUnitCost()) : "0", normalFont));
                priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(priceCell);
                
                BigDecimal lineTotal = item.getUnitCost() != null ? item.getUnitCost().multiply(new BigDecimal(item.getQuantity())) : BigDecimal.ZERO;
                PdfPCell totalCell = new PdfPCell(new Phrase(String.format("%,.0f", lineTotal), normalFont));
                totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(totalCell);
                
                totalQty += item.getQuantity();
                totalAmount = totalAmount.add(lineTotal);
            }
        }

        document.add(table);

        // Footer info
        Paragraph pTotal = new Paragraph("Tổng số lượng: " + totalQty, boldFont);
        pTotal.setSpacingBefore(10f);
        pTotal.setAlignment(Element.ALIGN_RIGHT);
        document.add(pTotal);
        
        Paragraph pAmount = new Paragraph("Tổng tiền: " + String.format("%,.0f", totalAmount), boldFont);
        pAmount.setAlignment(Element.ALIGN_RIGHT);
        document.add(pAmount);

        // Signatures
        PdfPTable signTable = new PdfPTable(2);
        signTable.setWidthPercentage(100);
        signTable.setSpacingBefore(30f);
        
        PdfPCell cellCreator = new PdfPCell(new Phrase("Người lập phiếu\n(Ký, ghi rõ họ tên)", normalFont));
        cellCreator.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellCreator.setBorder(0);
        
        PdfPCell cellApprover = new PdfPCell(new Phrase("Người duyệt\n(Ký, ghi rõ họ tên)", normalFont));
        cellApprover.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellApprover.setBorder(0);
        
        signTable.addCell(cellCreator);
        signTable.addCell(cellApprover);
        
        document.add(signTable);

        document.close();
    }

    public static void generateExportPdf(StockExport export, OutputStream outputStream) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        Font titleFont = getBoldFont(18);
        Font boldFont = getBoldFont(12);
        Font normalFont = getNormalFont(12);

        // Header
        Paragraph header = new Paragraph("WATCHSTORE", titleFont);
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);

        Paragraph subHeader = new Paragraph("PHIẾU XUẤT KHO", getBoldFont(16));
        subHeader.setAlignment(Element.ALIGN_CENTER);
        subHeader.setSpacingAfter(20f);
        document.add(subHeader);

        // Info
        document.add(new Paragraph("Mã phiếu: " + export.getExportCode(), normalFont));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String dateStr = export.getExportDate() != null ? export.getExportDate().format(formatter) : "";
        document.add(new Paragraph("Ngày tạo: " + dateStr, normalFont));
        document.add(new Paragraph("Người lập phiếu: " + export.getCreatedByName(), normalFont));
        document.add(new Paragraph("Kho xuất: " + export.getWarehouseName(), normalFont));
        document.add(new Paragraph("Người nhận: " + (export.getReceiverName() != null ? export.getReceiverName() : ""), normalFont));
        if (export.getOrderId() != null && export.getOrderId() > 0) {
            document.add(new Paragraph("Mã đơn hàng: " + export.getOrderId(), normalFont));
        }
        
        Paragraph pInfo = new Paragraph("Danh sách sản phẩm:", boldFont);
        pInfo.setSpacingBefore(15f);
        pInfo.setSpacingAfter(10f);
        document.add(pInfo);

        // Table
        PdfPTable table = new PdfPTable(new float[]{1, 5, 2, 2});
        table.setWidthPercentage(100);
        
        String[] headers = {"STT", "Sản phẩm", "SKU", "Số lượng"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, boldFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5f);
            table.addCell(cell);
        }

        int index = 1;
        int totalQty = 0;

        if (export.getItems() != null) {
            for (StockExportItem item : export.getItems()) {
                table.addCell(new Phrase(String.valueOf(index++), normalFont));
                table.addCell(new Phrase(item.getProductName() + " - " + item.getVariantName(), normalFont));
                table.addCell(new Phrase(item.getSku(), normalFont));
                
                PdfPCell qtyCell = new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
                qtyCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(qtyCell);
                
                totalQty += item.getQuantity();
            }
        }

        document.add(table);

        // Footer info
        Paragraph pTotal = new Paragraph("Tổng số lượng: " + totalQty, boldFont);
        pTotal.setSpacingBefore(10f);
        pTotal.setAlignment(Element.ALIGN_RIGHT);
        document.add(pTotal);
        
        // Signatures
        PdfPTable signTable = new PdfPTable(2);
        signTable.setWidthPercentage(100);
        signTable.setSpacingBefore(30f);
        
        PdfPCell cellCreator = new PdfPCell(new Phrase("Người lập phiếu\n(Ký, ghi rõ họ tên)", normalFont));
        cellCreator.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellCreator.setBorder(0);
        
        PdfPCell cellApprover = new PdfPCell(new Phrase("Người nhận\n(Ký, ghi rõ họ tên)", normalFont));
        cellApprover.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellApprover.setBorder(0);
        
        signTable.addCell(cellCreator);
        signTable.addCell(cellApprover);
        
        document.add(signTable);

        document.close();
    }
}
