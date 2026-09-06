package com.abhi;

import com.abhi.model.Order;
import com.abhi.model.Status;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.abhi.model.Status.*;

/**
 * Hello world!
 *
 */
public class App 
{
    public static Predicate<Order> amountFilter = order -> order.getAmount() > 0;
    public static void main( String[] args )
    {

        List<Order> orders = List.of(
                new Order(1,PENDING,56.0),
                new Order(2,PENDING,89),
                new Order(3,PENDING,42.0),

                new Order(4,SHIPPED,78.0),
                new Order(5,SHIPPED,61.0),
                new Order(6,SHIPPED,42.0),

                new Order(7,CANCELLED,55.0),
                new Order(8,CANCELLED,46.0),
                new Order(9,CANCELLED,36.0),
                new Order(10,CANCELLED,6.0)

        );

        System.out.println(getOrderByStatus(orders));

    }

    public static Map<Status, Double> getOrderByStatus(List<Order> orders){
        return orders.stream().filter(amountFilter).collect(Collectors.groupingBy(Order::getStatus,Collectors.summingDouble(Order::getAmount)));
    }

    public static Map<Status, List<Long>> orderIdsByStatus(List<Order> orders){
        return orders.stream().collect(Collectors.groupingBy(Order::getStatus,Collectors.mapping(Order::getOrderId,Collectors.toList())));
    }

    public static Map<Status, Long> countByStatus(List<Order> orders){
        return orders.stream().collect(Collectors.groupingBy(Order::getStatus,Collectors.counting()));
    }
}
