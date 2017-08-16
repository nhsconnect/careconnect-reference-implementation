package uk.gov.hscic.location;

import org.apache.commons.collections4.Transformer;
import uk.gov.hscic.model.location.LocationDetails;

class LocationEntityToLocationDetailsTransformer implements Transformer<LocationEntity, LocationDetails> {

	@Override
	public LocationDetails transform(LocationEntity locationEntity) {
		LocationDetails locationDetails = null;

		if (locationEntity != null) {
			locationDetails = new LocationDetails();

			locationDetails.setId(locationEntity.getId());
			locationDetails.setName(locationEntity.getName());
            locationDetails.setOrgOdsCode(locationEntity.getOrgOdsCode());
			locationDetails.setOrgOdsCodeName(locationEntity.getOrgOdsCodeName());
			locationDetails.setSiteOdsCode(locationEntity.getSiteOdsCode());
			locationDetails.setSiteOdsCodeName(locationEntity.getSiteOdsCodeName());
            locationDetails.setLastUpdated(locationEntity.getLastUpdated());
		}

		return locationDetails;
	}
}
