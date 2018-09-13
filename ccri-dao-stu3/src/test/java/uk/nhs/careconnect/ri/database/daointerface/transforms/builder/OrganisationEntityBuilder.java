package uk.nhs.careconnect.ri.database.daointerface.transforms.builder;

import uk.nhs.careconnect.ri.database.entity.organization.OrganisationEntity;

public class OrganisationEntityBuilder {

    public static final long DEFAULT_ID = 310111L;

    private String name;

    public OrganisationEntity build(){
        OrganisationEntity organisationEntity = new OrganisationEntity();
        organisationEntity.setId(DEFAULT_ID);
        organisationEntity.setActive(true);
        if (name != null){
            organisationEntity.setName(name);
        }

        return organisationEntity;
    }

}
