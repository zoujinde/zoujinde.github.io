//
//  Landmark.swift
//  Landmarks
//
//  Created by Jinde Zou on 12/16/21.
//  Copyright © 2021 Jinde Zou. All rights reserved.
//

import Foundation
import SwiftUI
import CoreLocation

// To simplify the List codes by adding Identifiable conformance to the Landmark type.
// For example :
// The old codes are : List(landmarks, id: \.id)
// The new codes are : List(landmarks)
struct Landmark: Hashable, Codable, Identifiable {
    
    //Because Landmark conforms to Codable, you can read the value associated with the key
    //  by creating a new property with the same name as the key in landmarkData.json file
    var id: Int
    var name: String
    var park: String
    var state: String
    var description: String
    var isFavorite: Bool

    private var imageName: String

    var image: Image {
        Image(imageName)
    }
    
    private var coordinates: Coordinates

    struct Coordinates: Hashable, Codable {
        var latitude: Double
        var longitude: Double
    }

    var locationCoordinate: CLLocationCoordinate2D {
        CLLocationCoordinate2D(latitude: coordinates.latitude, longitude: coordinates.longitude)
    }
    
}
