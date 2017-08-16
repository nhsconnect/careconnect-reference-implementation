package uk.gov.hscic.appointment.schedule;

import org.apache.commons.collections4.Transformer;
import uk.gov.hscic.model.appointment.ScheduleDetail;

public class ScheduleEntityToScheduleDetailTransformer implements Transformer<ScheduleEntity, ScheduleDetail> {

    @Override
    public ScheduleDetail transform(ScheduleEntity item) {
        ScheduleDetail scheduleDetail = new ScheduleDetail();
        scheduleDetail.setId(item.getId());
        scheduleDetail.setPractitionerId(item.getPractitionerId());
        scheduleDetail.setIdentifier(item.getIdentifier());
        scheduleDetail.setTypeCode(item.getTypeCode());
        scheduleDetail.setTypeDescription(item.getTypeDescription());
        scheduleDetail.setLocationId(item.getLocationId());
        scheduleDetail.setStartDateTime(item.getStartDateTime());
        scheduleDetail.setEndDateTime(item.getEndDateTime());
        scheduleDetail.setComment(item.getComment());
        scheduleDetail.setLastUpdated(item.getLastUpdated());
        return scheduleDetail;
    }
}
