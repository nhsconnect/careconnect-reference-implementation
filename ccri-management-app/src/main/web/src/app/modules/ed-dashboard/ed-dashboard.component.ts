import {Component, OnInit, ViewChild} from '@angular/core';
import OlMap from 'ol/Map';
import OlXYZ from 'ol/source/XYZ';
import OlTileLayer from 'ol/layer/Tile';
import OlView from 'ol/View';
import OlProj from 'ol/proj';

@Component({
  selector: 'app-ed-dashboard',
  templateUrl: './ed-dashboard.component.html',
  styleUrls: ['./ed-dashboard.component.css']
})
export class EdDashboardComponent implements OnInit {


    map: OlMap;
    source: OlXYZ;
    layer: OlTileLayer;
    view: OlView;


    constructor() { }

  ngOnInit() {
      this.source = new OlXYZ({
          url: 'http://tile.osm.org/{z}/{x}/{y}.png'
      });

      this.layer = new OlTileLayer({
          source: this.source
      });

      this.view = new OlView({
          center: OlProj.fromLonLat([6.661594, 50.433237]),
          zoom: 3
      });

      this.map = new OlMap({
          target: 'map',
          layers: [this.layer],
          view: this.view
      });
      /*
      this.map = new ol.Map({
          target: 'map',
          layers: [
              new ol.layer.Tile({
                  source: new ol.source.OSM()
              })
          ],
          view: new ol.View({
              center: ol.proj.fromLonLat([-1.5230420347013478, 53.80634615690993]),
              zoom: 13
          })
      });

      var lonLat = new ol.LonLat(-1.5230420347013478 ,53.80634615690993  )
          .transform(
              new ol.Projection("EPSG:4326"), // transform from WGS 1984
              this.map.getProjectionObject() // to Spherical Mercator Projection
          );

      var zoom=16;

      var markers = new ol.Layer.Markers( "Markers" );
      this.map.addLayer(markers);

      markers.addMarker(new ol.Marker(lonLat));
*/
     /*
      let mapProp = {
          center: new google.maps.LatLng(53.80634615690993, -1.5230420347013478),
          zoom: 14,
          mapTypeId: google.maps.MapTypeId.ROADMAP
      };
      this.map = new google.maps.Map(this.gmapElement.nativeElement, mapProp);

*/

      this.showCustomMarker();

  }

    showCustomMarker() {

/*
        this.map.setCenter(new google.maps.LatLng(53.80, -1.5295702591538431));

        let location = new google.maps.LatLng(53.80634615690993, -1.5230420347013478);

        var trafficLayer = new google.maps.TrafficLayer();
        trafficLayer.setMap(this.map);

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

        */
    }



}
