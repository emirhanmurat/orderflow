package com.orderflow.orderworker.repository;

import com.orderflow.orderworker.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {}
