//
//  LandmarkList.swift
//  Landmarks
//
//  Created by Jinde Zou on 12/16/21.
//  Copyright © 2021 Jinde Zou. All rights reserved.
//

import SwiftUI

struct LandmarkList: View {
    
    //Add an @EnvironmentObject property declaration to the view, and the environmentObject(_:) modifier to the preview.
    //The modelData property gets its value automatically, as long as the environmentObject(_:) modifier has been applied to a parent.
    @EnvironmentObject var modelData: ModelData
    
    //Filter the List View
    //You can customize the list view so that it shows all of the landmarks, or just the user’s favorites.
    //To do this, you’ll need to add a bit of state to the LandmarkList type.
    //State is a value, or a set of values, that can change over time, and that affects a view’s behavior, content, or layout.
    //You use a property with the @State attribute to add state to a view.
    @State private var showFavoritesOnly = false
    
    //Compute a filtered version of the landmarks list by checking the showFavoritesOnly property and each landmark.isFavorite value.
    var filteredLandmarks: [Landmark] {
        modelData.landmarks.filter { landmark in
            (!showFavoritesOnly || landmark.isFavorite)
        }
    }

    var body: some View {
        /* static list
        List {
            LandmarkRow(landmark: landmarks[0])
            LandmarkRow(landmark: landmarks[1])
        }*/
        
        // When the Landmark hasn't Identifiable protocol, we have to add the id: \.id
        /*
        List(landmarks, id: \.id) { landmark in
            LandmarkRow(landmark: landmark)
        }
        */

        // When the Landmark has Identifiable protocol, we can remove the id: \.id
        NavigationView {
            //Comment the version without filter, use the filtered version of the list
            //List(landmarks) { landmark in
            /*
            List(filteredLandmarks) { landmark in
                
                //Comment the row view without link
                //LandmarkRow(landmark: landmark)
              
                //Wrap the returned row in a NavigationLink, specifying the LandmarkDetail view as the destination.
                /* Below codes compile error
                NavigationLink {
                    LandmarkDetail()
                } label: {
                    LandmarkRow(landmark: landmark)
                }.navigationTitle("Landmarks")
                 */
                NavigationLink(
                    destination: LandmarkDetail(landmark: landmark),
                    label: {LandmarkRow(landmark: landmark)}
                ).navigationBarTitle("Landmark List")
                
            }*/
            
            //Dynamic views, use the ForEach type instead of passing your collection of data to List.
            List {
                //Add a Toggle view as the first child of the List view, passing a binding to showFavoritesOnly with $
                Toggle(isOn: $showFavoritesOnly) {
                    Text("Favorites only")
                }
                
                ForEach(filteredLandmarks) { landmark in
                    NavigationLink(
                        destination: LandmarkDetail(landmark: landmark),
                        label: {LandmarkRow(landmark: landmark)}
                    ).navigationBarTitle("Landmark List")
                }
            }
            
        }
        
    }
}

//Section 8
//Generate Previews Dynamically
struct LandmarkList_Previews: PreviewProvider {
    static var previews: some View {
        //Embed the LandmarkList in a ForEach instance, using an array as the data.
        ForEach(["iPhone SE (2nd generation)", "iPhone XS Max"], id: \.self) { deviceName in
            LandmarkList()
                .previewDevice(PreviewDevice(rawValue: deviceName))
                .previewDisplayName(deviceName)
                .environmentObject(ModelData())
        }
        //LandmarkList()
    }
}
