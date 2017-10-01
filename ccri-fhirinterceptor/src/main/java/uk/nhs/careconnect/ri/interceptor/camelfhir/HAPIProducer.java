package uk.nhs.careconnect.ri.interceptor.camelfhir;

import org.apache.camel.*;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.util.AsyncProcessorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HAPIProducer implements Producer,AsyncProcessor {

  private static final Logger log = LoggerFactory.getLogger(HAPIProducer.class);

    private ExchangePattern exchangePattern = ExchangePattern.InOnly;

    private HAPIEndPoint fhirEndPoint;

   public HAPIProducer(HAPIEndPoint fhirEndPoint) {
      // super(fhirEndPoint);
       this.fhirEndPoint = fhirEndPoint;

   }
    @Override
    public void process(Exchange exchange) throws Exception {
        log.info("process(Exchange exchange)");
        exchange.getOut().copyFrom(exchange.getIn());
       System.out.println("FHIR Producer :"+exchange.getIn().getHeader(Exchange.HTTP_PATH));
        System.out.println(exchange.getIn().getBody());
        AsyncProcessorHelper.process(this, exchange);
    }

    @Override
    public boolean process(Exchange exchange, AsyncCallback callback) {
        log.info("process(Exchange exchange, AsyncCallback callback)");
        log.info("ASync "+callback.toString());
        exchange.setPattern(exchangePattern);
        callback.done(false);
        return true;
    }

    @Override
    public Exchange createExchange() {
        log.info("createExchange");
        return null;
    }

    @Override
    public Exchange createExchange(ExchangePattern pattern) {

        log.info("createExchange - pattern");
        CamelContext cml =null;
        Exchange exchange = new DefaultExchange(cml, pattern);
        return exchange;
    }

    @Override
    public Exchange createExchange(Exchange exchange) {
        log.info("createExchange - returns exchange");
        return exchange;
    }

    @Override
    public Endpoint getEndpoint() {
        return this.fhirEndPoint;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }


}
