package uk.nhs.careconnect.ri.database.daointerface.transforms;

import org.hl7.fhir.dstu3.model.Organization;
import org.junit.Test;
import uk.nhs.careconnect.ri.dao.transforms.BaseAddressToFHIRAddressTransformer;
import uk.nhs.careconnect.ri.dao.transforms.OrganisationEntityToFHIROrganizationTransformer;
import uk.nhs.careconnect.ri.database.daointerface.transforms.builder.OrganisationEntityBuilder;
import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class OrganisationEntityToFHIROrganizationTransformerTest {

    OrganisationEntityToFHIROrganizationTransformer transformer = new OrganisationEntityToFHIROrganizationTransformer(new BaseAddressToFHIRAddressTransformer());

    @Test
    public void testOrganisationTransform(){

        OrganisationEntity organisationEntity = new OrganisationEntityBuilder().build();
        Organization organisation = transformer.transform(organisationEntity);

        assertThat(organisation, not(nullValue()));
        assertThat(organisation.getId(), equalTo((new Long(OrganisationEntityBuilder.DEFAULT_ID)).toString()));
    }

}