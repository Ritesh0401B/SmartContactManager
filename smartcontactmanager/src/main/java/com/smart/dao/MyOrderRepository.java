package com.smart.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smart.entities.MyOrder;
import java.util.List;


public interface MyOrderRepository extends JpaRepository<MyOrder, Long> {
	
	public MyOrder findByOrderId(String orderId);

}
