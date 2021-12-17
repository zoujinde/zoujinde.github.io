//
//  FavoriteButton.swift
//  Landmarks
//
//  Created by Jinde Zou on 12/17/21.
//  Copyright © 2021 Jinde Zou. All rights reserved.
//

import SwiftUI

struct FavoriteButton: View {
    
    //Add an isSet binding that indicates the button’s current state, and provide a constant value for the preview.
    //Because you use a binding, changes made inside this view propagate back to the data source.
    @Binding var isSet: Bool
    
    var body: some View {
        //Create a Button with an action that toggles the isSet state
        //The title string that you provide for the button’s label doesn’t appear in the UI
        //when you use the iconOnly label style, but VoiceOver uses it to improve accessibility.
        /*
        Button {
            isSet.toggle()
        } label: {
            Label("Toggle Favorite", systemImage: isSet ? "star.fill" : "star")
            .labelStyle(.iconOnly)
            .foregroundColor(isSet ? .yellow : .gray)
        }*/
        Button(
            action:{self.isSet.toggle()},
            label:{Image(systemName: isSet ? "star.fill" : "star")
                .foregroundColor(isSet ? .yellow : .gray)}
        )
        
    }
}

struct FavoriteButton_Previews: PreviewProvider {
    static var previews: some View {
        FavoriteButton(isSet: .constant(true))
    }
}
