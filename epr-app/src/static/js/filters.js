'use strict';

/* Filters */

angular.module('sandManApp.filters', []).filter('formatAttribute', function ($filter) {
    return function (input) {
        if (Object.prototype.toString.call(input) === '[object Date]') {
            return $filter('date')(input, 'MM/dd/yyyy HH:mm');
        } else {
            return input;
        }
    };
}).filter('nameGivenFamily', function () {
    return function (p) {
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
    };
}).filter('nameFamilyGiven', function () {
    return function (p) {
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
            user = personName.family.join(" ") + ", " + personName.given.join(" ");
        } else {
            user = personName.family + ", " + personName.given.join(" ");
        }
        if (personName.suffix) {
            user = user + ", " + personName.suffix.join(", ");
        }

        return user;
    };
}).filter('ageFilter', function () {
    return function (dob) {
        // var dob = patient.birthDate;
        if (!dob) return "";

        //fix year or year-month style dates
        if (/\d{4}$/.test(dob))
            dob = dob + "-01";
        if (/\d{4}-d{2}$/.test(dob))
            dob = dob + "-01";

        return moment(dob).fromNow(true)
            .replace("a ", "1 ")
            .replace(/minutes?/, "min");
    }
}).filter('capFilter', function () {
    return function (input) {
        return (!!input) ? input.charAt(0).toUpperCase() + input.substr(1).toLowerCase() : '';
    }
});

