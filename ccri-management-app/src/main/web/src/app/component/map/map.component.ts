import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';


@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements OnInit {
/*
    map: Map;


    source: XYZ;
    layer: TileLayer;

    ambLayer: TileLayer;
    patLayer: TileLayer;
    edLayer: TileLayer;


    view: View;
    markerEd: Style;
    markerAmb: Style;
    markerPat: Style;


    edSource : Vector;
    ambSource : Vector;
    patSource : Vector;

*/
  //  @ViewChild('mapref') mapRef: ElementRef;

  constructor() { }

  ngOnInit() {
/*
      console.log('init');
      let proj = fromLonLat([-1.5230420347013478, 53.80634615690993]);
      this.source = new XYZ({
          url: 'http://tile.osm.org/{z}/{x}/{y}.png'
      });

      // https://tile.thunderforest.com/neighbourhood/{z}/{x}/{y}.png?apikey=<insert-your-apikey-here>

      this.markerEd = new Style({
          image: new Icon( ({
              anchor: [0.5, 46],
              anchorXUnits: 'fraction',
              anchorYUnits: 'pixels',
              opacity: 0.75,
              scale: 0.3,
              src: '/assets/marker.png'
          }))
      });

      this.markerAmb = new Style({
          image: new Icon( ({
              anchor: [0.5, 46],
              anchorXUnits: 'fraction',
              anchorYUnits: 'pixels',
              opacity: 0.75,
              scale: 0.3,
              src: '/assets/tram-2.png'
          }))
      });

      this.markerPat = new Style({
          image: new Icon( ({
              anchor: [0.5, 46],
              anchorXUnits: 'fraction',
              anchorYUnits: 'pixels',
              opacity: 0.75,
              scale: 0.3,
              src: '/assets/street-view.png'
          }))
      });

      this.edSource = new Vector();
      this.ambSource = new Vector();
      this.patSource = new Vector();

      this.layer = new TileLayer({
          source: this.source
      });

      this.ambLayer = new VectorLayer({
          source: this.ambSource,
          style: this.markerAmb,
      });
      this.patLayer = new VectorLayer({
          source: this.patSource,
          style: this.markerPat,
      });
      this.edLayer = new VectorLayer({
          source: this.edSource,
          style: this.markerEd,
      });

      this.view = new View({
          center: proj,
          zoom: 13
      });



      this.map = new Map({
          target: 'mapdiv',
          layers: [this.layer, this.ambLayer, this.patLayer, this.edLayer],
          view: this.view
      });


    let ambFeature = new Feature({
      geometry: new Point(transform([-1.5295702591538431, 53.795387709017916, ],'EPSG:4326',
        'EPSG:3857')),
      name: 'Ambulance: Danzig',
      population: 4000,
      rainfall: 500
    });
    this.ambSource.addFeature(ambFeature);

    let edFeature = new Feature({
      geometry: new Point(transform([-1.5230420347013478,53.80634615690993],'EPSG:4326',
        'EPSG:3857')),
      name: 'ED: LTH',
      population: 4000,
      rainfall: 500
    });
    this.edSource.addFeature(edFeature);

    let patientFeature = new Feature({
      geometry: new Point(transform([-1.5508230590282892,53.796284092469236],'EPSG:4326',
        'EPSG:3857')),
      name: 'Patient: A',
      population: 4000,
      rainfall: 500
    });
    this.patSource.addFeature(patientFeature);

      var featureListener = function ( event ) {
          console.log("featureListenerCalled");
          alert("Feature Listener Called");
      };

    this.map.on("click" , evt => {
        //console.log(evt.pixel);

        var iconFeatureA = this.map.getFeaturesAtPixel(evt.pixel);
        if (iconFeatureA !== null) {
            for(let feature of iconFeatureA) {
                console.log(feature);
                alert(feature.values_.name);
            }
            //var adres = iconFeatureA[0].get("adres");

            evt.preventDefault(); // avoid bubbling
        }
    } );

      console.log('init - end');

      */
  }

}
