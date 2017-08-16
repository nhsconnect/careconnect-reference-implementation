package uk.gov.hscic.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hscic.model.order.OrderDetail;
import uk.gov.hscic.order.OrderEntity;
import uk.gov.hscic.order.OrderRepository;
import uk.gov.hscic.order.OrderEntityToOrderDetailTransformer;

@Service
public class OrderStore {
    private final OrderEntityToOrderDetailTransformer entityToDetailTransformer = new OrderEntityToOrderDetailTransformer();
    private final OrderDetailToOrderEntityTransformer detailToEntityTransformer = new OrderDetailToOrderEntityTransformer();

    @Autowired
    private OrderRepository orderRepository;

    public OrderDetail saveOrder(OrderDetail orderDetail) {
        OrderEntity orderEntity = detailToEntityTransformer.transform(orderDetail);
        orderEntity = orderRepository.saveAndFlush(orderEntity);
        return entityToDetailTransformer.transform(orderEntity);
    }

    public void clearOrders() {
        orderRepository.deleteAll();
    }
}
