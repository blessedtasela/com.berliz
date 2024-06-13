package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.Order;
import com.berliz.models.OrderDetails;
import com.berliz.models.Product;
import com.berliz.models.User;
import com.berliz.repositories.OrderDetailsRepo;
import com.berliz.repositories.OrderRepo;
import com.berliz.repositories.ProductRepo;
import com.berliz.services.OrderService;
import com.berliz.utils.BerlizUtilities;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Stream;

@Service
@Slf4j
public class OrderServiceImplement implements OrderService {


    @Autowired
    OrderDetailsRepo orderDetailsRepo;

    @Autowired
    OrderRepo orderRepo;

    @Autowired
    JWTFilter jwtFilter;

    @Autowired
    ProductRepo productRepo;

    /**
     * Adds a new order based on the provided request map.
     *
     * @param requestMap The map containing order details to be added
     * @return ResponseEntity indicating the result of the operation
     */
    @Override
    public ResponseEntity<String> addOrder(Map<String, Object> requestMap) {
        try {
            log.info("Inside addOrder {}", requestMap);

            // Validate the request map
            boolean isValid = validateOrderMap(requestMap, false);
            log.info("Is request valid? {}", isValid);

            if (isValid) {
                String uuid;

                // Check if the UUID needs to be generated or provided
                if (requestMap.containsKey("isGenerate") && !(Boolean) requestMap.get("isGenerate")) {
                    uuid = (String) requestMap.get("uuid");
                } else {
                    uuid = BerlizUtilities.getOrderUUID();
                    requestMap.put("uuid", uuid);
                }

                // Check if an order with the provided UUID already exists
                Order existingOrder = orderRepo.findByUuid(uuid);
                if (existingOrder == null) {

                    // Insert the order and return success message
                    getOrderFromMap(requestMap);
                    return BerlizUtilities.getResponseEntity("Order added successfully", HttpStatus.OK);
                } else {
                    return BerlizUtilities.getResponseEntity("Order already exists", HttpStatus.BAD_REQUEST);
                }
            } else {
                return BerlizUtilities.getResponseEntity(BerlizConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Updates an existing order based on the provided request map.
     *
     * @param requestMap The map containing updated order details
     * @return ResponseEntity indicating the result of the operation
     */
    @Override
    public ResponseEntity<String> updateOrder(Map<String, Object> requestMap) {
        try {
            log.info("Inside updateOrder", requestMap);

            // Get the ID of the current user from the JWT token
            Integer userId = jwtFilter.getCurrentUserId();

            // Validate the request map
            boolean isValid = validateOrderMap(requestMap, true);
            log.info("Is request valid? {}", isValid);

            if (isValid) {
                Integer orderId = (Integer) requestMap.get("id");
                Order existingOrder = orderRepo.findByOrderId(orderId);
                if (existingOrder == null) {
                    return BerlizUtilities.getResponseEntity("Order id not found", HttpStatus.BAD_REQUEST);
                }

                // Check if the order is already completed
                if (existingOrder.getStatus().equalsIgnoreCase("true")) {
                    return new ResponseEntity<>("Cannot make update. Order completed", HttpStatus.FORBIDDEN);
                }

                Integer validUser = existingOrder.getUser().getId();

                // Check if the current user is authorized to update the order
                if (jwtFilter.isAdmin() || validUser.equals(userId)) {
                    updateOrderFromMap(requestMap);
                    return new ResponseEntity<>("Center updated successfully", HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
                }

            } else {
                return BerlizUtilities.getResponseEntity("Invalid data", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Retrieves a list of all orders.
     *
     * @return ResponseEntity containing the list of orders or an error response
     */
    @Override
    public ResponseEntity<List<Order>> getAllOrders() {
        try {
            log.info("Inside getAllOrders");
            Integer userId = jwtFilter.getCurrentUserId();
            Order orderUser = orderRepo.findByOrderId(userId);

            // case for admin
            if (jwtFilter.isAdmin()) {
                List<Order> orders = orderRepo.findAll();

                // Initialize user and product associations for each order
                for (Order order : orders) {
                    Hibernate.initialize(order.getUser());  // Initialize the user association
                    for (OrderDetails orderDetails : order.getOrderDetailsSet()) {
                        Hibernate.initialize(orderDetails.getProduct());  // Initialize product associations
                    }
                }

                return new ResponseEntity<>(orders, HttpStatus.OK);
            }
            // case for currently logged-in user
            else if (userId.equals(orderUser.getUser().getId())) {
                List<Order> orders = orderRepo.findByUserId(userId);

                // Initialize user and product associations for each order
                for (Order order : orders) {
                    Hibernate.initialize(order.getUser());  // Initialize the user association
                    for (OrderDetails orderDetails : order.getOrderDetailsSet()) {
                        Hibernate.initialize(orderDetails.getProduct());  // Initialize product associations
                    }
                }

                return new ResponseEntity<>(orders, HttpStatus.OK);
            } else {
                return new ResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Deletes an order by its ID.
     *
     * @param id The ID of the order to delete
     * @return ResponseEntity indicating the result of the delete operation
     */
    @Override
    public ResponseEntity<String> deleteOrder(Integer id) {
        try {
            if (jwtFilter.isAdmin()) {
                Order order = orderRepo.findByOrderId(id);
                if (order != null) {
                    orderRepo.delete(order);
                    return BerlizUtilities.getResponseEntity("Order deleted successfully", HttpStatus.OK);
                } else {
                    return BerlizUtilities.getResponseEntity("Order id not found", HttpStatus.BAD_REQUEST);
                }
            } else {
                return BerlizUtilities.getResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Updates the status of an order by its ID.
     *
     * @param id The ID of the order to update status
     * @return ResponseEntity indicating the result of the status update operation
     */
    @Override
    public ResponseEntity<String> updateStatus(Integer id) {
        try {
            log.info("Inside updateStatus {}", id);
            String status;
            if (jwtFilter.isAdmin()) {
                Optional<Order> optional = orderRepo.findById(id);
                if (optional.isPresent()) {
                    log.info("Inside optional {}", optional);
                    status = optional.get().getStatus();
                    if (status.equalsIgnoreCase("true")) {
                        status = "false";
                        orderRepo.updateStatus(id, status);
                        return BerlizUtilities.getResponseEntity("Order Status updated successfully. ORDER PENDING", HttpStatus.OK);
                    } else {
                        status = "true";
                        orderRepo.updateStatus(id, status);
                        return BerlizUtilities.getResponseEntity("Order Status updated successfully. ORDER COMPLETE", HttpStatus.OK);
                    }
                } else {
                    return BerlizUtilities.getResponseEntity("Order id not found", HttpStatus.BAD_REQUEST);
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
    public ResponseEntity<List<Order>> getByUser(Integer id) {
        try {
            log.info("Inside getByUser {}", id);
            if (jwtFilter.isAdmin() || !jwtFilter.getCurrentUserEmail().isEmpty()) {
                List<Order> order = orderRepo.findByUserId(id);
                if (order != null) {
                    log.info("Inside optional {}", order);
                    return new ResponseEntity<>(order, HttpStatus.OK);
                } else {
                    return new ResponseEntity("User id not found", HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity("User not found", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param id The ID of the order to retrieve
     * @return ResponseEntity containing the retrieved order or an error response
     */
    @Override
    public ResponseEntity<Order> getOrder(Integer id) {
        try {
            log.info("Inside getOrder {}", id);

            Order order = orderRepo.findByOrderId(id);
            Integer userId = order.getUser().getId();
            if (order == null) {
                return new ResponseEntity("Order id not found", HttpStatus.BAD_REQUEST);
            }
            if (jwtFilter.isAdmin() || jwtFilter.getCurrentUserId().equals(userId)) {
                return new ResponseEntity<>(order, HttpStatus.OK);
            } else {
                return new ResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Retrieves a list of orders by their status.
     *
     * @param status The status of the orders to retrieve
     * @return ResponseEntity containing the list of orders with the given status or an error response
     */
    @Override
    public ResponseEntity<List<Order>> getByStatus(String status) {
        try {
            log.info("Inside getByStatus {}", status);
            List<Order> order = orderRepo.findByStatus(status);
            if (order != null) {
                log.info("Inside optional {}", order);
                return new ResponseEntity<>(order, HttpStatus.OK);
            } else {
                return new ResponseEntity("Order status not found", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Generates a PDF bill for the given order ID.
     *
     * @param orderId The ID of the order for which to generate the bill.
     * @return ResponseEntity containing the generated bill UUID and HTTP status.
     */
    @Override
    public ResponseEntity<String> generateBill(Integer orderId) {
        try {
            log.info("Inside generateBill for orderId: {}", orderId);

            // Find the order by ID
            Order order = orderRepo.findByOrderId(orderId);
            if (order == null) {
                return new ResponseEntity<>("Order id not found or is null", HttpStatus.NOT_FOUND);
            }

            // Create bill data for PDF
            String fileName = order.getUuid();

            // Create a PDF document
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(BerlizConstants.BILL_LOCATION + "\\" + fileName + ".pdf"));

            document.open();

            Image image = Image.getInstance(BerlizConstants.BILL_LOGO);
            image.scaleAbsolute(100, 50); // Set the width and height of the image

            // Add header with image icon
            Paragraph header = new Paragraph();
            header.setAlignment(Element.ALIGN_CENTER);
            header.add(image); // Add the image to the paragraph
            header.add(new Phrase("Berliz Order invoice", getFont("header"))); // Add the text
            document.add(header);

            // Leave some space between the image and title
            document.add(Chunk.NEWLINE);

            String status = order.getStatus().equalsIgnoreCase("true") ? "Completed" : "Pending";

            // Add data
            String data = "Name: " + order.getName() +
                    "\nContact: " + order.getContact() +
                    "\nEmail: " + order.getEmail() +
                    "\nPayment method: " + order.getPaymentMethod() +
                    "\nStatus: " + status;
            Paragraph paragraph = new Paragraph(data + "\n\n", getFont("data"));
            document.add(paragraph);

            // Create and add table for order details
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            addTableHeader(table);

            // Add rows for order details
            addRow(table, order);

            document.add(table);

            DecimalFormat decimalFormat = new DecimalFormat("#0.00");
            // Add footer
            Paragraph footer = new Paragraph("\nTotal: $ " + decimalFormat.format(order.getTotalAmount())
                    + "\nThank you for your purchase. " +
                    "We are always here to deliver the best!", getFont("remark"));
            document.add(footer);
            document.close();

            return new ResponseEntity<>("Bill for " + fileName + " generated successfully", HttpStatus.OK);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * Validates order based on the provided request maps in request body.
     *
     * @param requestMap elements to be validated
     * @param validId    to check if the request meet a condition
     * @return ResponseEntity containing the list of orders or an error response
     */
    private boolean validateOrderMap(Map<String, Object> requestMap, boolean validId) {
        if (validId) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("uuid")
                    && requestMap.containsKey("name")
                    && requestMap.containsKey("email")
                    && requestMap.containsKey("contact")
                    && requestMap.containsKey("country")
                    && requestMap.containsKey("state")
                    && requestMap.containsKey("postalCode")
                    && requestMap.containsKey("city")
                    && requestMap.containsKey("address")
                    && requestMap.containsKey("paymentMethod")
                    && requestMap.containsKey("productDetails");
        } else {
            return requestMap.containsKey("uuid")
                    && requestMap.containsKey("name")
                    && requestMap.containsKey("email")
                    && requestMap.containsKey("contact")
                    && requestMap.containsKey("country")
                    && requestMap.containsKey("state")
                    && requestMap.containsKey("postalCode")
                    && requestMap.containsKey("city")
                    && requestMap.containsKey("address")
                    && requestMap.containsKey("paymentMethod")
                    && requestMap.containsKey("productDetails");
        }
    }

    /**
     * Inserts a new order into the repository based on the provided request map.
     *
     * @param requestMap The map containing order details to be inserted
     * @throws Exception If an error occurs while inserting the order
     */
    private Order getOrderFromMap(Map<String, Object> requestMap) throws Exception {
        try {
            Order order = new Order();

            User user = new User();
            // Get the current user based on the JWT token
            Integer userId = jwtFilter.getCurrentUserId();
            user.setId(userId);

            order.setUser(user);

            Date currentDate = new Date();
            order.setDate(currentDate);
            order.setLastUpdate(currentDate);

            List<Map<String, Object>> productDetailsList = (List<Map<String, Object>>) requestMap.get("productDetails");
            Set<OrderDetails> orderDetailsSet = new HashSet<>();  // Use Set to store order details

            float totalAmount = 0;
            Set<Integer> processedProductIds = new HashSet<>();

            for (Map<String, Object> productMap : productDetailsList) {
                Integer productId = (Integer) productMap.get("productId");

                // Find product in repository by product id
                Product product = productRepo.getReferenceById(productId);

                // Check if the product ID is null or not found
                if (!productRepo.existsById(productId)) {
                    throw new Exception("product id is null");
                }

                // Check if the product ID has already been processed
                if (processedProductIds.contains(productId)) {
                    log.info("Duplicate product id. Only one added");
                } else {
                    // Mark the product ID as processed
                    processedProductIds.add(productId);

                    product.setQuantity((Integer) productMap.get("quantity"));
                    double price = product.getPrice() * ((Number) productMap.get("quantity")).doubleValue();
                    product.setPrice(price);

                    // Calculate the total amount for the order
                    totalAmount += product.getQuantity() * product.getPrice();

                    OrderDetails orderDetail = new OrderDetails();
                    orderDetail.setProduct(product);
                    orderDetail.setQuantity(product.getQuantity());
                    orderDetail.setSubTotal(product.getQuantity() * product.getPrice());
                    orderDetailsRepo.save(orderDetail);
                    orderDetailsSet.add(orderDetail); // Add the order detail to the set
                }
            }

            order.setOrderDetailsSet(orderDetailsSet);  // Set the order details set

            // Set other order details from the request map
            order.setUuid((String) requestMap.get("uuid"));
            order.setName((String) requestMap.get("name"));
            order.setEmail((String) requestMap.get("email"));
            order.setContact((String) requestMap.get("contact"));
            order.setCountry((String) requestMap.get("country"));
            order.setState((String) requestMap.get("state"));
            order.setPostalCode(Integer.parseInt((String) requestMap.get("postalCode")));
            order.setCity((String) requestMap.get("city"));
            order.setAddress((String) requestMap.get("address"));
            order.setPaymentMethod((String) requestMap.get("paymentMethod"));
            order.setTotalAmount(totalAmount);
            order.setStatus("false");

            orderRepo.save(order); // Save the order (including its associated order details) to the repository

            return order;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        throw new Exception(BerlizConstants.SOMETHING_WENT_WRONG);
    }

    /**
     * Updates an existing order based on the provided request map.
     *
     * @param requestMap The map containing order details to be updated
     * @return ResponseEntity indicating the result of the operation
     */
    private ResponseEntity<String> updateOrderFromMap(Map<String, Object> requestMap) {
        try {
            Integer orderId = (Integer) requestMap.get("id");
            Order existingOrder = orderRepo.findByOrderId(orderId);

            if (existingOrder == null) {
                return new ResponseEntity<>("Order id not found", HttpStatus.NOT_FOUND);
            }

            List<Map<String, Object>> productDetailsList = (List<Map<String, Object>>) requestMap.get("productDetails");
            Set<OrderDetails> orderDetailsSet = new HashSet<>();  // Use Set to store order details

            float totalAmount = 0;
            for (Map<String, Object> productMap : productDetailsList) {
                // Construct a Product object
                Product product = new Product();
                product.setId((Integer) productMap.get("productId"));
                product.setQuantity((Integer) productMap.get("quantity"));
                double price = product.getPrice() * ((Integer) productMap.get("quantity"));
                product.setPrice(price);

                // Calculate the total amount for the order
                totalAmount += product.getQuantity() * product.getPrice();

                OrderDetails orderDetail = orderDetailsRepo.findByOrderDetailsId(productMap.get("orderDetailsId"));

                // Create a new order detail if it is null
                if (orderDetail == null) {
                    orderDetail = new OrderDetails();
                }

                orderDetail.setProduct(product);
                orderDetail.setQuantity((Integer) productMap.get("quantity"));
                orderDetail.setSubTotal((Integer) productMap.get("quantity") * product.getPrice());

                orderDetailsRepo.save(orderDetail);
                orderDetailsSet.add(orderDetail); // Add the order detail to the set
            }

            // Update existing order details
            existingOrder.setOrderDetailsSet(orderDetailsSet);
            existingOrder.setName((String) requestMap.get("name"));
            existingOrder.setEmail((String) requestMap.get("email"));
            existingOrder.setContact((String) requestMap.get("contact"));
            existingOrder.setCountry((String) requestMap.get("country"));
            existingOrder.setState((String) requestMap.get("state"));
            existingOrder.setPostalCode(Integer.parseInt((String) requestMap.get("postalCode")));
            existingOrder.setCity((String) requestMap.get("city"));
            existingOrder.setAddress((String) requestMap.get("address"));
            existingOrder.setPaymentMethod((String) requestMap.get("paymentMethod"));
            existingOrder.setDate(new Date());

            existingOrder.setTotalAmount(totalAmount);
            orderRepo.save(existingOrder);

            return BerlizUtilities.getResponseEntity("Order updated successfully", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Sets a rectangle in the PDF document.
     *
     * @param document The Document to which the rectangle will be added.
     * @throws DocumentException If there's an error while adding the rectangle to the document.
     */
    private void setRectangleInPdf(Document document) throws DocumentException {
        log.info("Inside setRectangleInPdf", document);
        float sideLength = 1000; // Adjust this value as needed

        float left = (document.getPageSize().getWidth() - sideLength) / 2;
        float bottom = (document.getPageSize().getHeight() - sideLength) / 2;
        float right = left + sideLength;
        float top = bottom + sideLength;

        Rectangle rectangle = new Rectangle(left, bottom, right, top);
        rectangle.enableBorderSide(Rectangle.BOX);
        rectangle.setBorderColor(BaseColor.BLACK);
        rectangle.setBorderWidth(1);
        document.add(rectangle);
    }

    /**
     * Retrieves a Font object based on the provided font type.
     *
     * @param type The type of font to retrieve ("header", "remark", or "data").
     * @return The Font object with the specified properties.
     */
    private Font getFont(String type) {
        log.info("Inside getFont {}", type);
        switch (type) {
            case "header":
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 20, BaseColor.BLACK);
                headerFont.setStyle(Font.BOLDITALIC);
                return headerFont;

            case "remark":
                Font remarkFont = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 15, BaseColor.BLACK);
                remarkFont.setStyle(Font.NORMAL);
                return remarkFont;

            case "data":
                Font dataFont = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 10, BaseColor.BLACK);
                dataFont.setStyle(Font.NORMAL);
                return dataFont;

            default:
                return new Font();
        }
    }

    /**
     * Adds a row of OrderDetails data to the PDF table.
     *
     * @param table The PdfPTable to which the row cells will be added.
     * @param order The Order object containing the OrderDetails to be added.
     */
    private void addRow(PdfPTable table, Order order) {
        log.info("Inside addRow{}", order);
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");

        for (OrderDetails orderDetails : order.getOrderDetailsSet()) {
            table.addCell(orderDetails.getProduct().getName());
            table.addCell(orderDetails.getProduct().getDescription());
            table.addCell(String.valueOf(orderDetails.getQuantity()));
            table.addCell(String.valueOf(decimalFormat.format(orderDetails.getProduct().getPrice())));
            table.addCell(String.valueOf(decimalFormat.format(orderDetails.getSubTotal())));
        }
    }

    /**
     * Adds the table header to the PDF table.
     *
     * @param table The PdfPTable to which the header cells will be added.
     */
    private void addTableHeader(PdfPTable table) {
        log.info("Inside addTableHeader {}", table);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 20, BaseColor.BLACK);
        headerFont.setColor(BaseColor.WHITE);  // Set font color to white

        Stream.of("Name", "Description", "Quantity", "Price", "Sub total").forEach(columnTitle -> {
            PdfPCell header = new PdfPCell();
            header.setBackgroundColor(BaseColor.LIGHT_GRAY);
            header.setPhrase(new Phrase(columnTitle, headerFont));  // Apply the headerFont
            header.setPhrase(new Phrase(columnTitle));
            header.setPadding(2);
            header.setBackgroundColor(BaseColor.RED);
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setVerticalAlignment(Element.ALIGN_CENTER);
            table.addCell(header);
        });
    }

}
