import {Component, OnInit, ViewChild} from '@angular/core';
import Map from 'ol/Map';
import View from 'ol/View';
import TileLayer from 'ol/layer/Tile';
import XYZ from 'ol/source/XYZ';
import VectorLayer from 'ol/layer/Vector';
import Vector from 'ol/source/Vector';
import { Style ,Icon } from 'ol/style';
import Feature from 'ol/Feature.js';
import Overlay from 'ol/Overlay.js';
import Point from 'ol/geom/Point'
import {fromLonLat} from 'ol/proj';
import {transform} from 'ol/proj';

@Component({
  selector: 'app-ed-dashboard',
  templateUrl: './ed-dashboard.component.html',
  styleUrls: ['./ed-dashboard.component.css']
})
export class EdDashboardComponent implements OnInit {


    map: Map;
    source: XYZ;
    layer: TileLayer;
    markerlayer: TileLayer;
    view: View;
    markerEd: Style;
    markerAmb: Style;
    markerPat: Style;
    markerSource : Vector;

    constructor() { }

  ngOnInit() {

      let proj = fromLonLat([-1.5230420347013478, 53.80634615690993]);
      this.source = new XYZ({
          url: 'http://tile.osm.org/{z}/{x}/{y}.png'
      });

    this.markerEd = new Style({
      image: new Icon(/** @type {olx.style.IconOptions} */ ({
        anchor: [0.5, 46],
        anchorXUnits: 'fraction',
        anchorYUnits: 'pixels',
        opacity: 0.75,
        scale: 0.3,
        src: '/assets/marker.png'
      }))
    });

    this.markerAmb = new Style({
      image: new Icon(/** @type {olx.style.IconOptions} */ ({
        anchor: [0.5, 46],
        anchorXUnits: 'fraction',
        anchorYUnits: 'pixels',
        opacity: 0.75,
        scale: 0.3,
        src: '/assets/tram-2.png'
      }))
    });

    this.markerPat = new Style({
      image: new Icon(/** @type {olx.style.IconOptions} */ ({
        anchor: [0.5, 46],
        anchorXUnits: 'fraction',
        anchorYUnits: 'pixels',
        opacity: 0.75,
        scale: 0.3,
        src: '/assets/street_view.png'
      }))
    });

    this.markerSource = new Vector();

    this.layer = new TileLayer({
          source: this.source
      });

    this.markerlayer = new VectorLayer({
      source: this.markerSource,
      style: this.markerAmb,
    }),

      this.view = new View({
          center: proj,
          zoom: 13
      });

      this.map = new Map({
          target: 'map',
          layers: [this.layer, this.markerlayer],
          view: this.view
      });

    let ambFeature = new Feature({
      geometry: new Point(transform([-1.5295702591538431, 53.795387709017916, ],'EPSG:4326',
        'EPSG:3857')),
      name: 'Null Island',
      population: 4000,
      rainfall: 500
    });
    this.markerSource.addFeature(ambFeature);

    let edFeature = new Feature({
      geometry: new Point(transform([-1.5230420347013478,53.80634615690993],'EPSG:4326',
        'EPSG:3857')),
      name: 'Null Island',
      population: 4000,
      rainfall: 500
    });
    this.markerSource.addFeature(edFeature);

    let patientFeature = new Feature({
      geometry: new Point(transform([-1.5508230590282892,53.796284092469236],'EPSG:4326',
        'EPSG:3857')),
      name: 'Null Island',
      population: 4000,
      rainfall: 500
    });
    this.markerSource.addFeature(patientFeature);

    this.map.on("click" , evt => {
      alert( 'click' + evt);
    } );

   // iconFeature.on( 'click', this.clickMarker );



    /*
          let ed = new Overlay({
            position: new Point(fromLonLat([-1.5230420347013478,53.80634615690993],'EPSG:4326',
              'EPSG:3857')),
            positioning: 'center-center',
            style : this.markerStyle,
            stopEvent: false
          });
          this.layer.addOverlay(ed);
*/

  }


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


    }
*/


}
