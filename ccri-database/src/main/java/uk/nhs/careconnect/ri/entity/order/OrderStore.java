package uk.nhs.careconnect.ri.entity.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.nhs.careconnect.ri.model.order.OrderDetail;

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
