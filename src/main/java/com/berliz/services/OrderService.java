package com.berliz.services;

import com.berliz.models.Order;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing order-related operations.
 */
public interface OrderService {

    /**
     * Adds a new order based on the provided request map.
     *
     * @param requestMap Request body containing order details.
     * @return ResponseEntity indicating the result of the order addition operation.
     */
    ResponseEntity<String> addOrder(Map<String, Object> requestMap);

    /**
     * Retrieves a list of all orders.
     *
     * @return ResponseEntity containing the list of all orders.
     */
    ResponseEntity<List<Order>> getAllOrders();

    /**
     * Updates the details of an order based on the provided request map.
     *
     * @param requestMap Request body containing updated order details.
     * @return ResponseEntity indicating the result of the order update operation.
     */
    ResponseEntity<String> updateOrder(Map<String, Object> requestMap);

    /**
     * Deletes an order with the specified ID.
     *
     * @param id The ID of the order to delete.
     * @return ResponseEntity indicating the result of the order deletion operation.
     */
    ResponseEntity<String> deleteOrder(Integer id);

    /**
     * Updates the status of an order.
     *
     * @param id The ID of the order to update.
     * @return ResponseEntity indicating the result of the order status update operation.
     */
    ResponseEntity<String> updateStatus(Integer id);

    /**
     * Retrieves a list of orders associated with a user.
     *
     * @param id The ID of the user whose orders are to be retrieved.
     * @return ResponseEntity containing the list of orders associated with the user.
     */
    ResponseEntity<List<Order>> getByUser(Integer id);

    /**
     * Retrieves an order by its ID.
     *
     * @param id The ID of the order.
     * @return ResponseEntity containing the order with the specified ID.
     */
    ResponseEntity<Order> getOrder(Integer id);

    /**
     * Retrieves a list of orders based on their status.
     *
     * @param status The status of the orders to retrieve.
     * @return ResponseEntity containing the list of orders with the specified status.
     */
    ResponseEntity<List<Order>> getByStatus(String status);

    /**
     * Generates a bill based on it's order ID.
     *
     * @param orderId The ID of the order.
     * @return ResponseEntity containing the order with the specified ID.
     */
    ResponseEntity<String> generateBill(Integer orderId);

}
