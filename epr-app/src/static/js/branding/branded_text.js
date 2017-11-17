angular.module("sandManApp.branding", [], ["$provide", function($provide) {

    $provide.value("brandedText", {
            default: {
                mainImage: "https://content.hspconsortium.org/images/hspc-sandbox/logo/hspc-sndbx-logo.png",
                mainImage2x: "https://content.hspconsortium.org/images/hspc-sandbox/logo/hspc-sndbx-logo@2x.png 2x",
                whiteImage: "https://content.hspconsortium.org/images/hspc-sandbox/logo/hspc-sndbx-logo-wh.png",
                whiteImage2x: "https://content.hspconsortium.org/images/hspc-sandbox/logo/hspc-sndbx-logo-wh@2x.png 2x",
                mainTitle: "The Healthcare Innovation Ecosystem",
                sandboxText: "",
                moreLinks: true,
                dashboardTitle: "Dashboard",
                showEmptyInviteList: true,
                copyright: "© 2017 by Healthcare Services Platform Consortium",
                showCert: true,
                showTermsLink: true,
                userSettingsPWM: false,
                loginDoc: "",
                defaultApiEndpointIndex : "6",
                defaultLaunchScenario: true,
                sandboxApiEndpointIndexes : [
                    {index: "1", name: "FHIR DSTU 2 (v1.0.2)", fhirVersion: "1.0.2", fhirTag: "1_0_2", altName: "FHIR v1.0.2", canCreate: false, supportsDataSets: false},
                    {index: "2", name: "FHIR DSTU 2 (v1.6.0)", fhirVersion: "1.6.0", fhirTag: "1_0_2", altName: "FHIR v1.6", canCreate: false, supportsDataSets: false},
                    {index: "3", name: "FHIR STU 3 (v1.8.0)", fhirVersion: "1.8.0", fhirTag: "1_8_0", altName: "FHIR v1.8", canCreate: false, supportsDataSets: false},
                    {index: "4", name: "FHIR STU 3 (v3.0.1)", fhirVersion: "3.0.1", fhirTag: "3_0_1", altName: "FHIR v3.0.1", canCreate: false, supportsDataSets: true},
                    {index: "5", name: "FHIR DSTU 2 (v1.6.0)", fhirVersion: "1.6.0", fhirTag: "1_0_2", altName: "FHIR v1.6", canCreate: true, supportsDataSets: false},
                    {index: "6", name: "FHIR STU 3 (v3.0.1)", fhirVersion: "3.0.1", fhirTag: "3_0_1", altName: "FHIR v3.0.1", canCreate: true, supportsDataSets: true}
                ],
                sandboxDescription: {
                    title: "What is a sandbox?",
                    description: "A sandbox is your very own instance of an HSPC Platform* combined with tools and utilities to help you build and test your medical apps.",
                    bottomNote: "*An HSPC Platform is a standardized way to interact with a medical system such as an EHR, Hospital, Clinic, HIE, PHR, Lab, Insurer, etc.",
                    checkList: [
                        "Create apps for practitioners that launch within an EHR, smart phone, tablet, or web browser",
                        "Create apps for patients and their related persons that launch from a smart phone, tablet, web browser, or personal computer",
                        "Create backend services that interact directly with HSPC Platforms*",
                        "Verify your app follows the SMART security and launch context standards",
                        "Run your app against your very own FHIR server",
                        "Test your apps by creating various launch scenarios",
                        "Create practitioners, patients, and clinical data",
                        "Verify that your app is HSPC compliant"
                    ]
                },
                documentationLinks : [
                    {name: "registerAnApp", link: "https://healthservices.atlassian.net/wiki/display/HSPC/Sandbox+Registered+Apps"},
                    {name: "inboundRegisterApp", link: "https://bitbucket.org/hspconsortium/registration"},
                    {name: "sandboxVersions", link: "http://hl7.org/fhir/directory.html"},
                    {name: "launchScenarios", link: "https://healthservices.atlassian.net/wiki/display/HSPC/Sandbox+Launch+Scenarios"},
                    {name: "sandboxPersona", link: "https://healthservices.atlassian.net/wiki/display/HSPC/Sandbox+Persona"},       
                    {name: "mainDocs", link: "https://healthservices.atlassian.net/wiki/display/HSPC/HSPC+Sandbox"}
                ]
        }
    });
}]);