package uk.nhs.careconnect.ri.gateway.provider;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.context.RuntimeSearchParam;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.rest.annotation.Metadata;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.server.IServerConformanceProvider;
import ca.uhn.fhir.rest.server.ResourceBinding;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.RestulfulServerConfiguration;
import ca.uhn.fhir.rest.server.method.BaseMethodBinding;
import ca.uhn.fhir.rest.server.method.IParameter;
import ca.uhn.fhir.rest.server.method.SearchMethodBinding;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Enumerations;
import ca.uhn.fhir.rest.server.method.SearchParameter;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import uk.org.hl7.fhir.core.Stu3.CareConnectProfile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.*;


public class CareConnectConformanceProvider implements IServerConformanceProvider<CapabilityStatement> {

    /*

    This is a stripped down version of package org.hl7.fhir.dstu3.hapi.rest.server
    public class ServerCapabilityStatementProvider

    We remove StructureDefinition and OperationOutcome
    Also add in the Care Connect profiles instead of the baseProfiles.

     */

    private RestulfulServerConfiguration serverConfiguration;

    private volatile CapabilityStatement capabilityStatement;

    private static final Logger log = LoggerFactory.getLogger(CareConnectConformanceProvider.class);

    @Value("${fhir.resource.serverName}")
    private String serverName;

    @Value("${fhir.resource.serverVersion}")
    private String serverVersion;

    @Override
    @Metadata
    public CapabilityStatement getServerConformance(HttpServletRequest theRequest) {
        if (capabilityStatement != null ) {
            return capabilityStatement;
        }

        CapabilityStatement retVal = new CapabilityStatement();

        capabilityStatement = retVal;

        retVal.setPublisher("NHS Digital");
        retVal.setDateElement(conformanceDate());
        retVal.setFhirVersion(FhirVersionEnum.DSTU3.getFhirVersionString());
        retVal.setAcceptUnknown(CapabilityStatement.UnknownContentCode.EXTENSIONS); // TODO: make this configurable - this is a fairly big
        // effort since the parser
        // needs to be modified to actually allow it

        retVal.getImplementation().setDescription(serverConfiguration.getImplementationDescription());
        retVal.setKind(CapabilityStatement.CapabilityStatementKind.INSTANCE);

        // TODO KGM move to config
        retVal.getSoftware().setName("Care Connect RI FHIR Server");
        retVal.getSoftware().setVersion("3.3.6-SNAPSHOT");
        retVal.addFormat(Constants.CT_FHIR_XML_NEW);
        retVal.addFormat(Constants.CT_FHIR_JSON_NEW);
        retVal.setStatus(Enumerations.PublicationStatus.ACTIVE);

        CapabilityStatement.CapabilityStatementRestComponent rest = retVal.addRest();

        rest.setMode(CapabilityStatement.RestfulCapabilityMode.SERVER);

        Map<String, List<BaseMethodBinding<?>>> resourceToMethods = collectMethodBindings();
        for (Map.Entry<String, List<BaseMethodBinding<?>>> nextEntry : resourceToMethods.entrySet()) {

            if (nextEntry.getKey().isEmpty() == false) {
                Set<CapabilityStatement.TypeRestfulInteraction> resourceOps = new HashSet<CapabilityStatement.TypeRestfulInteraction>();
                CapabilityStatement.CapabilityStatementRestResourceComponent resource = rest.addResource();
                String resourceName = nextEntry.getKey();
                RuntimeResourceDefinition def = serverConfiguration.getFhirContext().getResourceDefinition(resourceName);
                resource.getTypeElement().setValue(def.getName());
                ServletContext servletContext = (ServletContext) (theRequest == null ? null : theRequest.getAttribute(RestfulServer.SERVLET_CONTEXT_ATTRIBUTE));
                String serverBase = serverConfiguration.getServerAddressStrategy().determineServerBase(servletContext, theRequest);
                switch (resourceName) {
                    case "Patient":
                        resource.getProfile().setReference(CareConnectProfile.Patient_1);
                        break;
                    case "Practitioner":
                        resource.getProfile().setReference(CareConnectProfile.Practitioner_1);
                        break;
                    case "PractitionerRole":
                        resource.getProfile().setReference(CareConnectProfile.PractitionerRole_1);
                        break;
                    case "Organization":
                        resource.getProfile().setReference(CareConnectProfile.Organization_1);
                        break;
                    case "Location":
                        resource.getProfile().setReference(CareConnectProfile.Location_1);
                        break;
                    case "Observation":
                        resource.getProfile().setReference(CareConnectProfile.Observation_1);
                        break;
                    case "Encounter":
                        resource.getProfile().setReference(CareConnectProfile.Encounter_1);
                        break;
                    case "Condition":
                        resource.getProfile().setReference(CareConnectProfile.Condition_1);
                        break;
                    case "Procedure":
                        resource.getProfile().setReference(CareConnectProfile.Procedure_1);
                        break;
                    case "Immunization":
                        resource.getProfile().setReference(CareConnectProfile.Immunization_1);
                        break;
                    case "MedicationRequest":
                        resource.getProfile().setReference(CareConnectProfile.MedicationRequest_1);
                        break;
                    case "MedicationStatement":
                        resource.getProfile().setReference(CareConnectProfile.MedicationStatement_1);
                        break;
                    case "AllergyIntolerance":
                        resource.getProfile().setReference(CareConnectProfile.AllergyIntolerance_1);
                        break;
                    default:
                        resource.getProfile().setReference((def.getResourceProfile(serverBase)));
                }


                TreeSet<String> includes = new TreeSet<String>();


                for (BaseMethodBinding<?> nextMethodBinding : nextEntry.getValue()) {
                    if (nextMethodBinding.getRestOperationType() != null) {
                        String resOpCode = nextMethodBinding.getRestOperationType().getCode();
                        if (resOpCode != null) {
                            CapabilityStatement.TypeRestfulInteraction resOp;
                            try {
                                resOp = CapabilityStatement.TypeRestfulInteraction.fromCode(resOpCode);
                            } catch (Exception e) {
                                resOp = null;
                            }
                            if (resOp != null) {
                                if (resourceOps.contains(resOp) == false) {
                                    resourceOps.add(resOp);
                                    resource.addInteraction().setCode(resOp);
                                }
                                if ("vread".equals(resOpCode)) {
                                    // vread implies read
                                    resOp = CapabilityStatement.TypeRestfulInteraction.READ;
                                    if (resourceOps.contains(resOp) == false) {
                                        resourceOps.add(resOp);
                                        resource.addInteraction().setCode(resOp);
                                    }
                                }

                                if (nextMethodBinding.isSupportsConditional()) {
                                    switch (resOp) {
                                        case CREATE:
                                            resource.setConditionalCreate(true);
                                            break;
                                        case DELETE:
                                            if (nextMethodBinding.isSupportsConditionalMultiple()) {
                                                resource.setConditionalDelete(CapabilityStatement.ConditionalDeleteStatus.MULTIPLE);
                                            } else {
                                                resource.setConditionalDelete(CapabilityStatement.ConditionalDeleteStatus.SINGLE);
                                            }
                                            break;
                                        case UPDATE:
                                            resource.setConditionalUpdate(true);
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }
                        }
                        //checkBindingForSystemOps(rest, systemOps, nextMethodBinding);

                        if (nextMethodBinding instanceof SearchMethodBinding) {
                            handleSearchMethodBinding(rest, resource, resourceName, def, includes, (SearchMethodBinding) nextMethodBinding);
                        }
                    }
                }

            }
        }
        return retVal;
    }

    @Override
    public void setRestfulServer(RestfulServer theRestfulServer) {
        serverConfiguration = theRestfulServer.createConfiguration();
    }

    private DateTimeType conformanceDate() {
        String buildDate = serverConfiguration.getConformanceDate();
        if (buildDate != null) {
            try {
                return new DateTimeType(buildDate);
            } catch (DataFormatException e) {
                // fall through
            }
        }
        return DateTimeType.now();
    }

    private Map<String, List<BaseMethodBinding<?>>> collectMethodBindings() {
        Map<String, List<BaseMethodBinding<?>>> resourceToMethods = new TreeMap<>();
        for (ResourceBinding next : serverConfiguration.getResourceBindings()) {
            String resourceName = next.getResourceName();
            log.trace("CapabilityStatement=" + resourceName);
            switch (resourceName) {
               // Add supported resources here. This is suppressing StructureDefinition which is part of equivalent hapi class

                case "Patient" :
                case "Practitioner":
                case "PractitionerRole":
                case "Organization":
                case "Location":
                case "Observation":
                case "Encounter":
                case "EpisodeOfCare":
                case "Condition":
                case "Procedure":
                case "Immunization":
                case "MedicationRequest":
                case "MedicationStatement":
                case "AllergyIntolerance":


                for (BaseMethodBinding<?> nextMethodBinding : next.getMethodBindings()) {
                    if (resourceToMethods.containsKey(resourceName) == false) {
                        resourceToMethods.put(resourceName, new ArrayList<>());
                    }
                    resourceToMethods.get(resourceName).add(nextMethodBinding);

                }
            }
        }

        for (BaseMethodBinding<?> nextMethodBinding : serverConfiguration.getServerBindings()) {
            String resourceName = "";
            if (resourceToMethods.containsKey(resourceName) == false) {
                resourceToMethods.put(resourceName, new ArrayList<BaseMethodBinding<?>>());
            }
            resourceToMethods.get(resourceName).add(nextMethodBinding);
        }

        return resourceToMethods;
    }


    private void sortSearchParameters(List<SearchParameter> searchParameters) {
        Collections.sort(searchParameters, new Comparator<SearchParameter>() {
            @Override
            public int compare(SearchParameter theO1, SearchParameter theO2) {
                if (theO1.isRequired() == theO2.isRequired()) {
                    return theO1.getName().compareTo(theO2.getName());
                }
                if (theO1.isRequired()) {
                    return -1;
                }
                return 1;
            }
        });
    }

    private void handleSearchMethodBinding(CapabilityStatement.CapabilityStatementRestComponent rest, CapabilityStatement.CapabilityStatementRestResourceComponent resource, String resourceName, RuntimeResourceDefinition def, TreeSet<String> includes,
                                           SearchMethodBinding searchMethodBinding) {
        includes.addAll(searchMethodBinding.getIncludes());

        List<IParameter> params = searchMethodBinding.getParameters();
        List<SearchParameter> searchParameters = new ArrayList<SearchParameter>();
        for (IParameter nextParameter : params) {
            if ((nextParameter instanceof SearchParameter)) {
                searchParameters.add((SearchParameter) nextParameter);
            }
        }
        sortSearchParameters(searchParameters);
        if (!searchParameters.isEmpty()) {
            // boolean allOptional = searchParameters.get(0).isRequired() == false;
            //
            // OperationDefinition query = null;
            // if (!allOptional) {
            // RestOperation operation = rest.addOperation();
            // query = new OperationDefinition();
            // operation.setDefinition(new ResourceReferenceDt(query));
            // query.getDescriptionElement().setValue(searchMethodBinding.getDescription());
            // query.addUndeclaredExtension(false, ExtensionConstants.QUERY_RETURN_TYPE, new CodeDt(resourceName));
            // for (String nextInclude : searchMethodBinding.getIncludes()) {
            // query.addUndeclaredExtension(false, ExtensionConstants.QUERY_ALLOWED_INCLUDE, new StringDt(nextInclude));
            // }
            // }

            for (SearchParameter nextParameter : searchParameters) {

                String nextParamName = nextParameter.getName();

                String chain = null;
                String nextParamUnchainedName = nextParamName;
                if (nextParamName.contains(".")) {
                    chain = nextParamName.substring(nextParamName.indexOf('.') + 1);
                    nextParamUnchainedName = nextParamName.substring(0, nextParamName.indexOf('.'));
                }

                String nextParamDescription = nextParameter.getDescription();

				/*
				 * If the parameter has no description, default to the one from the resource
				 */
                if (StringUtils.isBlank(nextParamDescription)) {
                    RuntimeSearchParam paramDef = def.getSearchParam(nextParamUnchainedName);
                    if (paramDef != null) {
                        nextParamDescription = paramDef.getDescription();
                    }
                }

                CapabilityStatement.CapabilityStatementRestResourceSearchParamComponent param = resource.addSearchParam();
                param.setName(nextParamUnchainedName);

//				if (StringUtils.isNotBlank(chain)) {
//					param.addChain(chain);
//				}
//
//				if (nextParameter.getParamType() == RestSearchParameterTypeEnum.REFERENCE) {
//					for (String nextWhitelist : new TreeSet<String>(nextParameter.getQualifierWhitelist())) {
//						if (nextWhitelist.startsWith(".")) {
//							param.addChain(nextWhitelist.substring(1));
//						}
//					}
//				}

                param.setDocumentation(nextParamDescription);
                if (nextParameter.getParamType() != null) {
                    param.getTypeElement().setValueAsString(nextParameter.getParamType().getCode());
                }
                for (Class<? extends IBaseResource> nextTarget : nextParameter.getDeclaredTypes()) {
                    RuntimeResourceDefinition targetDef = serverConfiguration.getFhirContext().getResourceDefinition(nextTarget);
                    if (targetDef != null) {
                        ResourceType code;
                        try {
                            code = ResourceType.fromCode(targetDef.getName());
                        } catch (FHIRException e) {
                            code = null;
                        }
//						if (code != null) {
//							param.addTarget(targetDef.getName());
//						}
                    }
                }
            }
        }
    }


}
