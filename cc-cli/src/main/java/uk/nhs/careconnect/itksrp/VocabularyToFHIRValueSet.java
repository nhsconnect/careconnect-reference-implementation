package uk.nhs.careconnect.itksrp;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.lang3.math.NumberUtils;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.ValueSet;


public class VocabularyToFHIRValueSet  {

	public VocabularyToFHIRValueSet(FhirContext ctx)
	{
		this.ctx = ctx;
	}
	private FhirContext ctx;
	

	public ValueSet process(Vocabulary vocab, String vocabName, String prefix)  {

       ValueSet valueSet = new ValueSet();
		

		switch(vocab.getStatus())
		{
			case "active" :
			case "Active" :
			case "created" :
				break;
			default:
		}


        vocabName = prefix +vocabName;
        String system = new String();
        system="https://fhir-dev.nhs.uk/"+vocabName;
		
		if (vocab.getId().contains("."))
		{
				if (vocab.getId().equals("2.16.840.1.113883.2.1.3.2.4.15"))
				{
					system="http://snomed.info/sct";
				}
		}
		else if (NumberUtils.isNumber(vocab.getId())) 
		{
			system = "http://snomed.info/sct";

		}
		else
		{
			// May not be robust
			system = "http://snomed.info/sct";
		}

		String idStr = vocabName;

        valueSet.setId(idStr);

        valueSet.setUrl("https://fhir-dev.nhs.uk/ValueSet/"+idStr);

     //   valueSet.getCodeSystem().setSystem(system);
        valueSet.setName(vocab.name);
        String desc = vocab.getDescription();
        valueSet.setDescription(desc);
        valueSet.setVersion(vocab.getVersion());

        switch(vocab.getStatus())
        {
            case "active" :
            case "Active" :
            case "created" :
                valueSet.setStatus(Enumerations.PublicationStatus.ACTIVE);
                break;
            case "superseded" :
                valueSet.setStatus(Enumerations.PublicationStatus.RETIRED);
                break;
            default:
                valueSet.setStatus(Enumerations.PublicationStatus.NULL);
        }

		
		if (system.equals("http://snomed.info/sct"))
		{

            System.out.println("if snomed");

			ValueSet.ConceptSetComponent concepts = new ValueSet.ConceptSetComponent();
			concepts.setSystem(system);


			
		//	exchange.getIn().setHeader("ActiveStatus", vocab.getStatus());



			
			for (int f=0;f<vocab.getConcept().size();f++)
			{
				ValueSet.ConceptReferenceComponent code = new ValueSet.ConceptReferenceComponent();
				code.setCode(vocab.getConcept().get(f).getCode().toString());
				for (int g=0;g<vocab.getConcept().get(f).getDisplayName().size();g++)
				{
					if (vocab.getConcept().get(f).getDisplayName().get(g).getType() != null)
					{
						if (vocab.getConcept().get(f).getDisplayName().get(g).getType().equals("PT"))
						{
							code.setDisplay(vocab.getConcept().get(f).getDisplayName().get(g).getValue());
						}
					}
					else
					{
						code.setDisplay(vocab.getConcept().get(f).getDisplayName().get(g).getValue());
						
					}
				}
				concepts.addConcept(code);
			}
			ValueSet.ValueSetComposeComponent comp = new ValueSet.ValueSetComposeComponent();
		
			comp.addInclude(concepts);
			
			valueSet.setCompose(comp);

		}
		else
		{

/* TODO This needs to be moved over to CodeSystem

           for (int f=0;f<vocab.getConcept().size();f++)
			{
				ValueSet.ConceptDefinitionComponent concept = new ValueSet.ConceptDefinitionComponent();
				
				concept.setCode(vocab.getConcept().get(f).getCode().toString());
				for (int g=0;g<vocab.getConcept().get(f).getDisplayName().size();g++)
				{
					if (vocab.getConcept().get(f).getDisplayName().get(g).getType() != null)
					{
						if (vocab.getConcept().get(f).getDisplayName().get(g).getType().equals("PT"))
						{
							concept.setDisplay(vocab.getConcept().get(f).getDisplayName().get(g).getValue());
						}
					}
					else
					{
						concept.setDisplay(vocab.getConcept().get(f).getDisplayName().get(g).getValue());
					}
				}
				valueSet.getCodeSystem().addConcept(concept);
			}
*/
		}

		return valueSet;
	}

}
