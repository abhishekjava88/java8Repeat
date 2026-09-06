package com.abhi;

import com.abhi.model.Order;
import com.abhi.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.abhi.model.Status.*;
import static com.abhi.model.Status.CANCELLED;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {

   private List<Order> orders;

   @BeforeEach
   void setUp(){
       orders = List.of(
               new Order(1,PENDING,56.0),
               new Order(2,PENDING,89),
               new Order(3,PENDING,42.0),

               new Order(4,SHIPPED,78.0),
               new Order(5,SHIPPED,61.0),
               new Order(6,SHIPPED,42.0),

               new Order(7,CANCELLED,55.0),
               new Order(8,CANCELLED,46.0),
               new Order(9,CANCELLED,-36.0),
               new Order(10,CANCELLED,9.0)

       );
   }

    @Test
    void testGetOrderByStatus() {
        Map<Status,Double> result = App.getOrderByStatus(orders);
        double expectedResult = 110.0;
        assertEquals(result.get(CANCELLED),expectedResult,0.01);
    }

    @Test
    void testOrderIdsByStatus() {
       Map<Status,List<Long>> result = App.orderIdsByStatus(orders);
       List<Long> pendingIds = List.of(1L, 2L, 3L);
       assertEquals(result.get(PENDING),pendingIds);
    }
}