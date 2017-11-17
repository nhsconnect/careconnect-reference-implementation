'use strict';

angular.module('sandManApp', ['ui.router', 'ngSanitize', 'ngAnimate', 'ui.bootstrap', 'highcharts-ng', 'sandManApp.filters', 'sandManApp.services',
        'sandManApp.controllers', 'sandManApp.directives', "sandManApp.branding", "ngCookies"],
    function ($stateProvider, $urlRouterProvider, $locationProvider) {

//   $locationProvider.html5Mode(true);

        $urlRouterProvider.otherwise('/start');

        $stateProvider

            .state('dashboard-view', {
                url: '/dashboard-view',
                templateUrl: 'static/js/templates/dashboardView.html',
                authenticate: true
            })

            .state('create-sandbox', {
                url: '/create-sandbox',
                templateUrl: 'static/js/templates/createSandbox.html',
                authenticate: true
            })

            .state('progress', {
                url: '/progress',
                templateUrl: 'static/js/templates/createProgress.html',
                authenticate: true
            })

            .state('login', {
                url: '/login',
                templateUrl: 'static/js/templates/login.html'
            })

            .state('patient-view', {
                url: '/patient-view/:source',
                templateUrl: 'static/js/templates/patientView.html',
                needsSandbox: true,
                authenticate: true
            })

            .state('practitioner-view', {
                url: '/practitioner-view/:source',
                templateUrl: 'static/js/templates/practitionerView.html',
                needsSandbox: true,
                authenticate: true
            })

            .state('launch-scenarios', {
                url: '/launch-scenarios',
                templateUrl: 'static/js/templates/launchScenarioView.html',
                needsSandbox: true,
                authenticate: true
            })

            .state('apps', {
                url: '/apps/:source/:action',
                templateUrl: 'static/js/templates/appPickerView.html',
                scenarioBuilderStep: true,
                needsSandbox: true,
                authenticate: true
            })

            .state('personas', {
                url: '/personas',
                templateUrl: 'static/js/templates/personaView.html',
                needsSandbox: true,
                authenticate: true
            })


            .state('persona-view', {
                url: '/persona-view/:source',
                templateUrl: 'static/js/templates/personas.html',
                needsSandbox: true,
                authenticate: true
            })

            .state('patients', {
                url: '/patients',
                templateUrl: 'static/js/templates/patients.html',
                needsSandbox: true,
                authenticate: true
            })

            .state('practitioners', {
                url: '/practitioners',
                templateUrl: 'static/js/templates/practitioners.html',
                needsSandbox: true,
                authenticate: true
            })

            .state('users', {
                url: '/users',
                templateUrl: 'static/js/templates/users.html',
                needsSandbox: true,
                authenticate: true
            })

            .state('manage-apps', {
                url: '/manage-apps',
                templateUrl: 'static/js/templates/apps.html',
                needsSandbox: true,
                authenticate: true
            })

            .state('data-manager', {
                url: '/data-manager',
                templateUrl: 'static/js/templates/dataManager.html',
                needsSandbox: true,
                authenticate: true
            })

            .state('ehr-integration', {
                url: '/ehr-integration',
                templateUrl: 'static/js/templates/ehrIntegration.html',
                needsSandbox: true,
                authenticate: true
            })

            .state('settings', {
                url: '/settings',
                templateUrl: 'static/js/templates/settings.html',
                needsSandbox: true,
                authenticate: true
            })

            .state('admin-dashboard', {
                url: '/admin-dashboard',
                templateUrl: 'static/js/templates/adminDashboard.html',
                authenticate: true
            })

            .state('after-auth', {
                url: '/after-auth',
                templateUrl: 'static/js/templates/after-auth.html'
            })

            .state('404', {
                url: '/404',
                templateUrl: 'static/js/templates/404.html',
                authenticate: true
            })

            .state('error', {
                url: '/error',
                templateUrl: 'static/js/templates/error.html',
                authenticate: true
            })

            .state('future', {
                url: '/future',
                templateUrl: 'static/js/templates/future.html',
                authenticate: true
            })

            .state('start', {
                url: '/start',
                templateUrl: 'static/js/templates/start.html'
            })

            .state('resolve', {
                url: '/resolve/:context/against/:iss/for/:clientName/then/:endpoint',
                templateUrl: 'static/js/templates/resolve.html'
            });
    });
