//
//  ContentView.swift
//  QuizApp
//
//  Created by Jinde Zou on 6/13/22.
//  Copyright © 2022 QuizApp. All rights reserved.
//

import SwiftUI

struct ContentView: View {

    private var mQuizView = QuizView()

    // init
    init () {
        let url = URL(string: "https://raw.githubusercontent.com/zoujinde/zoujinde.github.io/main/server.txt")
        mQuizView.load(gitUrl: url!)
    }

    var body: some View {
        mQuizView
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}

