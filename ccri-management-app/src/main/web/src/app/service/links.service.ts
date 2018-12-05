import { Injectable } from '@angular/core';

@Injectable()
export class LinksService {

  constructor() { }

  isSNOMED(system: string): boolean {
    if (system == undefined) return false;
    if (system == "http://snomed.info/sct")
      return true;

  }

  getCodeSystem(system: string): string {
    switch(system) {
      case "http://snomed.info/sct": return "SNOMED";
      case "http://loinc.org": return "LOINC";
      case "http://hl7.org/fhir/sid/cvx": return "CVX (vaccine administered)";
      case "http://www.nlm.nih.gov/research/umls/rxnorm": return "RxNorm (USA Drug CodeSystem)";
      case "http://unitsofmeasure.org": return "Units Of Measure";
      case "https://fhir.hl7.org.uk/STU3/CodeSystem/CareConnect-ConditionCategory-1": return "FHIR ConditionCategory";
      default:
        //console.log(system);
        return system;
    }
  }

  getDMDLink(code: fhir.Coding) {
    return 'http://dmd.medicines.org.uk/DesktopDefault.aspx?VMP='+code.code+'&toc=nofloat';
  }

  getSNOMEDLink(code: fhir.Coding): string {

    if (this.isSNOMED(code.system)) {
//&server=https://termbrowser.nhs.uk/sct-browser-api/snomed&langRefset=999000681000001101,999001251000000103
      return "https://termbrowser.nhs.uk/?perspective=full&conceptId1="+code.code+"&edition=uk-edition&server=https://termbrowser.nhs.uk/sct-browser-api/snomed&langRefset=999000681000001101,999001251000000103";

    } else {
      return null;
    }
  }

}
