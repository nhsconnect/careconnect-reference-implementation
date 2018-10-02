import {Component, OnInit, ViewChild} from '@angular/core';

declare var google: any;

@Component({
  selector: 'app-ed-dashboard',
  templateUrl: './ed-dashboard.component.html',
  styleUrls: ['./ed-dashboard.component.css']
})
export class EdDashboardComponent implements OnInit {

    @ViewChild('gmap') gmapElement: any;

    map: google.maps.Map;

    latitude: any;
    longitude: any;

    iconBase = 'https://maps.google.com/mapfiles/kml/shapes/';

  constructor() { }

  ngOnInit() {
      let mapProp = {
          center: new google.maps.LatLng(53.80634615690993, -1.5230420347013478),
          zoom: 13,
          mapTypeId: google.maps.MapTypeId.ROADMAP
      };
      this.map = new google.maps.Map(this.gmapElement.nativeElement, mapProp);



      this.showCustomMarker();

  }

    showCustomMarker() {


        this.map.setCenter(new google.maps.LatLng(53.80634615690993, -1.5230420347013478));



       // console.log(`selected marker: ${this.selectedMarkerType}`);

        //https://fusiontables.google.com/data?docid=1BDnT5U1Spyaes0Nj3DXciJKa_tuu7CzNRXWdVA#map:id=3

        let location = new google.maps.LatLng(53.80634615690993, -1.5230420347013478);
        let marker = new google.maps.Marker({
            position: location,
            map: this.map,
            icon: this.iconBase + "hospitals_maps.png",
            text: 'YAS',
            title: 'Got you!'
        });
        location = new google.maps.LatLng(53.795387709017916, -1.5295702591538431);
        marker = new google.maps.Marker({
            position: location,
            map: this.map,
            icon: this.iconBase + "cabs_maps.png",
            text: 'YAS'
        });

        location = new google.maps.LatLng(53.796284092469236,-1.5508230590282892);
        marker = new google.maps.Marker({
            position: location,
            map: this.map,
            icon: this.iconBase + "man_maps.png",
            text: 'YAS'
        });
    }



}
