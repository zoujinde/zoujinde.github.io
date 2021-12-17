//
//  MapView.swift
//  Landmarks
//
//  Created by Jinde Zou on 12/16/21.
//  Copyright © 2021 Jinde Zou. All rights reserved.
//

import SwiftUI
import MapKit

/*
struct MapView: View {

    // The @State attribute for data binding
    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 34.011_286, longitude: -116.166_868),
        span: MKCoordinateSpan(latitudeDelta: 0.2, longitudeDelta: 0.2)
    )

    var body: some View {
        Map(condinateRegion: $region)
    }
}*/

//Old xcode can't find the Map, so use MKMapView
struct MapView: UIViewRepresentable{
 
    var coordinate: CLLocationCoordinate2D
    
    @State private var region = MKCoordinateRegion()
 
    func makeUIView(context: Context) -> MKMapView {
        MKMapView()
    }
    
    func updateUIView(_ uiView: MKMapView, context: Context) {
        //Comment the hard code
        //let center = CLLocationCoordinate2D(latitude: 34.011_286, longitude: -116.166_868)
        let span = MKCoordinateSpan(latitudeDelta: 0.2, longitudeDelta: 0.2)
        /*
        uiView.setRegion(
            MKCoordinateRegion(center: center,span: span),animated: true
        )*/
        uiView.setRegion(MKCoordinateRegion(center: coordinate, span: span),animated: true)
        
        // Update the var region
        //uiView.setRegion(region, animated: true)
        //updateRegion(coordinate)
        //uiView.setRegion(coordinate)
    }
        
    //Add a method that updates the region based on a coordinate value.
    private func updateRegion(_ coordinate: CLLocationCoordinate2D) {
        region = MKCoordinateRegion(
               center: coordinate,
               span: MKCoordinateSpan(latitudeDelta: 0.2, longitudeDelta: 0.2)
        )
    }

}

struct MapView_Previews: PreviewProvider {
    static var previews: some View {
        //MapView()
        MapView(coordinate: CLLocationCoordinate2D(latitude: 34.011_286, longitude: -116.166_868))
    }
}
