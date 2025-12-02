package com.myorg.trading.controller;

import com.myorg.trading.controller.dto.PlaceOrderRequest;
import com.myorg.trading.controller.dto.PlaceOrderResponse;
import com.myorg.trading.controller.dto.ScheduleOrderRequest;
import com.myorg.trading.domain.entity.Order;
import com.myorg.trading.domain.entity.OrderStatus;
import com.myorg.trading.domain.entity.ScheduledOrder;
import com.myorg.trading.service.trading.OrderService;
import com.myorg.trading.domain.repository.OrderRepository;
import com.myorg.trading.service.user.UserService; // <--- NEW IMPORT
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final UserService userService; // <--- NEW FIELD

    public OrderController(OrderService orderService,
                           OrderRepository orderRepository,
                           UserService userService) { // <--- NEW ARGUMENT
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.userService = userService;
    }

    @PostMapping("/place")
    public ResponseEntity<PlaceOrderResponse> placeOrder(@AuthenticationPrincipal UserDetails user,
                                                         @Valid @RequestBody PlaceOrderRequest req) {
        Order o = Order.builder()
                .userId(getUserIdFromPrincipal(user))
                .brokerAccountId(req.getBrokerAccountId())
                .symbol(req.getSymbol())
                .side(req.getSide().name())
                .quantity(req.getQuantity())
                .price(req.getPrice())
                .orderType(req.getOrderType().name())
                .status(OrderStatus.PENDING)
                .build();

        Order saved = orderService.createOrder(o);
        orderService.placeOrderNow(saved.getId());

        return ResponseEntity.ok(new PlaceOrderResponse(saved.getId(), "CREATED"));
    }

    @PostMapping("/schedule")
    public ResponseEntity<?> scheduleOrder(@AuthenticationPrincipal UserDetails user,
                                           @Valid @RequestBody ScheduleOrderRequest req) throws Exception {
        Order o = Order.builder()
                .userId(getUserIdFromPrincipal(user))
                .brokerAccountId(req.getBrokerAccountId())
                .symbol(req.getSymbol())
                .side(req.getSide().name())
                .quantity(req.getQuantity())
                .price(req.getPrice())
                .orderType(req.getOrderType().name())
                .status(OrderStatus.PENDING)
                .build();

        Order saved = orderService.createOrder(o);
        Instant when = req.getTriggerTime();
        ScheduledOrder so = orderService.scheduleOrder(saved.getId(), when);
        return ResponseEntity.ok(so);
    }

    @GetMapping
    public ResponseEntity<List<Order>> listOrders(@AuthenticationPrincipal UserDetails user) {
        Long userId = getUserIdFromPrincipal(user);
        List<Order> orders = orderService.getOrdersForUser(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        Order o = orderRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(o);
    }

    private Long getUserIdFromPrincipal(UserDetails user) {
        // FIX: Look up ID from database
        return userService.getUserIdForUsername(user.getUsername());
    }
}