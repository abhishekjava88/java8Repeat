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

    @Test
    void testCountByStatus() {
        Map<Status, Long> result = App.countByStatus(orders);
        Long expectedResult = 4l;
        assertEquals(result.get(CANCELLED),expectedResult);
    }

    @Test
    void testSplitByHighValue() {
        Map<Boolean, List<Order>> result = App.splitByHighValue(orders,34);
        List<Order> trueList = result.get(true);
        List<Order> falseList = result.get(false);
        Order order = new Order(8,CANCELLED,46.0);
        assertTrue(trueList.contains(order));
        assertFalse(falseList.contains(order));

    }

    @Test
    void testSummarize() {
       String result = App.summarize(orders);
       String expected = "1,2,3,4,5,6,7,8,9,10";
       assertEquals(result,expected);
    }


    @Test
    void pendingAndHighValue() {
       List<Order> actualResults = App.pendingAndHighValue(orders,61.0);
       List<Order> expectedResults = List.of(new Order(2,PENDING,89));
       assertEquals(expectedResults,actualResults);
    }

    @Test
    void sumUsingReduce() {
        Double actualResults = App.sumUsingReduce(orders);
        Double expected = 442.0;
        assertEquals(expected,actualResults,0.01);
    }

    @Test
    void hasOverdueHighValue() {
       boolean actaulResults = App.hasOverdueHighValue(orders,61.0);
       assertTrue(actaulResults);
    }
}