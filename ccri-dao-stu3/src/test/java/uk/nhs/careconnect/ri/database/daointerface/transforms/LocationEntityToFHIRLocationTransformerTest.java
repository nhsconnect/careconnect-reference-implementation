package uk.nhs.careconnect.ri.database.daointerface.transforms;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Location;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.nhs.careconnect.ri.dao.transforms.BaseAddressToFHIRAddressTransformer;
import uk.nhs.careconnect.ri.dao.transforms.LocationEntityToFHIRLocationTransformer;
import uk.nhs.careconnect.ri.database.daointerface.transforms.builder.LocationEntityBuilder;
import uk.nhs.careconnect.ri.database.entity.location.LocationEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(MockitoJUnitRunner.class)
public class LocationEntityToFHIRLocationTransformerTest {

    private LocationEntityToFHIRLocationTransformer transformer;

    @Before
    public void setup(){
        final BaseAddressToFHIRAddressTransformer addressTransformer = new BaseAddressToFHIRAddressTransformer();
        transformer = new LocationEntityToFHIRLocationTransformer(addressTransformer);
    }

    @Test
    public void testTransformLocationEntity(){

        LocationEntity locationEntity = new LocationEntityBuilder()
                .setName("Example Location")
                .addAddress("20 High Street", "Holmfirth", null,
                        "Halifax", "West Yorkshire", "HX1 2TT")
                .addHomePhone("0113240998")
                .build();

        Location location = transformer.transform(locationEntity);

        // Check that the Name has been populated
        assertThat(location, not(nullValue()));
        assertThat(location.getName(), equalTo("Example Location"));

        // Check that the Address has been populated
        Address address = location.getAddress();
        assertThat(address, not(nullValue()));
        assertThat(address.getLine().get(0).getValue(), equalTo("20 High Street"));
        assertThat(address.getLine().get(1).getValue(), equalTo("Holmfirth"));
        assertThat(address.getLine().size(),equalTo(2));
       // assertThat(address.getLine().get(3), nullValue());
        assertThat(address.getDistrict(), equalTo("West Yorkshire"));
        assertThat(address.getCity(), equalTo("Halifax"));
        assertThat(address.getPostalCode(), equalTo("HX1 2TT"));

        // Check that the Telephone Number has been populated
        assertThat(location.getTelecom(), not(nullValue()));
        assertThat(location.getTelecom().size(), equalTo(1));
        ContactPoint phoneNumber = location.getTelecom().get(0);
        assertThat(phoneNumber.getValue(), equalTo("0113240998"));
        assertThat(phoneNumber.getUse().getDisplay(), equalTo("Home"));
    }

}
