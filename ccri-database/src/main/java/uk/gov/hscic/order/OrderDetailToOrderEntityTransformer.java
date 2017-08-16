package uk.gov.hscic.order;

import org.apache.commons.collections4.Transformer;
import uk.gov.hscic.model.order.OrderDetail;

public class OrderDetailToOrderEntityTransformer implements Transformer<OrderDetail, OrderEntity> {

    @Override
    public OrderEntity transform(OrderDetail item) {
        OrderEntity order = new OrderEntity();
        order.setId(item.getId());
        order.setIdentifier(item.getIdentifier());
        order.setOrderDate(item.getOrderDate());
        order.setSubjectPatientId(item.getSubjectPatientId());
        order.setSourceOrgId(item.getSourceOrgId());
        order.setTargetOrgId(item.getTargetOrgId());
        order.setReasonCode(item.getReasonCode());
        order.setReasonDescription(item.getReasonDescription());
        order.setReasonText(item.getReasonText());
        order.setDetail(item.getDetail());
        order.setRecieved(item.getRecieved());
        return order;
    }
}
