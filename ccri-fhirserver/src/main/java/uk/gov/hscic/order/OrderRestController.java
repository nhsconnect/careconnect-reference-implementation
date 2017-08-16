package uk.gov.hscic.order;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hscic.model.order.OrderDetail;

@RestController
@RequestMapping("api/notfhir/orders")
public class OrderRestController {

    @Autowired
    private OrderSearch orderSearch;

    @Autowired
    private OrderStore orderStore;

    @GetMapping("/patient/{patientId}")
    public List<OrderDetail> findOrdersByPatientId(
            @PathVariable("patientId") Long patientId,
            @RequestParam(value = "recieved", required = false, defaultValue = "true") boolean recieved,
            @RequestParam(value = "sent", required = false, defaultValue = "false") boolean sent) {
        return orderSearch.findOrdersForPatientId(patientId)
                .stream()
                .filter(order -> (order.getRecieved() && recieved) || (!order.getRecieved() && sent))
                .collect(Collectors.toList());
    }

    @PostMapping("/order")
    public void saveSentOrders(@RequestBody OrderDetail orderDetail) {
        orderDetail.setRecieved(false);
        orderDetail.setOrderDate(new Date());
        orderStore.saveOrder(orderDetail);
    }
}
