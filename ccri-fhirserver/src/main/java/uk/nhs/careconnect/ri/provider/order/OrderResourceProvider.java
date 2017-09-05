package uk.nhs.careconnect.ri.provider.order;


import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Order;
import org.hl7.fhir.instance.model.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.order.OrderStore;
import uk.nhs.careconnect.ri.model.order.OrderDetail;

import java.util.Date;

@Component
public class OrderResourceProvider implements IResourceProvider {

    @Autowired
    private OrderStore orderStore;

    @Override
    public Class<Order> getResourceType() {
        return Order.class;
    }

    @Create
    public MethodOutcome createOrder(@ResourceParam Order order) {
        OrderDetail orderDetail = orderResourceToOrderDetailConverter(order);
        orderDetail = orderStore.saveOrder(orderDetail);
        // Build response containing the new resource id
        MethodOutcome methodOutcome = new MethodOutcome();
        methodOutcome.setId(new IdDt("Order", orderDetail.getId()));
        methodOutcome.setResource(orderDetailToOrderResourceConverter(orderDetail));
        methodOutcome.setCreated(Boolean.TRUE);
        return methodOutcome;
    }

    public OrderDetail orderResourceToOrderDetailConverter(Order order){

        // TODO mock up
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setId(Long.valueOf(order.getId()).longValue());
        orderDetail.setIdentifier(order.getIdentifier().get(0).getValue());
       // orderDetail.setDetail(order.getContained().getContainedResources().get(0).getText().getDivAsString());
        orderDetail.setOrderDate(new Date());
        orderDetail.setReasonCode(((CodeableConcept)order.getReason()).getCoding().get(0).getCode());
        orderDetail.setReasonDescription(((CodeableConcept)order.getReason()).getCoding().get(0).getDisplay());
        orderDetail.setReasonText(((CodeableConcept)order.getReason()).getText());
        //orderDetail.setSourceOrgId(order.getSource().getReference().);
        //orderDetail.setSubjectPatientId(order.getSubject().getReference().getIdPartAsLong());
        //orderDetail.setTargetOrgId(order.getTarget().getReference().getIdPartAsLong());
        orderDetail.setRecieved(true);
        return orderDetail;
    }

    public Order orderDetailToOrderResourceConverter(OrderDetail orderDetail){
        Order order = new Order();
        order.setId(new IdDt(orderDetail.getId()));
        order.addIdentifier(new Identifier().setSystem("Not done TODO").setValue(orderDetail.getIdentifier()));

        /* REMOVED KGM
        Basic basic = new Basic();
        basic.setCode(new CodeableConceptDt(SystemURL.HL7_BASIC_RESOURCE_TYPE, "OrderDetails"));
        basic.getText().setDiv(new XhtmlDt(orderDetail.getDetail()));
        order.setDetail(Collections.singletonList(new ResourceReferenceDt(basic)));

        order.setDate(new DateTimeDt(orderDetail.getOrderDate()));
        CodingDt coding = new CodingDt(SystemURL.VS_GPC_REASON_TYPE,orderDetail.getReasonCode());
        coding.setDisplay(orderDetail.getReasonDescription());
        CodeableConceptDt codeConcept = new CodeableConceptDt().setCoding(Collections.singletonList(coding)).setText(orderDetail.getReasonText());
        order.setReason(codeConcept);
        */
        order.setSource(new Reference("Organization/"+orderDetail.getSourceOrgId()));
        order.setSubject(new Reference("Patient/"+orderDetail.getSubjectPatientId()));
        order.setTarget(new Reference("Organization/"+orderDetail.getTargetOrgId()));
        return order;
    }
}
