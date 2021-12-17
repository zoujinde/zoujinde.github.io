//
//  LandmarkDetail.swift
//  Landmarks
//
//  Created by Jinde Zou on 12/16/21.
//  Copyright © 2021 Jinde Zou. All rights reserved.
//

import SwiftUI

struct LandmarkDetail: View {
    
    var landmark: Landmark

    // We already define the modelData in LandmarkList, the duplicated object will be same?
    @EnvironmentObject var modelData: ModelData

    // Compute the index of the input landmark by comparing it with the model data.
    // To support this, you also need access to the environment’s model data.
    var landmarkIndex: Int {
        modelData.landmarks.firstIndex(where: { $0.id == landmark.id })!
    }
    
    var body: some View {

        
        //Change the container from a VStack to a ScrollView so the user can scroll through
        //       the descriptive content, and delete the Spacer, which you no longer need.
        //VStack {
        ScrollView {
            MapView(coordinate: landmark.locationCoordinate)
                .edgesIgnoringSafeArea(.top).frame(height: 300)

            CircleImage(image: landmark.image)
                .offset(y: -130).padding(.bottom, -130)
            
            VStack(alignment: .leading) {
                
                //Embed the landmark’s name in an HStack with a new FavoriteButton;
                //provide a binding to the isFavorite property with the dollar sign ($).
                HStack {
                    Text(landmark.name).font(.title).foregroundColor(.blue)
                    FavoriteButton(isSet: $modelData.landmarks[landmarkIndex].isFavorite)
                }
                
                HStack {
                    Text(landmark.park)
                        .font(.subheadline)
                    Spacer()
                    Text(landmark.state)
                        .font(.subheadline).foregroundColor(.red)
                }
                .font(.subheadline)
                .foregroundColor(.secondary)
                
                Divider()

                Text("About \(landmark.name)").font(.title)
                Text(landmark.description)
                
            }.padding()
            //Spacer()
        }.navigationBarTitle("@ \(landmark.name)")
        //.navigationBarTitle(title: landmark.name, displayMode: .inline)
    }
}

struct LandmarkDetail_Previews: PreviewProvider {
    static var previews: some View {
        LandmarkDetail(landmark: ModelData().landmarks[0])
    }
}
