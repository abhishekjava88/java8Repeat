package com.abhi;

import com.abhi.model.Order;
import com.abhi.model.Status;

import java.util.*;
import java.util.function.BiPredicate;
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
    public static Predicate<Order> pendingFilter = order -> order.getStatus() == PENDING;
    public static Comparator<Order> amountComparator = Comparator.comparing(Order::getAmount);
    public static Comparator<Order> StatusComparator = Comparator.comparing(Order::getStatus);
    public static Comparator<Order> statusAndAmountComparator = StatusComparator.thenComparing(amountComparator);


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
        System.out.println(splitByHighValue(orders,25));
        System.out.println(summarize(orders));
        System.out.println(pendingAndHighValue(orders,61));
        System.out.println(sumUsingReduce(orders));
        System.out.println(sortByStatusThenAmount(orders));

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

    public static Map<Boolean, List<Order>> splitByHighValue(List<Order> orders, double threshold){
        return orders.stream().collect(Collectors.partitioningBy(order -> order.getAmount()>threshold ));
    }


    public static String summarize(List<Order> orders){
        return orders.stream().map(order -> String.valueOf(order.getOrderId())).collect(Collectors.joining(","));
    }

    public static List<Order> pendingAndHighValue(List<Order> orders, double threshold){
        return orders.stream().filter(pendingFilter.and(order -> order.getAmount()>threshold)).toList();
    }

    public static double sumUsingReduce(List<Order> orders){
        return orders.stream().map(Order::getAmount).reduce(0.0,(a,b) -> a+b);
    }

    public static boolean hasOverdueHighValue(List<Order> orders, double threshold){
        return orders.stream().filter(pendingFilter).anyMatch(order -> order.getAmount()>threshold);
    }

    public static List<Order> topNByAmount(List<Order> orders, int n){
        return orders.stream().sorted(amountComparator.reversed()).limit(n).toList();
    }

    public static List<Order> sortByStatusThenAmount(List<Order> orders){
        return orders.stream().sorted(statusAndAmountComparator).toList();
    }

    public static Optional<Order> findOrderById(List<Order> orders, long orderId){
       return orders.stream().filter(order -> order.getOrderId() == orderId).findFirst();
    }

    public static double getOrderAmountOrDefault(List<Order> orders, long orderId, double defaultValue){
        return orders.stream().filter(order -> order.getOrderId() == orderId).map(order -> order.getAmount()).findFirst().orElse(defaultValue);
    }

    public static Order getOrderOrThrow(List<Order> orders, long orderId){
        return orders.stream().filter(order -> order.getOrderId() == orderId).findFirst().orElseThrow();
    }

    public static double getOrderAmountOrDefaultEager(List<Order> orders, long orderId, double defaultValue){
        return orders.stream().filter(order -> order.getOrderId() == orderId).map(order -> order.getAmount()).findFirst().orElse(computeExpensiveDefault());
    }

    public static double getOrderAmountOrDefaultLazy(List<Order> orders, long orderId, double defaultValue){
        return orders.stream().filter(order -> order.getOrderId() == orderId).map(order -> order.getAmount()).findFirst().orElse(computeExpensiveDefault());
    }

    private static double computeExpensiveDefault(){
        System.out.println("Connecting to Database");
        return 78.0;
    }

}
