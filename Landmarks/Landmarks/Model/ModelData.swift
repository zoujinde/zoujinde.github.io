//
//  ModelData.swift
//  Landmarks
//
//  Created by Jinde Zou on 12/16/21.
//  Copyright © 2021 Jinde Zou. All rights reserved.
//

import Foundation
import Combine

//Use an Observable Object for Storage
//Declare a new model type that conforms to the ObservableObject protocol from the Combine framework.
//SwiftUI subscribes to your observable object, and updates any views that need refreshing when the data changes.
final class ModelData: ObservableObject {
    //Move the landmarks array into the model.
    //Create an array of landmarks that you initialize from landmarkData.json.
    //An observable object needs to publish any changes to its data, so that its subscribers can pick up the change.
    //Add the @Published attribute to the landmarks array.
    @Published var landmarks: [Landmark] = load("landmarkData.json")
}

// Load data from json file
func load<T: Decodable>(_ filename: String) -> T {
    let data: Data

    guard let file = Bundle.main.url(forResource: filename, withExtension: nil)
    else {
        fatalError("Couldn't find \(filename) in main bundle.")
    }

    do {
        data = try Data(contentsOf: file)
    } catch {
        fatalError("Couldn't load \(filename) from main bundle:\n\(error)")
    }

    do {
        let decoder = JSONDecoder()
        return try decoder.decode(T.self, from: data)
    } catch {
        fatalError("Couldn't parse \(filename) as \(T.self):\n\(error)")
    }
}
