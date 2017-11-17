var fhirClient = null;
var code = getParameterByName("code");
var launched = false;

$('#relaunch-message').hide();

if (code === null) {

    var key = window.location.search.slice(1);
    if (!(key in window.localStorage)) {
        console.log('Failed to launch app -- no launch key.');
        $('#patient-details').hide();
        $('#relaunch-message').show();
    } else {
        window.localStorage['embeddedLaunchKey'] = key;
        onStorage();
        window.addEventListener('storage', onStorage, false);
    }

} else {
    // window.history.replaceState = false;
    FHIR.oauth2.ready(function(newSmart){
        fhirClient = newSmart;

        var savedKey = window.localStorage['embeddedLaunchKey'];
        window.localStorage.removeItem('embeddedLaunchKey');
        $('#patient-details').hide();

        var details = JSON.parse(window.localStorage[savedKey]);
        getFhirUserResource(fhirClient, details.launchDetails.userPersona.resourceUrl)
            .done(function(profileResult){
                document.getElementById("user-name").innerHTML = profileResult.name;
            }).fail(function(){
        });

        if (details.launchDetails.patientContext) {
            getFhirUserResource(fhirClient, "Patient/" + details.launchDetails.patientContext)
                .done(function (patientResult) {
                    $('#patient-details').show();
                    document.getElementById("patient-name").innerHTML = patientResult.name;
                    document.getElementById("patient-gender").innerHTML = patientResult.details.gender;

                    // Check for the patient-birthTime Extension
                    if (typeof patientResult.details.extension !== "undefined") {
                        patientResult.details.extension.forEach(function (extension) {
                            if (extension.url == "http://hl7.org/fhir/StructureDefinition/patient-birthTime") {
                                patientResult.details.dob = extension.valueDateTime;
                            }
                        });
                    }

                    if (patientResult.details.dob === undefined) {
                        patientResult.details.dob = patientResult.details.birthDate;
                    }
                    patientResult.details.dob = new Date(patientResult.details.dob);

                    document.getElementById("patient-dob").innerHTML = moment(patientResult.details.dob).format('D MMM YYYY HH:mm');
                    document.getElementById("patient-age").innerHTML = ageFilter(patientResult.details.dob);
                }).fail(function (error) {
                    console.log(error);
            });
        }

        var iframe = document.getElementById('embeddedSmartAppIframe');
        iframe.src = "launch.html?" + savedKey;
    });
}

function onStorage() {

    var key = window.localStorage['embeddedLaunchKey'];
    console.log("key " + key);

    if (key === "" || launched || window.localStorage[key] === 'requested-launch') {
        return;
    }

    launched = true;
    var details = JSON.parse(window.localStorage[key]);

    delete sessionStorage.tokenResponse;

    // window.location.origin does not exist in some non-webkit browsers
    if (!window.location.origin) {
        window.location.origin = window.location.protocol + "//"
            + window.location.hostname
            + (window.location.port ? ':' + window.location.port : '');
    }

    var thisUri = window.location.origin + window.location.pathname;
    var thisUrl = thisUri.replace(/\/+$/, "/");

    var client = {
        "client_id": "sand_man",
        "redirect_uri": thisUrl,
        "scope": "smart/orchestrate_launch user/*.* profile openid"
    };

    FHIR.oauth2.authorize({
        client: client,
        server: details.iss,
        from: thisUrl
    }, function (err) {
    });
}

function getFhirUserResource(fhirClient, userId) {
    var deferred = $.Deferred();
    var userIdSections = userId.split("/");

    $.when(fhirClient.api.read({type: userIdSections[userIdSections.length-2], id: userIdSections[userIdSections.length-1]}))
        .done(function(userResult){

            var user = {name:""};
            user.name = nameGivenFamily(userResult.data);
            user.id  = userResult.data.id;
            user.details = userResult.data;
            deferred.resolve(user);
        }).fail(function(){
        deferred.reject();
    });
    return deferred;
}

function nameGivenFamily(p){
    var isArrayName = p && p.name && p.name[0];
    var personName;

    if (isArrayName) {
        personName = p && p.name && p.name[0];
        if (!personName) return null;

    } else {
        personName = p && p.name;
        if (!personName) return null;
    }

    var user;
    if (Object.prototype.toString.call(personName.family) === '[object Array]') {
        user = personName.given.join(" ") + " " + personName.family.join(" ");
    } else {
        user = personName.given.join(" ") + " " + personName.family;
    }
    if (personName.suffix) {
        user = user + ", " + personName.suffix.join(", ");
    }
    return user;
}

function ageFilter(date) {
    var yearNow = new Date().getYear();
    var monthNow = new Date().getMonth();
    var dateNow = new Date().getDate();

    var yearDob = new Date(date).getYear();
    var monthDob = new Date(date).getMonth();
    var dateDob = new Date(date).getDate();

    var yearAge = yearNow - yearDob;
    var monthAge = null;
    var dateAge = null;

    if (monthNow >= monthDob)
        monthAge = monthNow - monthDob;
    else {
        yearAge--;
        monthAge = 12 + monthNow - monthDob;
    }

    if (dateNow >= dateDob)
        dateAge = dateNow - dateDob;
    else {
        monthAge--;
        dateAge = 31 + dateNow - dateDob;
        if (monthAge < 0) {
            monthAge = 11;
            yearAge--;
        }
    }

    var hours = (new Date().getTime() - new Date(date).getTime()) / 36e5;
    if (dateAge > 1) {
        hours = hours/(24 * dateAge);
    }

    if ( (yearAge > 0) && (monthAge > 0) && (dateAge > 0) )
        return yearAge + "y " + monthAge + "m " + dateAge + "d";
    else if ( (yearAge > 0) && (monthAge > 0) && (dateAge == 0) )
        return yearAge + "y " + monthAge + "m";
    else if ( (yearAge > 0) && (monthAge == 0) && (dateAge > 0) )
        return yearAge + "y " + dateAge + "d";
    else if ( (yearAge > 0) && (monthAge == 0) && (dateAge == 0) )
        return yearAge + "y";
    else if ( (yearAge == 0) && (monthAge > 0) && (dateAge > 0) )
        return monthAge + "m " + dateAge + "d";
    else if ( (yearAge == 0) && (monthAge > 0) && (dateAge == 0) )
        return monthAge + "m";
    else if ( (yearAge == 0) && (monthAge == 0) && (dateAge > 1) )
        return dateAge + "d";
    else if ( (yearAge == 0) && (monthAge == 0) && (dateAge > 0) )
        return $filter('number')(hours, 2) + "h";
    else return "Could not calculate age";
}

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? null : decodeURIComponent(results[1].replace(/\+/g, " "));
}
