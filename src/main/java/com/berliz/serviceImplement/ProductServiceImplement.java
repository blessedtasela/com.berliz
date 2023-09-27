package com.berliz.serviceImplement;

import com.berliz.JWT.JWTFilter;
import com.berliz.constants.BerlizConstants;
import com.berliz.models.*;
import com.berliz.repository.*;
import com.berliz.services.ProductService;
import com.berliz.utils.BerlizUtilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ProductServiceImplement implements ProductService {

    @Autowired
    ProductRepo productRepo;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    BrandRepo brandRepo;

    @Autowired
    StoreRepo storeRepo;

    @Autowired
    TagRepo tagRepo;

    @Autowired
    JWTFilter jwtFilter;

    /**
     * Adds a new product based on the provided request map.
     *
     * @param requestMap The map containing product details to be added
     * @return ResponseEntity indicating the result of the operation
     */
    @Override
    public ResponseEntity<String> addProduct(Map<String, String> requestMap) {
        try {
            log.info("inside addProduct {}", requestMap);
            if (jwtFilter.isAdmin()) {
                boolean isValid = validateProductMap(requestMap, false);
                log.info("is request isValid? {}", isValid);
                if (isValid) {
                    Product product = productRepo.findByName(requestMap.get("name"));
                    if (product == null) {
                        productRepo.save(getProductFromMap(requestMap, false));
                        return BerlizUtilities.getResponseEntity("Product added successfully", HttpStatus.OK);
                    } else {
                        return BerlizUtilities.getResponseEntity("Product exits", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    return BerlizUtilities.getResponseEntity(BerlizConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
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
     * Updates an existing product based on the provided request map.
     *
     * @param requestMap The map containing updated product details
     * @return ResponseEntity indicating the result of the operation
     */
    @Override
    public ResponseEntity<String> updateProduct(Map<String, String> requestMap) {
        try {
            log.info("inside updateProduct {}", requestMap);
            Integer id = Integer.parseInt(requestMap.get("id"));
            Optional<Product> optional = productRepo.findById(id);
            Integer userId = jwtFilter.getCurrentUserId();
            boolean isValid = validateProductMap(requestMap, true);
            log.info("Is request valid? {}", isValid);

            if (!isValid) {
                return BerlizUtilities.getResponseEntity(BerlizConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
            log.info("Does product exist? {}", optional);
            if (optional.isEmpty()) {
                return BerlizUtilities.getResponseEntity("Product does not exist", HttpStatus.BAD_REQUEST);
            }

            Store store = optional.get().getStore();
            Integer validUser = store.getPartner().getUser().getId();
            if (jwtFilter.isAdmin() || validUser.equals(userId)) {
                updateProductFromMap(requestMap);
                return BerlizUtilities.getResponseEntity("Product updated successfully", HttpStatus.OK);
            } else {
                return BerlizUtilities.getResponseEntity(BerlizConstants.UNAUTHORIZED_REQUEST, HttpStatus.UNAUTHORIZED);
            }
        } catch (
                Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * Retrieves a list of all products.
     *
     * @return ResponseEntity containing the list of products or an error response
     */
    @Override
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            log.info("Inside getAllProducts");
            if (jwtFilter.isAdmin()) {
                List<Product> products = productRepo.findAll();
                return new ResponseEntity<>(products, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Deletes a product with the specified ID.
     *
     * @param id The ID of the product to be deleted
     * @return ResponseEntity indicating the result of the operation
     */
    @Override
    public ResponseEntity<String> deleteProduct(Integer id) {
        try {
            log.info("inside deleteProduct {}", id);
            if (jwtFilter.isAdmin()) {
                Optional<Product> optional = productRepo.findById(id);
                if (optional.isPresent()) {
                    log.info("inside optional {}", id);
                    productRepo.deleteById(id);
                    return BerlizUtilities.getResponseEntity("Product deleted successfully", HttpStatus.OK);
                } else {
                    return BerlizUtilities.getResponseEntity("Product id does not exist", HttpStatus.BAD_REQUEST);
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
     * Updates status of a product.
     *
     * @param id The ID of the product status to be updated
     * @return ResponseEntity indicating the result of the operation
     */
    @Override
    public ResponseEntity<String> updateStatus(Integer id) {
        try {
            log.info("Inside updateStatus {}", id);
            String status;
            if (jwtFilter.isAdmin()) {
                Optional<Product> optional = productRepo.findById(id);
                if (optional.isPresent()) {
                    log.info("Inside optional {}", optional);
                    status = optional.get().getStatus();
                    if (status.equalsIgnoreCase("true")) {
                        status = "false";
                        productRepo.updateStatus(id, status);
                        return BerlizUtilities.getResponseEntity("Product Status updated successfully. Now DISABLED", HttpStatus.OK);
                    } else {
                        status = "true";
                        productRepo.updateStatus(id, status);
                        return BerlizUtilities.getResponseEntity("Product Status updated successfully. Now ACTIVE", HttpStatus.OK);
                    }
                } else {
                    return BerlizUtilities.getResponseEntity("Product id not found", HttpStatus.BAD_REQUEST);
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
     * Retrieves a product with the specified ID.
     *
     * @param id The ID of the product to retrieve
     * @return ResponseEntity containing the retrieved product or an error response
     */
    @Override
    public ResponseEntity<Product> getProduct(Integer id) {
        try {
            log.info("Inside getProduct {}", id);
            Product product = productRepo.findByProductId(id);
            if (product != null) {
                log.info("Inside optional {}", product);
                return new ResponseEntity<>(product, HttpStatus.OK);
            } else {
                return new ResponseEntity("Product id not found", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Product(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Retrieves a list of products by the specified brand ID.
     *
     * @param id The ID of the brand to retrieve products for
     * @return ResponseEntity containing the list of products or an error response
     */
    @Override
    public ResponseEntity<List<Product>> getByBrand(Integer id) {
        try {
            log.info("Inside getProduct {}", id);
            List<Product> product = productRepo.findByBrandId(id);
            if (!product.isEmpty()) {
                log.info("Inside optional {}", product);
                return new ResponseEntity<>(product, HttpStatus.OK);
            } else {
                return new ResponseEntity("Brand id not found in product", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Retrieves a list of products by the specified store ID.
     *
     * @param id The ID of the store to retrieve products for
     * @return ResponseEntity containing the list of products or an error response
     */
    @Override
    public ResponseEntity<List<Product>> getByStore(Integer id) {
        try {
            log.info("Inside getByStore {}", id);
            List<Product> product = productRepo.findByStoreId(id);
            if (!product.isEmpty()) {
                log.info("Inside optional {}", product);
                return new ResponseEntity<>(product, HttpStatus.OK);
            } else {
                return new ResponseEntity("Store id not found in product", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Retrieves a list of products by the specified status.
     *
     * @param status The status to filter products by
     * @return ResponseEntity containing the list of products or an error response
     */
    @Override
    public ResponseEntity<List<Product>> getByStatus(String status) {
        try {
            log.info("Inside getByStatus {}", status);
            List<Product> product = productRepo.findByStatus(status);
            if (!product.isEmpty()) {
                log.info("Inside optional {}", product);
                return new ResponseEntity<>(product, HttpStatus.OK);
            } else {
                return new ResponseEntity("Status not found in product", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Validates product based on the provided request maps in request body.
     *
     * @param requestMap elements to be validated
     * @param validId    to check if the request meet a condition
     * @return ResponseEntity containing the list of products or an error response
     */
    private boolean validateProductMap(Map<String, String> requestMap, boolean validId) {
        if (validId) {
            return requestMap.containsKey("id")
                    && requestMap.containsKey("name")
                    && requestMap.containsKey("description")
                    && requestMap.containsKey("price")
                    && requestMap.containsKey("quantity")
                    && requestMap.containsKey("photo")
                    && requestMap.containsKey("sex")
                    && requestMap.containsKey("categoryIds")
                    && requestMap.containsKey("brandId")
                    && requestMap.containsKey("storeId")
                    && requestMap.containsKey("tagIds");
        } else {
            return requestMap.containsKey("name")
                    && requestMap.containsKey("description")
                    && requestMap.containsKey("price")
                    && requestMap.containsKey("quantity")
                    && requestMap.containsKey("photo")
                    && requestMap.containsKey("sex")
                    && requestMap.containsKey("categoryIds")
                    && requestMap.containsKey("brandId")
                    && requestMap.containsKey("storeId")
                    && requestMap.containsKey("tagIds");
        }
    }

    /**
     * Constructs a Product object based on the provided request map.
     *
     * @param requestMap The map containing product details
     * @param isAdd      A flag indicating whether the product is being added (true) or updated (false)
     * @return The constructed Product object
     */
    private Product getProductFromMap(Map<String, String> requestMap, Boolean isAdd) {
        // Parse categoryIds as a comma-separated string
        String categoryIdsString = requestMap.get("categoryIds");
        String[] categoryIdsArray = categoryIdsString.split(",");

        Set<Category> categorySet = new HashSet<>();
        for (String categoryIdString : categoryIdsArray) {
            // Remove leading and trailing spaces before parsing
            int categoryId = Integer.parseInt(categoryIdString.trim());

            Category category = new Category();
            category.setId(categoryId);
            categorySet.add(category);
        }

        // Parse tagIds as a comma-separated string
        String tagIdsString = requestMap.get("tagIds");
        String[] tagIdsArray = tagIdsString.split(",");

        Set<Tag> tagSet = new HashSet<>();
        for (String tagIdString : tagIdsArray) {
            // Remove leading and trailing spaces before parsing
            int tagId = Integer.parseInt(tagIdString.trim());

            Tag tag = new Tag();
            tag.setId(tagId);
            tagSet.add(tag);
        }

        // Create Brand object
        Brand brand = new Brand();
        brand.setId(Integer.parseInt(requestMap.get("brandId")));

        // Create Store object
        Store store = new Store();
        store.setId(Integer.parseInt(requestMap.get("storeId")));

        // Create and populate Product object
        Product product = new Product();
        Date currentDate = new Date();

        if (isAdd) {
            product.setId(Integer.parseInt(requestMap.get("id")));
        }
        product.setCategorySet(categorySet);
        product.setUuid(BerlizUtilities.getProductUUID());
        product.setBrand(brand);
        product.setTagSet(tagSet);
        product.setStore(store);
        product.setName(requestMap.get("name"));
        product.setDescription(requestMap.get("description"));
        product.setPrice(Float.parseFloat(requestMap.get("price")));
        product.setQuantity(Integer.parseInt(requestMap.get("quantity")));
        product.setPhoto(requestMap.get("photo"));
        product.setSex(requestMap.get("sex"));
        product.setStatus("true");
        product.setDate(currentDate);
        product.setLastUpdate(currentDate);

        return product;
    }

    /**
     * Updates an existing product based on the provided request map.
     *
     * @param requestMap The map containing updated product details
     * @return ResponseEntity indicating the result of the operation
     */
    private ResponseEntity<String> updateProductFromMap(Map<String, String> requestMap) {
        try {
            // Get the ID of the product to be updated
            Integer id = Integer.parseInt(requestMap.get("id"));
            Optional<Product> optional = productRepo.findById(id);
            log.info("Does product exist? {}", optional);

            // Check if the product exists
            if (optional.isEmpty()) {
                return new ResponseEntity<>("Product id is null or not found", HttpStatus.NOT_FOUND);
            }

            // Get the existing product to be updated
            Product existingProduct = optional.get();

            // Save the existing data
            Date existingDate = existingProduct.getDate();
            String existingUuid = existingProduct.getUuid();

            // Update product attributes
            existingProduct.setUuid(existingUuid);
            existingProduct.setName(requestMap.get("name"));
            existingProduct.setDescription(requestMap.get("description"));
            existingProduct.setPrice(Float.parseFloat(requestMap.get("price")));
            existingProduct.setQuantity(Integer.parseInt(requestMap.get("quantity")));
            existingProduct.setPhoto(requestMap.get("photo"));
            existingProduct.setSex(requestMap.get("sex"));
            existingProduct.setLastUpdate(new Date());
            existingProduct.setDate(existingDate);

            // Update relationships
            Category category = categoryRepo.findById(Integer.parseInt(requestMap.get("categoryId"))).orElse(null);
            existingProduct.getCategorySet().clear(); // Clear existing categories
            if (category != null) {
                existingProduct.getCategorySet().add(category);
            }

            Brand brand = brandRepo.findById(Integer.parseInt(requestMap.get("brandId"))).orElse(null);
            existingProduct.setBrand(brand);

            Store store = storeRepo.findById(Integer.parseInt(requestMap.get("storeId"))).orElse(null);
            existingProduct.setStore(store);

            Tag tag = tagRepo.findById(Integer.parseInt(requestMap.get("tagId"))).orElse(null);
            existingProduct.getTagSet().clear(); // Clear existing tags
            if (tag != null) {
                existingProduct.getTagSet().add(tag);
            }

            // Save the updated product
            productRepo.save(existingProduct);
            return BerlizUtilities.getResponseEntity("Product updated successfully", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
