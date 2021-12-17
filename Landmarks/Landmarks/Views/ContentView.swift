//
//  ContentView.swift
//  Landmarks
//
//  Created by Jinde Zou on 12/15/21.
//  Copyright © 2021 Jinde Zou. All rights reserved.
//

import SwiftUI

//Sample codes are from below docs:
//https://developer.apple.com/tutorials/swiftui/creating-and-combining-views
//https://developer.apple.com/tutorials/swiftui/building-lists-and-navigation
//https://developer.apple.com/tutorials/swiftui/handling-user-input

struct ContentView: View {
    
    //Use the @StateObject attribute to initialize a model object for a given property only once during the life time of the app.
    //This is true when you use the attribute in an app instance, as shown here, as well as when you use it in a view.
    //Comment the @StateObject, because current xcode can't support it
    //@StateObject
    private var modelData = ModelData()
    
    var body: some View {
        //Add the model object to the environment, which makes the object available to any subview.
        LandmarkList().environmentObject(modelData)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
