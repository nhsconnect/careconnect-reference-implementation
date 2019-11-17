package uk.nhs.careconnect.ri.stu3.dao.transforms;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.graphDefinition.*;


@Component
public class GraphDefinitionEntityToFHIRGraphDefinitionTransformer implements Transformer<GraphDefinitionEntity, GraphDefinition> {

    private static final Logger log = LoggerFactory.getLogger(GraphDefinitionEntityToFHIRGraphDefinitionTransformer.class);

    @Override
    public GraphDefinition transform(final GraphDefinitionEntity graphEntity) {
        final GraphDefinition graph = new GraphDefinition();


        graph.setId(graphEntity.getId().toString());

        graph.setUrl(graphEntity.getUrl());

        if (graphEntity.getVersion() != null) {
            graph.setVersion(graphEntity.getVersion());
        }

        graph.setName(graphEntity.getName());

      
        switch(graphEntity.getStatus()) {
            case NULL:
                graph.setStatus(Enumerations.PublicationStatus.NULL);
                break;
            case ACTIVE:
                graph.setStatus(Enumerations.PublicationStatus.ACTIVE);
                break;
            case DRAFT:
                graph.setStatus(Enumerations.PublicationStatus.DRAFT);
                break;
            case RETIRED:
                graph.setStatus(Enumerations.PublicationStatus.RETIRED);
                break;
            case UNKNOWN:
                graph.setStatus(Enumerations.PublicationStatus.UNKNOWN);
                break;
        }


        if (graphEntity.getExperimental() != null) {
            graph.setExperimental(graphEntity.getExperimental());
        }

        if (graphEntity.getDateTime() != null) {
            graph.setDate(graphEntity.getDateTime());
        }
        if (graphEntity.getPublisher() != null) {
            graph.setPublisher(graphEntity.getPublisher());
        }

        for (GraphDefinitionTelecom telecom : graphEntity.getContacts()) {
            graph.addContact()
                    .addTelecom()
                    .setUse(convertSTU3(telecom.getTelecomUse()))
                    .setValue(telecom.getValue())
                    .setSystem(convertSTU3(telecom.getSystem()));
        }

        graph.setDescription(graphEntity.getDescription());

        if (graphEntity.getPurpose() != null) {
            graph.setPurpose(graphEntity.getPurpose());
        }

        if (graphEntity.getStart() != null) {
            graph.setStart(graphEntity.getStart());
        }
        if (graphEntity.getProfile() != null) {
            graph.setProfile(graphEntity.getProfile());
        }

        for (GraphDefinitionLink graphLink : graphEntity.getLinks()) {
            GraphDefinition.GraphDefinitionLinkComponent link = graph.addLink();
            buildLinks(link, graphLink);
        }

        log.trace("GraphDefinitionEntity name ="+graphEntity.getName());

        return graph;

    }

    private void buildLinks(GraphDefinition.GraphDefinitionLinkComponent link , GraphDefinitionLink graphLink) {
        if (graphLink.getPath() != null) {
            link.setPath(graphLink.getPath());
        }
        if (graphLink.getSlice() != null) {
            link.setSliceName(graphLink.getSlice());
        }
        if (graphLink.getMinimum() != null) {
            link.setMin(graphLink.getMinimum());
        }
        if (graphLink.getMaximum() != null) {
            link.setMax(graphLink.getMaximum());
        }
        if (graphLink.getDescription() != null) {
            link.setDescription(graphLink.getDescription());
        }
        if (graphLink.getSourceId() != null) {
            link.setId(graphLink.getSourceId());
            //Extension params = new Extension("https://fhir.mayfield-is.co.uk/extension-GraphDefinition.sourceNodeId");
           // params.setValue(new StringType(graphLink.getSourceId()));
           // link.addExtension(params);
        }
        for (GraphDefinitionLinkTarget linkTarget : graphLink.getTargets()) {
            GraphDefinition.GraphDefinitionLinkTargetComponent component = link.addTarget();
            if (linkTarget.getType() != null) {
                component.setType(linkTarget.getType());
            }
            if (linkTarget.getProfile() != null) {
                component.setProfile(linkTarget.getProfile());
            }
            if (linkTarget.getParams() != null) {
                Extension params = new Extension("http://hl7.org/fhir/4.0/StructureDefinition/extension-GraphDefinition.link.target.params");
                params.setValue(new StringType(linkTarget.getParams()));
                component.addExtension(params);
            }
            if (linkTarget.getTargetId() != null) {
                Extension params = new Extension("https://fhir.mayfield-is.co.uk/extension-GraphDefinition.targetLinkId");
                params.setValue(new StringType(linkTarget.getTargetId()));
                component.addExtension(params);
            }
            for (GraphDefinitionLinkTargetCompartment compartment : linkTarget.getCompartments()) {
                GraphDefinition.GraphDefinitionLinkTargetCompartmentComponent compartmentComponent = component.addCompartment();
                if (compartment.getCode() != null) {
                    compartmentComponent.setCode(convertSTU3(compartment.getCode()));
                }
                if (compartment.getRule() != null) {
                    compartmentComponent.setRule(convertSTU3(compartment.getRule()));
                }
                if (compartment.getExpression() != null) {
                    compartmentComponent.setExpression(compartment.getExpression());
                }
                if (compartment.getDescription() != null) {
                    compartmentComponent.setDescription(compartment.getDescription());
                }
            }
            for (GraphDefinitionLink sublink : linkTarget.getLinks()) {
                GraphDefinition.GraphDefinitionLinkComponent linkComponent = component.addLink();
                buildLinks(linkComponent,sublink);
            }
        }
    }

       private ContactPoint.ContactPointUse convertSTU3(org.hl7.fhir.r4.model.ContactPoint.ContactPointUse use) {
        if (use == null) return null;
           switch (use) {
               case HOME:
                   return ContactPoint.ContactPointUse.HOME;
               case MOBILE:
                   return ContactPoint.ContactPointUse.MOBILE;

               case OLD:
                   return ContactPoint.ContactPointUse.OLD;
               case TEMP:
                   return ContactPoint.ContactPointUse.TEMP;
               case WORK:
                   return ContactPoint.ContactPointUse.WORK;
               case NULL:
               default:
                   return ContactPoint.ContactPointUse.NULL;
           }
       }


    private ContactPoint.ContactPointSystem convertSTU3(org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem system) {
       if (system == null) return null;
        switch(system) {
            case EMAIL:
                return ContactPoint.ContactPointSystem.EMAIL;
            case FAX:
                return ContactPoint.ContactPointSystem.FAX;
            case OTHER:
                return ContactPoint.ContactPointSystem.OTHER;
            case PAGER:
                return ContactPoint.ContactPointSystem.PAGER;
            case PHONE:
                return ContactPoint.ContactPointSystem.PHONE;
            case SMS:
                return ContactPoint.ContactPointSystem.SMS;
            case URL:
                return ContactPoint.ContactPointSystem.URL;
            case NULL:
            default:
                return ContactPoint.ContactPointSystem.NULL;
        }

    }
    private  GraphDefinition.CompartmentCode convertSTU3(org.hl7.fhir.r4.model.GraphDefinition.CompartmentCode code) {
        if (code == null) return null;
        switch(code) {
            case DEVICE:
                return GraphDefinition.CompartmentCode.DEVICE;
            case ENCOUNTER:
                return GraphDefinition.CompartmentCode.ENCOUNTER;
            case PATIENT:
                return GraphDefinition.CompartmentCode.PATIENT;
            case PRACTITIONER:
                return GraphDefinition.CompartmentCode.PRACTITIONER;
            case RELATEDPERSON:
                return GraphDefinition.CompartmentCode.RELATEDPERSON;
            case NULL:
            default:
                return GraphDefinition.CompartmentCode.NULL;

        }

    }


    private  GraphDefinition.GraphCompartmentRule convertSTU3(org.hl7.fhir.r4.model.GraphDefinition.GraphCompartmentRule rule) {
        if (rule == null) return null;
        switch(rule) {
            case CUSTOM:
                return GraphDefinition.GraphCompartmentRule.CUSTOM;
            case DIFFERENT:
                return GraphDefinition.GraphCompartmentRule.DIFFERENT;
            case IDENTICAL:
                return GraphDefinition.GraphCompartmentRule.IDENTICAL;
            case MATCHING:
                return GraphDefinition.GraphCompartmentRule.MATCHING;
            case NULL:
            default:
                return GraphDefinition.GraphCompartmentRule.NULL;

        }

    }


}
