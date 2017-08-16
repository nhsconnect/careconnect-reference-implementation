/*
 * Copyright 2015 Ripple OSI
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package uk.gov.hscic.patient.details;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hscic.model.patient.PatientDetails;
import uk.gov.hscic.model.patient.PatientSummary;

@Service
@Transactional
public class PatientSearch {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientEntityToDetailsTransformer patientEntityToDetailsTransformer;

    @Autowired
    private PatientEntityToSummaryTransformer patientEntityToSummaryTransformer;

    public List<PatientSummary> findAllPatients() {
        final Iterable<PatientEntity> patients = patientRepository.findAll(new Sort("nhsNumber"));

        return CollectionUtils.collect(patients, patientEntityToSummaryTransformer, new ArrayList<>());
    }

    public PatientDetails findPatient(final String patientNHSNumber) {
        final PatientEntity patient = patientRepository.findByNhsNumber(patientNHSNumber);

        return patient == null
                ? null
                : patientEntityToDetailsTransformer.transform(patient);
    }

    public PatientDetails findPatientByInternalID(final String internalID) {
        final PatientEntity patient = patientRepository.findById(Long.valueOf(internalID));

        return patient == null
                ? null
                : patientEntityToDetailsTransformer.transform(patient);
    }

    public PatientSummary findPatientSummary(final String patientId) {
        final PatientEntity patient = patientRepository.findByNhsNumber(patientId);

        return patient == null
                ? null
                : patientEntityToSummaryTransformer.transform(patient);
    }
}
