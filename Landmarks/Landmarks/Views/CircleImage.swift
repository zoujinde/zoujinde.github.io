//
//  CircleImage.swift
//  Landmarks
//
//  Created by Jinde Zou on 12/16/21.
//  Copyright © 2021 Jinde Zou. All rights reserved.
//

import SwiftUI

struct CircleImage: View {
    var image: Image
    
    var body: some View {
        //Comment the hard code of Image
        //Image("turtlerock").clipShape(Circle()).overlay(Circle().stroke(lineWidth: 5)).shadow(radius: 7)
        
        // Use the dynamic var image
        image.clipShape(Circle()).overlay(Circle().stroke(lineWidth: 5)).shadow(radius: 7)
    }
}

struct CircleImage_Previews: PreviewProvider {
    static var previews: some View {
        CircleImage(image: Image("turtlerock"))
    }
}
