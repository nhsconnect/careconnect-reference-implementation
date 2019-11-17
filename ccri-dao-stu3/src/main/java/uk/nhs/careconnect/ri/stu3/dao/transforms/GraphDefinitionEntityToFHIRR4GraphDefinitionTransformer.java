package uk.nhs.careconnect.ri.stu3.dao.transforms;


import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.GraphDefinition;
import org.hl7.fhir.r4.model.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.database.entity.graphDefinition.*;


@Component
public class GraphDefinitionEntityToFHIRR4GraphDefinitionTransformer implements Transformer<GraphDefinitionEntity, GraphDefinition> {

    private static final Logger log = LoggerFactory.getLogger(GraphDefinitionEntityToFHIRR4GraphDefinitionTransformer.class);

    @Override
    public GraphDefinition transform(final GraphDefinitionEntity graphEntity) {
        final GraphDefinition graph = new GraphDefinition();


        graph.setId(graphEntity.getId().toString());

        graph.setUrl(graphEntity.getUrl());

        if (graphEntity.getVersion() != null) {
            graph.setVersion(graphEntity.getVersion());
        }

        graph.setName(graphEntity.getName());

      

        graph.setStatus(graphEntity.getStatus());

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
                    .setUse(telecom.getTelecomUse())
                    .setValue(telecom.getValue())
                    .setSystem(telecom.getSystem());
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
                component.setParams(linkTarget.getParams());
                /* Backport for STU3
                Extension params = new Extension("http://hl7.org/fhir/4.0/StructureDefinition/extension-GraphDefinition.link.target.params");
                params.setValue(new StringType(linkTarget.getParams()));
                component.addExtension(params); */
            }
            if (linkTarget.getTargetId() != null) {
                Extension params = new Extension("https://fhir.mayfield-is.co.uk/extension-GraphDefinition.targetLinkId");
                params.setValue(new StringType(linkTarget.getTargetId()));
                component.addExtension(params);
            }
            for (GraphDefinitionLinkTargetCompartment compartment : linkTarget.getCompartments()) {
                GraphDefinition.GraphDefinitionLinkTargetCompartmentComponent compartmentComponent = component.addCompartment();
                if (compartment.getCode() != null) {
                    compartmentComponent.setCode(compartment.getCode());
                }
                if (compartment.getRule() != null) {
                    compartmentComponent.setRule(compartment.getRule());
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

}
