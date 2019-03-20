package uk.nhs.careconnect.ri.database.entity;


/*
 * #%L
 * HAPI FHIR JPA Server
 * %%
 * Copyright (C) 2014 - 2019 University Health Network
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.commons.lang3.Validate;

//import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.dstu3.model.BooleanType;

//import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.UriType;

import java.util.ArrayList;
import java.util.List;

public class TranslationRequests {
	private CodeableConcept myCodeableConcept;
	private Long myResourceId;
	private BooleanType myReverse;
	private UriType mySource;
	private UriType myTarget;
	private UriType myTargetSystem;

	public TranslationRequests() {
		super();

		myCodeableConcept = new CodeableConcept();
	}

	/**
	 * This is just a convenience method that creates a codeableconcept if one
	 * doesn't already exist, and adds a coding to it
	 */
	public TranslationRequests addCode(String theSystem, String theCode) {
		Validate.notBlank(theSystem, "theSystem must not be null");
		Validate.notBlank(theCode, "theCode must not be null");
		if (getCodeableConcept() == null) {
			setCodeableConcept(new CodeableConcept());
		}
		getCodeableConcept().addCoding(new Coding().setSystem(theSystem).setCode(theCode));
		return this;
	}

	public CodeableConcept getCodeableConcept() {
		return myCodeableConcept;
	}

	public TranslationRequests setCodeableConcept(CodeableConcept theCodeableConcept) {
		myCodeableConcept = theCodeableConcept;
		return this;
	}

	public Long getResourceId() {
		return myResourceId;
	}

	public void setResourceId(Long theResourceId) {
		myResourceId = theResourceId;
	}

	public BooleanType getReverse() {
		return myReverse;
	}

	public void setReverse(BooleanType theReverse) {
		myReverse = theReverse;
	}

	public boolean getReverseAsBoolean() {
		if (hasReverse()) {
			return myReverse.booleanValue();
		}

		return false;
	}

	public UriType getSource() {
		return mySource;
	}

	public TranslationRequests setSource(UriType theSource) {
		mySource = theSource;
		return this;
	}

	public UriType getTarget() {
		return myTarget;
	}

	public TranslationRequests setTarget(UriType theTarget) {
		myTarget = theTarget;
		return this;
	}

	public UriType getTargetSystem() {
		return myTargetSystem;
	}

	public TranslationRequests setTargetSystem(UriType theTargetSystem) {
		myTargetSystem = theTargetSystem;
		return this;
	}

	public List<TranslationQueries> getTranslationQueries() {
		List<TranslationQueries> retVal = new ArrayList<>();

		TranslationQueries translationQueries;
		for (Coding coding : getCodeableConcept().getCoding()) {
			translationQueries = new TranslationQueries();

			translationQueries.setCoding(coding);

			System.out.println("translation queries " + this.getCodeableConcept().getId() + " ,target system :" + this.getTargetSystem());	
			if (this.hasResourceId()) {
				translationQueries.setResourceId(this.getResourceId());
			}

			if (this.hasSource()) {
				translationQueries.setSource(this.getSource());
			}

			if (this.hasTarget()) {
				translationQueries.setTarget(this.getTarget());
			}

			if (this.hasTargetSystem()) {
				translationQueries.setTargetSystem(this.getTargetSystem());
			}

			retVal.add(translationQueries);
		}

		return retVal;
	}

	public boolean hasResourceId() {
		return myResourceId != null;
	}

	public boolean hasReverse() {
		return myReverse != null;
	}

	public boolean hasSource() {
		return mySource != null && mySource.hasValue();
	}

	public boolean hasTarget() {
		return myTarget != null && myTarget.hasValue();
	}

	public boolean hasTargetSystem() {
		return myTargetSystem != null && myTargetSystem.hasValue();
	}

	public void setReverse(boolean theReverse) {
		myReverse = new BooleanType(theReverse);
	}
}
