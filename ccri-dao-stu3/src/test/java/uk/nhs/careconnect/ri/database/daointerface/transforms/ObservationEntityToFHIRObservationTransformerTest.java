package uk.nhs.careconnect.ri.database.daointerface.transforms;

import org.hl7.fhir.dstu3.model.Observation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.nhs.careconnect.ri.dao.transforms.ObservationEntityToFHIRObservationTransformer;
import uk.nhs.careconnect.ri.database.daointerface.transforms.builder.ObservationEntityBuilder;
import uk.nhs.careconnect.ri.database.entity.observation.ObservationEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(MockitoJUnitRunner.class)
public class ObservationEntityToFHIRObservationTransformerTest {

    ObservationEntityToFHIRObservationTransformer transformer = new ObservationEntityToFHIRObservationTransformer();

    @Test
    public void testTransformObservationEntity(){
        ObservationEntity observationEntity = new ObservationEntityBuilder()
                .build();
        Observation observation = transformer.transform(observationEntity);
        assertThat(observation, not(nullValue()));
        assertThat(observation.getId(), not(nullValue()));
        assertThat(observation.getId(), equalTo((new Long(ObservationEntityBuilder.DEFAULT_ID)).toString()));
        assertThat(observation.getSubject(), not(nullValue()));
        assertThat(observation.getSubject().getReference(), equalTo("Patient/100002"));
        assertThat(observation.getEffective(), not(nullValue()));
    }
}
