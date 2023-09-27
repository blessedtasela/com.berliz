package com.berliz.restImplement;

import com.berliz.constants.BerlizConstants;
import com.berliz.models.Bill;
import com.berliz.rest.BillRest;
import com.berliz.services.BillService;
import com.berliz.utils.BerlizUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class BillRestImplement implements BillRest {

    @Autowired
    BillService billService;

    @Override
    public ResponseEntity<String> generateBill(Integer id, String uuid) {
        try {
            billService.generateBill(id, uuid);
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteBill(Integer id) {
        try {
            billService.deleteBill(id);
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return BerlizUtilities.getResponseEntity(BerlizConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Bill>> getByUserId(Integer id) {
        try {
            billService.getByUserId(id);
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Bill> getByOrderId(Integer id) {
        try {
            billService.getByOrderId(id);
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Bill(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Bill> getBill(Integer id) {
        try {
            billService.getBill(id);
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new Bill(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
