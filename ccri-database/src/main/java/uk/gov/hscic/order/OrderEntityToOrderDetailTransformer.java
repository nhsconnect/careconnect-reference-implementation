package uk.gov.hscic.order;

import org.apache.commons.collections4.Transformer;
import uk.gov.hscic.model.order.OrderDetail;

public class OrderEntityToOrderDetailTransformer implements Transformer<OrderEntity, OrderDetail> {

    @Override
    public OrderDetail transform(OrderEntity item) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setId(item.getId());
        orderDetail.setIdentifier(item.getIdentifier());
        orderDetail.setOrderDate(item.getOrderDate());
        orderDetail.setSubjectPatientId(item.getSubjectPatientId());
        orderDetail.setSourceOrgId(item.getSourceOrgId());
        orderDetail.setTargetOrgId(item.getTargetOrgId());
        orderDetail.setReasonCode(item.getReasonCode());
        orderDetail.setReasonDescription(item.getReasonDescription());
        orderDetail.setReasonText(item.getReasonText());
        orderDetail.setDetail(item.getDetail());
        orderDetail.setRecieved(item.getRecieved());
        return orderDetail;
    }
}

