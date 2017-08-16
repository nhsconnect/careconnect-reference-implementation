package uk.gov.hscic.order;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Basic;
import ca.uhn.fhir.model.dstu2.resource.Order;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.XhtmlDt;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import java.util.Collections;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hscic.SystemURL;
import uk.gov.hscic.model.order.OrderDetail;

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
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setId(order.getId().getIdPartAsLong());
        orderDetail.setIdentifier(order.getIdentifierFirstRep().getValue());
        orderDetail.setDetail(order.getContained().getContainedResources().get(0).getText().getDivAsString());
        orderDetail.setOrderDate(new Date());
        orderDetail.setReasonCode(((CodeableConceptDt)order.getReason()).getCodingFirstRep().getCode());
        orderDetail.setReasonDescription(((CodeableConceptDt)order.getReason()).getCodingFirstRep().getDisplay());
        orderDetail.setReasonText(((CodeableConceptDt)order.getReason()).getText());
        orderDetail.setSourceOrgId(order.getSource().getReference().getIdPartAsLong());
        orderDetail.setSubjectPatientId(order.getSubject().getReference().getIdPartAsLong());
        orderDetail.setTargetOrgId(order.getTarget().getReference().getIdPartAsLong());
        orderDetail.setRecieved(true);
        return orderDetail;
    }

    public Order orderDetailToOrderResourceConverter(OrderDetail orderDetail){
        Order order = new Order();
        order.setId(new IdDt(orderDetail.getId()));
        order.setIdentifier(Collections.singletonList(new IdentifierDt("",orderDetail.getIdentifier())));

        Basic basic = new Basic();
        basic.setCode(new CodeableConceptDt(SystemURL.HL7_BASIC_RESOURCE_TYPE, "OrderDetails"));
        basic.getText().setDiv(new XhtmlDt(orderDetail.getDetail()));
        order.setDetail(Collections.singletonList(new ResourceReferenceDt(basic)));

        order.setDate(new DateTimeDt(orderDetail.getOrderDate()));
        CodingDt coding = new CodingDt(SystemURL.VS_GPC_REASON_TYPE,orderDetail.getReasonCode());
        coding.setDisplay(orderDetail.getReasonDescription());
        CodeableConceptDt codeConcept = new CodeableConceptDt().setCoding(Collections.singletonList(coding)).setText(orderDetail.getReasonText());
        order.setReason(codeConcept);

        order.setSource(new ResourceReferenceDt("Organization/"+orderDetail.getSourceOrgId()));
        order.setSubject(new ResourceReferenceDt("Patient/"+orderDetail.getSubjectPatientId()));
        order.setTarget(new ResourceReferenceDt("Organization/"+orderDetail.getTargetOrgId()));
        return order;
    }
}
