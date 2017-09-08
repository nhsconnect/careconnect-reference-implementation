package uk.nhs.careconnect.ri.dao.ValueSet;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.instance.model.ValueSet;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.Terminology.ValueSetEntity;


@Component
public class ValueSetEntityToFHIRValueSetTransformer implements Transformer<ValueSetEntity, ValueSet> {

    @Override
    public ValueSet transform(final ValueSetEntity valueSetEntity) {
        final ValueSet valueSet = new ValueSet();


    return valueSet;
    }
}
