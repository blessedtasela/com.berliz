package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Bill;
import com.berliz.models.Order;
import com.berliz.models.User;
import com.berliz.repository.BillRepo;
import com.berliz.repository.UserRepo;
import com.berliz.services.BillService;
import com.berliz.utils.BerlizUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class BillServiceImplement implements BillService {

    @Autowired
    BillRepo billRepo;

    @Autowired
    JWTFilter jwtFilter;

    @Autowired
    UserRepo userRepo;

    @Override
    public ResponseEntity<String> generateBill(Integer id, String uuid) {
        try {
            log.info("Inside generateBill", id);
            String fileName;
            if (id != null) {
                if (uuid != null) {
                    fileName = uuid;
                } else {
                    fileName = BerlizUtilities.getBillUUID();
                    uuid = fileName;
                    getBillFromMap(id, uuid);
                }
                String data = "id: " + id + "\nuuid: " + uuid;

                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(BerlizConstants.BILL_LOCATION + "\\" + fileName + ".pdf"));

                document.open();
                setRectangleInPdf(document);

                Paragraph header = new Paragraph("Berliz", getFont("header"));
                header.setAlignment(Element.ALIGN_CENTER);
                document.add(header);

                Paragraph title = new Paragraph("Order invoice", getFont("title"));
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);

                Paragraph paragraph = new Paragraph(data + "\n\n", getFont("data"));
                document.add(paragraph);

                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                addTableHeader(table);

                JSONArray jsonArray = BerlizUtilities.getJsonArrayFromString(String.valueOf(id));
                for (int i = 0; i < jsonArray.length(); i++) {
                    addRow(table, BerlizUtilities.getMapFromJson(jsonArray.getString(i)));
                }
                document.add(table);

                Paragraph footer = new Paragraph("Total: " + uuid + "\nThank you for your purchase. We are always here to deliver the best!");
                document.add(footer);
                document.close();

                return new ResponseEntity<>("{\"uuid\": \"" + fileName + "}", HttpStatus.OK);
            } else {
                return BerlizUtilities.getResponseEntity("Required data not found", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteBill(Integer id) {
        try {
            if (jwtFilter.isAdmin()) {
                Bill bill = billRepo.findByBillId(id);
                if (bill != null) {
                    billRepo.delete(bill);
                    return BerlizUtilities.getResponseEntity("Bill deleted successfully", HttpStatus.OK);
                } else {
                    return BerlizUtilities.getResponseEntity("Bill id not found", HttpStatus.BAD_REQUEST);
                }
            } else {
                return BerlizUtilities.getResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Bill>> getByUserId(Integer id) {
        try {
            log.info("Inside getByUserId");
            if (jwtFilter.isAdmin()) {
                List<Bill> bills = billRepo.findByUserId(String.valueOf(id));
                if (bills != null) {
                    return new ResponseEntity<>(bills, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Bill> getByOrderId(Integer id) {
        try {
            log.info("Inside getByOrderId");
            if (jwtFilter.isAdmin()) {
                Bill bill = billRepo.findByOrderId(id);
                if (bill != null) {
                    return new ResponseEntity<>(bill, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(new Bill(), HttpStatus.NOT_FOUND);
                }
            } else {
                return new ResponseEntity<>(new Bill(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Bill(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Bill> getBill(Integer id) {
        try {
            log.info("Inside getBill");
            if (jwtFilter.isAdmin()) {
                Bill bill = billRepo.findByBillId(id);
                if (bill != null) {
                    return new ResponseEntity<>(bill, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(new Bill(), HttpStatus.NOT_FOUND);
                }
            } else {
                return new ResponseEntity<>(new Bill(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Bill(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private void addRow(PdfPTable table, Map<String, Object> data) {
        log.info("Inside addRow{}", data);
        table.addCell((String) data.get("id"));
        table.addCell((String) data.get("uuid"));
    }

    private void addTableHeader(PdfPTable table) {
        log.info("Inside addTableHeader {}", table);

        Stream.of("id", "uuid").forEach(columnTitle -> {
            PdfPCell header = new PdfPCell();
            header.setBackgroundColor(BaseColor.LIGHT_GRAY);
            header.setBorderWidth(2);
            header.setPhrase(new Phrase(columnTitle));
            header.setBackgroundColor(BaseColor.YELLOW);
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setVerticalAlignment(Element.ALIGN_CENTER);
            table.addCell(header);
        });
    }

    private Font getFont(String type) {
        log.info("Inside getFont {}", type);
        switch (type) {
            case "header":
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 20, BaseColor.BLACK);
                headerFont.setStyle(Font.BOLD);
                return headerFont;

            case "title":
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 15, BaseColor.BLACK);
                titleFont.setStyle(Font.BOLD);
                return titleFont;

            case "data":
                Font dataFont = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 15, BaseColor.BLACK);
                dataFont.setStyle(Font.BOLD);
                return dataFont;

            default:
                return new Font();
        }
    }

    private void setRectangleInPdf(Document document) throws DocumentException {
        log.info("Inside setRectangleInPdf", document);

        Rectangle rectangle = new Rectangle(557, 825, 18, 15);
        rectangle.enableBorderSide(1);
        rectangle.enableBorderSide(2);
        rectangle.enableBorderSide(4);
        rectangle.enableBorderSide(8);
        rectangle.setBorderColor(BaseColor.BLACK);
        rectangle.setBorderWidth(1);
        document.add(rectangle);
    }

    private void getBillFromMap(Integer id, String uuid) {
        Optional<User> optional = userRepo.findById(jwtFilter.getCurrentUserId());
        if(optional.isEmpty()){
            System.out.println("Error: User id not found");
        }
        Integer currentUser = optional.get().getId();
        Order order = new Order();
        order.setId(id);

        User user = new User();
        user.setId(currentUser);

        Bill bill = new Bill();
        Date currentDate = new Date();

        bill.setOrder(order);
        bill.setUuid(uuid);
        bill.setUser(user);
        bill.setDate(currentDate);
        billRepo.save(bill);
    }

    private boolean validateBillMap(Map<String, Object> requestMap, boolean validId) {
        if (validId) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("orderId")
                    && requestMap.containsKey("uuid")
                    && requestMap.containsKey("createdBy");
        } else {
            return requestMap.containsKey("orderId")
                    && requestMap.containsKey("uuid")
                    && requestMap.containsKey("createdBy");
        }
    }

    /**
     * Build a ResponseEntity with the given status code and message.
     *
     * @param status  The HTTP status code.
     * @param message The response message.
     * @return A ResponseEntity with the specified status and message.
     * @throws JsonProcessingException If there is an issue processing the JSON response.
     */
    private ResponseEntity<String> buildResponse(HttpStatus status, String message) throws JsonProcessingException {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", message);

        ObjectMapper objectMapper = new ObjectMapper();
        String responseBodyJson = objectMapper.writeValueAsString(responseBody);

        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBodyJson);
    }
}
